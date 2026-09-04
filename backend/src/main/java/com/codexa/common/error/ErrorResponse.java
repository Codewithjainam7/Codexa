package com.codexa.common.error;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String correlationId
) {
    public static ErrorResponse of(int status, String errorCode, String message, String correlationId) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, correlationId);
    }
}
