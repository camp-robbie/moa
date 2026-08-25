package com.sparta.moa.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(status.value(), null, data, LocalDateTime.now());
    }

    public static ApiResponse<Void> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null, LocalDateTime.now());
    }
}