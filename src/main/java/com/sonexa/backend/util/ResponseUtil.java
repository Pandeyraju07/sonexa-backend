package com.sonexa.backend.util;

import com.sonexa.backend.common.ApiResponse;
import com.sonexa.backend.common.ErrorResponse;
import com.sonexa.backend.constant.ErrorCode;

import java.time.LocalDateTime;

public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .error(null)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(errorCode, errorCode.getDescription(), null);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String errDesc, String field) {
        return failure(errorCode, null, errDesc, field);
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String headerMessage, String errDesc, String field) {
        String resolvedErrDesc = errDesc != null ? errDesc : errorCode.getDescription();
        String resolvedHeaderMessage = headerMessage != null ? headerMessage : resolvedErrDesc;

        return ApiResponse.<T>builder()
                .success(false)
                .message(resolvedHeaderMessage)
                .data(null)
                .timestamp(LocalDateTime.now())
                .error(
                        ErrorResponse.builder()
                                .errCode(errorCode.getCode())
                                .errDesc(resolvedErrDesc)
                                .field(field)
                                .build()
                )
                .build();
    }
}
