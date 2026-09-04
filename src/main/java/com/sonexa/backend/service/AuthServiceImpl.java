package com.sonexa.backend.service;

import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.exception.BusinessException;
import com.sonexa.backend.model.dto.AuthDtos.*;
import com.sonexa.backend.model.entity.OtpCode;
import com.sonexa.backend.model.entity.User;
import com.sonexa.backend.repository.OtpRepository;
import com.sonexa.backend.repository.UserRepository;
import com.sonexa.backend.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Environment environment;

    private String defaultAvatarUrl(String name, String email) {
        String seed = (name != null && !name.isBlank()) ? name.trim() : (email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : "Zynera");
        return "https://api.dicebear.com/7.x/initials/svg?seed=" + java.net.URLEncoder.encode(seed, java.nio.charset.StandardCharsets.UTF_8) + "&backgroundColor=6b3ce9,e534b2,38bdf8&textColor=ffffff";
    }

    private String defaultDisplayName(String name, String email) {
        if (name != null && !name.isBlank()) return name.trim();
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf("@"));
        return "Zynera Listener";
    }

    private boolean isProd() {
        return java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    @Override
    @Transactional
    public AuthResponseData register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("event=REGISTER_ATTEMPT email={}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (user.isEmailVerified()) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS,
                        "An account with email " + email + " already exists");
            }
            // Unverified signup — refresh credentials and resend OTP
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setName(request.name());
            user.setPhone(request.phone());
            user = userRepository.save(user);
            log.info("event=REGISTER_RESEND_OTP userId={} email={}", user.getId(), email);
        } else {
            user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setName(request.name());
            user.setPhone(request.phone());
            user.setProvider("LOCAL");
            user.setEmailVerified(false);
            user.setRole("USER");
            user.setProfilePicUrl(defaultAvatarUrl(request.name(), email));
            user = userRepository.save(user);
        }

        String otpCode = generateOtpCode();
        saveOtpCode(user.getEmail(), otpCode, "REGISTER");
        boolean emailDelivered = emailService.sendOtpEmail(user.getEmail(), otpCode, "REGISTER");
        if (!emailDelivered && isProd()) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                    "Could not send verification email. Please try again.");
        }
        if (!emailDelivered) {
            log.warn("event=OTP_EMAIL_NOT_DELIVERED_DEV userId={}", user.getId());
        }

        log.info("event=USER_REGISTERED_SUCCESS userId={} email={} emailDelivered={}",
                user.getId(), user.getEmail(), emailDelivered);

        return new AuthResponseData(null, null, mapToUserProfile(user), true, null, emailDelivered);
    }

    @Override
    public AuthResponseData login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("event=LOGIN_ATTEMPT email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        if ("LOCAL".equalsIgnoreCase(user.getProvider()) && user.getPassword() != null) {
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
            }
        }

        if ("LOCAL".equalsIgnoreCase(user.getProvider()) && !user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Please verify your email before signing in");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId(), user.getRole());

        log.info("event=LOGIN_SUCCESS userId={} email={} role={}", user.getId(), user.getEmail(), user.getRole());

        return new AuthResponseData(accessToken, refreshToken, mapToUserProfile(user), false);
    }

    @Override
    public AuthResponseData adminLogin(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("event=ADMIN_LOGIN_ATTEMPT email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Admin access required");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account is disabled");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), "ADMIN");
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId(), "ADMIN");

        log.info("event=ADMIN_LOGIN_SUCCESS userId={} email={}", user.getId(), user.getEmail());
        return new AuthResponseData(accessToken, refreshToken, mapToUserProfile(user), false);
    }

    @Override
    @Transactional
    public OtpSendData sendOtp(SendOtpRequest request) {
        String email = request.email().trim().toLowerCase();
        String purpose = request.purpose() != null ? request.purpose() : "REGISTER";

        log.info("event=SEND_OTP_REQUEST email={} purpose={}", email, purpose);

        String otpCode = generateOtpCode();
        saveOtpCode(email, otpCode, purpose);
        boolean emailDelivered = emailService.sendOtpEmail(email, otpCode, purpose);
        if (!emailDelivered && isProd()) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                    "Could not send verification email. Please try again.");
        }
        return new OtpSendData(null, emailDelivered);
    }

    @Override
    @Transactional
    public AuthResponseData verifyOtp(VerifyOtpRequest request) {
        String email = request.email().trim().toLowerCase();
        String purpose = request.purpose() != null ? request.purpose() : "REGISTER";

        log.info("event=VERIFY_OTP_REQUEST email={} purpose={}", email, purpose);

        OtpCode otpEntry = otpRepository.findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OTP, "No active OTP request found for email"));

        if (otpEntry.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED, "OTP has expired. Please request a new code");
        }

        if (!otpEntry.getOtp().equals(request.otp().trim())) {
            throw new BusinessException(ErrorCode.INVALID_OTP, "Invalid OTP code provided");
        }

        otpEntry.setUsed(true);
        otpRepository.save(otpEntry);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName("Sonexa Listener");
            newUser.setProvider("LOCAL");
            newUser.setProfilePicUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300");
            return newUser;
        });

        user.setEmailVerified(true);
        user = userRepository.save(user);

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());

        log.info("event=OTP_VERIFIED_SUCCESS email={}", email);

        return new AuthResponseData(accessToken, refreshToken, mapToUserProfile(user), false);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("event=FORGOT_PASSWORD_REQUEST email={}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.info("event=FORGOT_PASSWORD_UNKNOWN_EMAIL");
            return;
        }

        String otpCode = generateOtpCode();
        saveOtpCode(user.getEmail(), otpCode, "FORGOT_PASSWORD");
        emailService.sendOtpEmail(user.getEmail(), otpCode, "FORGOT_PASSWORD");
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("event=RESET_PASSWORD_REQUEST email={}", email);

        OtpCode otpEntry = otpRepository.findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email, "FORGOT_PASSWORD")
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OTP, "No active password reset request found"));

        if (otpEntry.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED, "OTP has expired. Please request a new code");
        }

        if (!otpEntry.getOtp().equals(request.otp().trim())) {
            throw new BusinessException(ErrorCode.INVALID_OTP, "Invalid OTP code provided");
        }

        otpEntry.setUsed(true);
        otpRepository.save(otpEntry);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("event=RESET_PASSWORD_SUCCESS email={}", email);
    }

    @Override
    @Transactional
    public AuthResponseData googleSignIn(GoogleSignInRequest request) {
        String email = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : "google_user_" + System.currentTimeMillis() + "@gmail.com";

        log.info("event=GOOGLE_SIGNIN_ATTEMPT email={}", email);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(request.name() != null ? request.name() : "Google Listener");
            newUser.setProfilePicUrl(request.profilePicUrl() != null ? request.profilePicUrl() : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300");
            newUser.setProvider("GOOGLE");
            newUser.setProviderId(request.idToken());
            newUser.setEmailVerified(true);
            return userRepository.save(newUser);
        });

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());

        log.info("event=GOOGLE_SIGNIN_SUCCESS userId={}", user.getId());

        return new AuthResponseData(accessToken, refreshToken, mapToUserProfile(user), false);
    }

    @Override
    @Transactional
    public AuthResponseData appleSignIn(AppleSignInRequest request) {
        String email = request.email() != null && !request.email().isBlank()
                ? request.email().trim().toLowerCase()
                : "apple_user_" + System.currentTimeMillis() + "@privaterelay.appleid.com";

        log.info("event=APPLE_SIGNIN_ATTEMPT email={}", email);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(request.name() != null ? request.name() : "Apple Listener");
            newUser.setProfilePicUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300");
            newUser.setProvider("APPLE");
            newUser.setProviderId(request.identityToken());
            newUser.setEmailVerified(true);
            return userRepository.save(newUser);
        });

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());

        log.info("event=APPLE_SIGNIN_SUCCESS userId={}", user.getId());

        return new AuthResponseData(accessToken, refreshToken, mapToUserProfile(user), false);
    }

    @Override
    public AuthResponseData refreshToken(RefreshTokenRequest request) {
        try {
            String email = jwtUtils.extractUsername(request.refreshToken());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found for refresh token"));

            if (jwtUtils.validateToken(request.refreshToken(), email) && jwtUtils.isRefreshToken(request.refreshToken())) {
                String newAccessToken = jwtUtils.generateAccessToken(user.getEmail(), user.getId(), user.getRole());
                String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId(), user.getRole());
                return new AuthResponseData(newAccessToken, newRefreshToken, mapToUserProfile(user), false);
            } else {
                throw new BusinessException(ErrorCode.EXPIRED_TOKEN, "Refresh token is expired");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
        }
    }

    @Override
    public void logout(String email) {
        log.info("event=LOGOUT_SUCCESS");
    }

    @Override
    @Transactional
    public void deleteAccount(String ignoredClientUserId) {
        deleteCurrentUser();
    }

    @Transactional
    public void deleteCurrentUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found"));
        log.info("event=DELETE_ACCOUNT userId={}", user.getId());
        userRepository.delete(user);
    }

    private static final int OTP_VALIDITY_MINUTES = 1;

    private String generateOtpCode() {
        int code = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(code);
    }

    private void saveOtpCode(String email, String otpCode, String purpose) {
        // Invalidate any previous unused OTPs for this email + purpose (resend / re-request)
        otpRepository.findByEmailAndPurposeAndIsUsedFalse(email, purpose).forEach(existing -> {
            existing.setUsed(true);
            otpRepository.save(existing);
        });

        OtpCode otpEntry = new OtpCode();
        otpEntry.setEmail(email);
        otpEntry.setOtp(otpCode);
        otpEntry.setPurpose(purpose);
        otpEntry.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        otpEntry.setUsed(false);
        otpRepository.save(otpEntry);
    }

    private UserProfileData mapToUserProfile(User user) {
        return new UserProfileData(
                "usr_" + user.getId(),
                defaultDisplayName(user.getName(), user.getEmail()),
                user.getEmail(),
                user.getProfilePicUrl() != null && !user.getProfilePicUrl().isBlank() ? user.getProfilePicUrl() : defaultAvatarUrl(user.getName(), user.getEmail()),
                user.isPremium(),
                user.isEmailVerified(),
                user.getFollowersCount(),
                user.getFollowingCount()
        );
    }
}
