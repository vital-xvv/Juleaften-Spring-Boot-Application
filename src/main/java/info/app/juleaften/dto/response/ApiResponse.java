package info.app.juleaften.dto.response;

import info.app.juleaften.exception.ExceptionCode;

import java.time.Instant;

public record ApiResponse<T>(String message, T payload, Instant timestamp, ExceptionCode exceptionCode) {
    public ApiResponse(String message, T payload) {
        this(message, payload, Instant.now(), null);
    }

    public ApiResponse(String message) {
        this(message, null, Instant.now(), null);
    }

    public ApiResponse(ExceptionCode exceptionCode) {
        this(null, null, Instant.now(), exceptionCode);
    }
}
