package com.atlas.shared.kernel.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus statusCode;
    private final Instant timestamp;

    protected BaseException(String message, String errorCode, HttpStatus statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.timestamp = Instant.now();
    }
}
