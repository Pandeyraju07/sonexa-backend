package com.sonexa.backend.controller;

import com.sonexa.backend.common.ApiResponse;
import com.sonexa.backend.model.dto.AuthDtos.*;
import com.sonexa.backend.service.AuthService;
import com.sonexa.backend.util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 1. REGISTER (CREATE ACCOUNT)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseData>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponseData data = authService.register(request);
        return ResponseEntity.ok(ResponseUtil.success(
                "User registered successfully. Verification OTP sent to your email.", data));
    }

    // 2. LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseData>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponseData data = authService.login(request);
        return ResponseEntity.ok(ResponseUtil.success("Login successful", data));
    }

    // 2b. ADMIN LOGIN
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponseData>> adminLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponseData data = authService.adminLogin(request);
        return ResponseEntity.ok(ResponseUtil.success("Admin login successful", data));
    }

    // 3. SEND OTP
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<OtpSendData>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        OtpSendData data = authService.sendOtp(request);
        return ResponseEntity.ok(ResponseUtil.success("Verification OTP sent successfully", data));
    }

    // 4. VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseData>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponseData data = authService.verifyOtp(request);
        return ResponseEntity.ok(ResponseUtil.success("OTP verified successfully!", data));
    }

    // 5. FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ResponseUtil.success("Password reset OTP code sent to " + request.email(), null));
    }

    // 6. RESET PASSWORD
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ResponseUtil.success("Password updated successfully. Please log in with your new password.", null));
    }

    // 7. CONTINUE WITH GOOGLE
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponseData>> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
        AuthResponseData data = authService.googleSignIn(request);
        return ResponseEntity.ok(ResponseUtil.success("Google authentication successful", data));
    }

    // 8. CONTINUE WITH APPLE
    @PostMapping("/apple")
    public ResponseEntity<ApiResponse<AuthResponseData>> appleSignIn(@Valid @RequestBody AppleSignInRequest request) {
        AuthResponseData data = authService.appleSignIn(request);
        return ResponseEntity.ok(ResponseUtil.success("Apple authentication successful", data));
    }

    // 9. REFRESH TOKEN
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseData>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponseData data = authService.refreshToken(request);
        return ResponseEntity.ok(ResponseUtil.success("Token refreshed successfully", data));
    }

    // 10. LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout(null);
        return ResponseEntity.ok(ResponseUtil.success("Logged out successfully", null));
    }

    // 11. DELETE ACCOUNT — authenticated user only; never trust client userId
    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        authService.deleteAccount(null);
        return ResponseEntity.ok(ResponseUtil.success("Account deleted successfully", null));
    }
}
