package info.app.juleaften.dto.response;

import java.time.Instant;

public record ApiResponse<T>(String message, T payload, Instant timestamp) {
    public ApiResponse(String message, T payload) {
        this(message, payload, Instant.now());
    }

    public ApiResponse(String message) {
        this(message, null, Instant.now());
    }
}
