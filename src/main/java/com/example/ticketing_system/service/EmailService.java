package com.example.ticketing_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.Random;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final SesClient sesClient;

    @Value("${aws.ses.sender.email}")
    private String senderEmail;

    public EmailService(
            @Value("${aws.access.key.id}") String accessKey,
            @Value("${aws.secret.access.key}") String secretKey,
            @Value("${aws.region}") String region) {
        this.sesClient = SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public String sendVerificationEmail(String recipientEmail) {
        String otp = generateOtp();
        String subject = "Your Verification Code";
        String bodyText = "Your OTP for verification is: " + otp + ". It expires in 10 minutes.";

        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(recipientEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(bodyText).build()).build())
                            .build())
                    .source(senderEmail)
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Verification email sent to {}", recipientEmail);
            return otp;
        } catch (SesException e) {
            logger.error("Failed to send email: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send email: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
