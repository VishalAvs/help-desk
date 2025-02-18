package com.example.ticketing_system.controller;

import com.example.ticketing_system.dto.LoginRequestDto;
import com.example.ticketing_system.dto.SignUpRequestDto;
import com.example.ticketing_system.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final CognitoIdentityProviderClient cognitoClient;
    private final EmailService emailService;
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    public AuthController(EmailService emailService) {
        this.emailService = emailService;
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of("ap-southeast-2"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        logger.info("Signup attempt for: {}", signUpRequestDto.getEmail());

        try {
            // Register user in Cognito (unconfirmed)
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(signUpRequestDto.getEmail())
                    .password(signUpRequestDto.getPassword())
                    .userAttributes(AttributeType.builder().name("email").value(signUpRequestDto.getEmail()).build())
                    .secretHash(getSecretHash(signUpRequestDto.getEmail()))
                    .build();

            cognitoClient.signUp(request);

            // Send OTP via SES
            String otp = emailService.sendVerificationEmail(signUpRequestDto.getEmail());
            otpStorage.put(signUpRequestDto.getEmail(), otp);

            logger.info("User {} registered successfully. OTP sent to email.", signUpRequestDto.getEmail());
            return ResponseEntity.ok("User registered. Please verify your email with the OTP sent to you.");
        } catch (CognitoIdentityProviderException e) {
            logger.error("Error during signup for {}: {}", signUpRequestDto.getEmail(), e.awsErrorDetails().errorMessage(), e);
            return ResponseEntity.badRequest().body("Error during signup: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during signup for {}: {}", signUpRequestDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        logger.info("OTP verification attempt for: {}", email);

        if (!otpStorage.containsKey(email)) {
            logger.warn("Invalid OTP or OTP expired for email: {}", email);
            return ResponseEntity.badRequest().body("Invalid email or OTP expired.");
        }

        if (!otpStorage.get(email).equals(otp)) {
            logger.warn("Invalid OTP for email: {}", email);
            return ResponseEntity.status(401).body("Invalid OTP.");
        }

        // Confirm user in Cognito
        try {
            AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build();

            cognitoClient.adminConfirmSignUp(confirmRequest);
            otpStorage.remove(email);

            logger.info("User {} successfully verified.", email);
            return ResponseEntity.ok("User verified successfully.");
        } catch (CognitoIdentityProviderException e) {
            logger.error("Error confirming user {}: {}", email, e.awsErrorDetails().errorMessage(), e);
            return ResponseEntity.badRequest().body("Error during verification: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during verification for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body("Unexpected error occurred during verification: " + e.getMessage());
        }
    }


    private String getSecretHash(String username) {
        try {
            String data = username + clientId;
            SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            logger.error("Error generating SECRET_HASH for username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error while generating SECRET_HASH", e);
        }
    }
}
