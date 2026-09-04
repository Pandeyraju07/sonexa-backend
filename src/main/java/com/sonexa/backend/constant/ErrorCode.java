package com.sonexa.backend.constant;

public enum ErrorCode {

    VALIDATION_ERROR("ERR_40001", "Invalid request parameters"),
    INVALID_CREDENTIALS("ERR_40101", "Invalid email or password"),
    UNAUTHORIZED("ERR_40102", "Unauthorized access"),
    EXPIRED_TOKEN("ERR_40103", "Token has expired"),
    INVALID_TOKEN("ERR_40104", "Invalid token signature or structure"),
    FORBIDDEN("ERR_40301", "Access denied"),
    USER_NOT_FOUND("ERR_40401", "User account not found"),
    RESOURCE_NOT_FOUND("ERR_40402", "Requested resource not found"),
    EMAIL_ALREADY_EXISTS("ERR_40901", "An account with this email already exists"),
    INVALID_OTP("ERR_40002", "Invalid or expired OTP code"),
    OTP_EXPIRED("ERR_40003", "OTP has expired. Please request a new code"),
    SOCIAL_AUTH_FAILED("ERR_40004", "Social authentication failed"),
    EMAIL_SEND_FAILED("ERR_50002", "Failed to send verification email. Please try again later"),
    METHOD_NOT_ALLOWED("ERR_40501", "HTTP method not supported"),
    UNSUPPORTED_MEDIA_TYPE("ERR_41501", "Unsupported media type"),
    INTERNAL_SERVER_ERROR("ERR_50001", "An unexpected error occurred on the server");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
