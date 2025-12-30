package com.example.auth_service.service.Impl;

import com.example.auth_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${resend.api.url}")
    private String resendApiUrl;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.api.from}")
    private String fromEmail;

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token, String username) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            // Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetUrl", resetUrl);

            // Render HTML
            String htmlContent = templateEngine.process("email/password-reset", context);

            // Prepare Request Body
            EmailRequest emailRequest = new EmailRequest(
                    fromEmail,
                    List.of(to),
                    "🔒 Password Reset Request",
                    htmlContent);

            // Send Request using RestClient
            RestClient restClient = RestClient.builder()
                    .baseUrl(resendApiUrl)
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            restClient.post()
                    .body(emailRequest)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    // DTOs for Resend API
    record EmailRequest(String from, List<String> to, String subject, String html) {
    }
}
