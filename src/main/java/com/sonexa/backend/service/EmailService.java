package com.sonexa.backend.service;

public interface EmailService {
    /** @return true if the email was delivered, false if skipped/failed (when delivery is optional). */
    boolean sendOtpEmail(String toEmail, String otpCode, String purpose);
}
