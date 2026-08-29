package com.sonexa.backend.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static record RegisterRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters long")
            String password,

            @NotBlank(message = "Name is required")
            String name,

            String phone
    ) {}

    public static record LoginRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public static record SendOtpRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email,

            String purpose // REGISTER, FORGOT_PASSWORD
    ) {}

    public static record VerifyOtpRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "OTP code is required")
            @Size(min = 4, max = 6, message = "OTP code must be 4 to 6 digits")
            String otp,

            String purpose // REGISTER, FORGOT_PASSWORD
    ) {}

    public static record ForgotPasswordRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email
    ) {}

    public static record ResetPasswordRequest(
            @NotBlank(message = "Email address is required")
            @Email(message = "Must be a valid email address")
            String email,

            @NotBlank(message = "OTP code is required")
            String otp,

            @NotBlank(message = "New password is required")
            @Size(min = 6, message = "New password must be at least 6 characters long")
            String newPassword
    ) {}

    public static record GoogleSignInRequest(
            @NotBlank(message = "Google ID token is required")
            String idToken,
            String email,
            String name,
            String profilePicUrl
    ) {}

    public static record AppleSignInRequest(
            @NotBlank(message = "Apple identity token is required")
            String identityToken,
            String email,
            String name
    ) {}

    public static record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) {}

    public static record UserProfileData(
            String id,
            String name,
            String email,
            String profilePicUrl,
            boolean isPremium,
            boolean isEmailVerified,
            int followersCount,
            int followingCount
    ) {}

    public static record AuthResponseData(
            String token,
            String refreshToken,
            UserProfileData user,
            boolean otpSent,
            String otp,
            Boolean emailDelivered
    ) {
        public AuthResponseData(String token, String refreshToken, UserProfileData user, boolean otpSent) {
            this(token, refreshToken, user, otpSent, null, null);
        }
    }

    public static record OtpSendData(
            String otp,
            boolean emailDelivered
    ) {}
}
