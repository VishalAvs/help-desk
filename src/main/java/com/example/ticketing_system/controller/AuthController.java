package com.example.ticketing_system.controller;

import com.example.ticketing_system.dto.LoginRequestDto;
import com.example.ticketing_system.dto.SignUpRequestDto;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    public AuthController() {
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of("ap-southeast-2"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    //Signup with AWS Cognito
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            logger.info("Signup attempt for: {}", signUpRequestDto.getEmail()); // Log attempt

            // Prepare request for Cognito SignUp
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(signUpRequestDto.getEmail())
                    .password(signUpRequestDto.getPassword())
                    .userAttributes(AttributeType.builder().name("email").value(signUpRequestDto.getEmail()).build())
                    .secretHash(getSecretHash(signUpRequestDto.getEmail())) // Generate SECRET_HASH and log it
                    .build();

            logger.debug("SignUpRequest prepared: {}", request); // Log request details

            cognitoClient.signUp(request);
            logger.info("User registered successfully: {}", signUpRequestDto.getEmail()); // Log success
            return ResponseEntity.ok("User registered successfully!");
        } catch (CognitoIdentityProviderException e) {
            logger.error("Error during signup for {}: {}", signUpRequestDto.getEmail(), e.awsErrorDetails().errorMessage()); // Log error
            return ResponseEntity.badRequest().body("Error: " + e.awsErrorDetails().errorMessage());
        }
    }

    //Login with AWS Cognito
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            logger.info("Login attempt for: {}", loginRequestDto.getEmail()); // Log attempt

            String secretHash = getSecretHash(loginRequestDto.getEmail());
            logger.debug("Generated SECRET_HASH login: {}", secretHash); // Log generated SECRET_HASH

            // Prepare request for Cognito Login
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", loginRequestDto.getEmail(),
                            "PASSWORD", loginRequestDto.getPassword(),
                            "SECRET_HASH", secretHash)) // Include secretHash
                    .build();

            logger.debug("InitiateAuthRequest prepared: {}", authRequest); // Log the details of the request

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            logger.info("Login successful for {}. ID Token: {}", loginRequestDto.getEmail(), authResponse.authenticationResult().idToken()); // Log success

            return ResponseEntity.ok("Login successful! Token: " + authResponse.authenticationResult().idToken());
        } catch (CognitoIdentityProviderException e) {
            logger.error("Login failed for {}: {}", loginRequestDto.getEmail(), e.awsErrorDetails().errorMessage()); // Log error
            return ResponseEntity.status(401).body("Invalid credentials: " + e.awsErrorDetails().errorMessage());
        }
    }

    private String getSecretHash(String username) {
        try {
            logger.debug("Generating SECRET_HASH for username: {}", username); // Log username

            String data = username + clientId;
            logger.debug("Data to sign (username + clientId): {}", data); // Log the data being used for hash generation

            SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());

            String secretHash = Base64.getEncoder().encodeToString(rawHmac);
            logger.debug("Generated SECRET_HASH: {}", secretHash); // Log the secret hash
            return secretHash;
        } catch (Exception e) {
            logger.error("Error while generating SECRET_HASH for username: {}", username, e); // Log error during hash generation
            throw new RuntimeException("Error while generating SECRET_HASH", e);
        }
    }
}
