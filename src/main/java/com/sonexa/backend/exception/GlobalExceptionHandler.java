package com.sonexa.backend.exception;

import com.sonexa.backend.common.ApiResponse;
import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.util.ResponseUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        log.warn("event=BUSINESS_EXCEPTION errorCode={} message={}",
                ex.getErrorCode().getCode(), ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED, INVALID_CREDENTIALS, EXPIRED_TOKEN, INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND, USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        return jsonResponse(status)
                .body(ResponseUtil.failure(ex.getErrorCode(), ex.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("event=BAD_CREDENTIALS message={}", ex.getMessage());

        return jsonResponse(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtil.failure(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();

        String field = fieldError != null ? fieldError.getField() : null;
        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : ErrorCode.VALIDATION_ERROR.getDescription();

        log.warn("event=VALIDATION_ERROR field={} message={}", field, message);

        return jsonResponse(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.failure(ErrorCode.VALIDATION_ERROR, message, field));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        ConstraintViolation<?> violation = ex.getConstraintViolations().stream().findFirst().orElse(null);

        String field = violation != null ? extractFieldName(violation.getPropertyPath().toString()) : null;
        String message = violation != null ? violation.getMessage() : ErrorCode.VALIDATION_ERROR.getDescription();

        log.warn("event=CONSTRAINT_VIOLATION field={} message={}", field, message);

        return jsonResponse(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.failure(ErrorCode.VALIDATION_ERROR, message, field));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName();
        String message = "Invalid value for " + field;

        log.warn("event=TYPE_MISMATCH field={} message={}", field, message);

        return jsonResponse(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.failure(ErrorCode.VALIDATION_ERROR, message, field));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = resolveUnreadableMessage(ex);

        log.warn("event=HTTP_MESSAGE_UNREADABLE message={}", message);

        return jsonResponse(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.failure(ErrorCode.VALIDATION_ERROR, message, "requestBody"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("event=RESOURCE_NOT_FOUND path={} message={}", ex.getResourcePath(), ex.getMessage());

        return jsonResponse(HttpStatus.NOT_FOUND)
                .body(ResponseUtil.failure(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("event=METHOD_NOT_ALLOWED method={} supported={}", ex.getMethod(), ex.getSupportedHttpMethods());

        return jsonResponse(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResponseUtil.failure(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        String contentType = ex.getContentType() != null ? ex.getContentType().toString() : null;
        String message = "Invalid Content Type";
        String errorDescription = "Unsupported content type"
                + (contentType != null ? ": " + contentType : "")
                + ". Please use application/json";

        log.warn("event=UNSUPPORTED_MEDIA_TYPE contentType={}", contentType);

        return jsonResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ResponseUtil.failure(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message, errorDescription, "Content-Type"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("event=UNHANDLED_EXCEPTION", ex);

        return jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtil.failure(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity.BodyBuilder jsonResponse(HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON);
    }

    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }
        int dotIndex = propertyPath.lastIndexOf('.');
        return dotIndex >= 0 ? propertyPath.substring(dotIndex + 1) : propertyPath;
    }

    private String resolveUnreadableMessage(HttpMessageNotReadableException ex) {
        String exceptionMessage = ex.getMessage();
        if (exceptionMessage != null && exceptionMessage.contains("Required request body is missing")) {
            return "Request body is required";
        }
        return "Invalid request body";
    }
}
