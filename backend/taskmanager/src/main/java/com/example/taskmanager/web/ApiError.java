package com.example.taskmanager.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Standard error response model used in GlobalExceptionHandler.
 * Appears in OpenAPI documentation for 4xx / 5xx responses.
 */
@Schema(description = "Standard error response for failed API calls")
public record ApiError(
        @Schema(example = "404", description = "HTTP status code") int status,
        @Schema(example = "NOT_FOUND", description = "Status name") String error,
        @Schema(example = "Project not found", description = "Error message") String message,
        @Schema(example = "/projects/99", description = "Request path") String path,
        @Schema(example = "2025-11-12T19:45:00Z", description = "Time of error (UTC)") Instant timestamp,
        @Schema(description = "Optional validation or field-level error details") Map<String, Object> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, Instant.now(), null);
    }

    public static ApiError of(int status, String error, String message, String path, Map<String,Object> details) {
        return new ApiError(status, error, message, path, Instant.now(), details);
    }
}
