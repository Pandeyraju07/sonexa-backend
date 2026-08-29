package com.sonexa.backend.service;

import com.sonexa.backend.model.dto.AuthDtos.*;

public interface AuthService {
    AuthResponseData register(RegisterRequest request);
    AuthResponseData login(LoginRequest request);
    AuthResponseData adminLogin(LoginRequest request);
    OtpSendData sendOtp(SendOtpRequest request);
    AuthResponseData verifyOtp(VerifyOtpRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponseData googleSignIn(GoogleSignInRequest request);
    AuthResponseData appleSignIn(AppleSignInRequest request);
    AuthResponseData refreshToken(RefreshTokenRequest request);
    void logout(String email);
    void deleteAccount(String userId);
}
