package com.atlas.shared.kernel.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorDetails error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .message(message)
                        .code(code)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class ErrorDetails {
        private final String message;
        private final String code;
        private final Instant timestamp;
    }
}
