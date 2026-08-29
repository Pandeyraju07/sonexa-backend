package com.sonexa.backend.service;

import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.exception.BusinessException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${sonexa.mail.require-delivery:false}")
    private boolean requireDelivery;

    @Override
    public boolean sendOtpEmail(String toEmail, String otpCode, String purpose) {
        String recipient = toEmail == null ? "" : toEmail.trim().toLowerCase();
        String subject = "FORGOT_PASSWORD".equalsIgnoreCase(purpose)
                ? "Sonexa password reset OTP (valid 1 minute)"
                : "Sonexa verification OTP (valid 1 minute)";
        String htmlContent = buildHtmlEmailContent(otpCode, purpose, recipient);

        log.info("event=SENDING_GMAIL_OTP to={} purpose={} otp={}", recipient, purpose, otpCode);

        String senderUser = smtpUsername == null ? "" : smtpUsername.trim();
        String senderPass = smtpPassword == null ? "" : smtpPassword.trim().replace(" ", "");

        if (mailSender == null || senderUser.isBlank() || senderPass.isBlank()) {
            log.warn("event=OTP_EMAIL_SKIPPED_NO_SMTP_SENDER to={} otp={}", recipient, otpCode);
            if (requireDelivery) {
                throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                        "Email SMTP sender is not configured. Set SONEXA_MAIL_USERNAME / SONEXA_MAIL_PASSWORD or application-local.properties.");
            }
            return false;
        }

        if (recipient.isBlank() || !recipient.contains("@")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "A valid recipient email is required to send OTP");
        }

        try {
            if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl) {
                impl.setUsername(senderUser);
                impl.setPassword(senderPass);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(senderUser, "Sonexa"));
            helper.setTo(recipient);
            helper.setReplyTo(senderUser);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("event=GMAIL_OTP_SENT_SUCCESSFULLY to={} from={}", recipient, senderUser);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=GMAIL_OTP_SEND_FAILED to={} otp={} error={}", recipient, otpCode, e.getMessage(), e);
            if (requireDelivery) {
                throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                        "Unable to send OTP email to " + recipient + ". Check SMTP App Password.");
            }
            return false;
        }
    }

    private String buildHtmlEmailContent(String otpCode, String purpose, String recipient) {
        String title = "FORGOT_PASSWORD".equalsIgnoreCase(purpose)
                ? "Password Reset Verification Code"
                : "Account Email Verification";
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #121212; color: #ffffff; padding: 20px; }
                        .container { max-width: 500px; margin: 0 auto; background: #1e1e2e; border-radius: 12px; padding: 30px; box-shadow: 0 8px 24px rgba(0,0,0,0.5); }
                        .logo { font-size: 28px; font-weight: bold; color: #bb86fc; text-align: center; margin-bottom: 20px; }
                        .otp-box { background: #2d2b42; font-size: 36px; font-weight: bold; letter-spacing: 6px; color: #03dac6; text-align: center; padding: 15px; border-radius: 8px; margin: 20px 0; }
                        .footer { font-size: 12px; color: #888888; text-align: center; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="logo">SONEXA</div>
                        <h2 style="text-align: center;">%s</h2>
                        <p>Hi, this code was requested for <strong>%s</strong>.</p>
                        <p>Use the OTP below to continue. Valid for <strong>1 minute</strong>.</p>
                        <div class="otp-box">%s</div>
                        <p style="color: #aaaaaa; font-size: 13px;">If you did not request this code, ignore this email.</p>
                        <div class="footer">&copy; 2026 Sonexa Music Inc. All rights reserved.</div>
                    </div>
                </body>
                </html>
                """.formatted(title, recipient, otpCode);
    }
}
