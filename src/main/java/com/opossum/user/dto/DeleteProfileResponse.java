package com.opossum.user.dto;

import java.time.Instant;

public class DeleteProfileResponse {
    private boolean success;
    private ErrorResponse error;
    private Instant timestamp;
    private String message;

    public DeleteProfileResponse(boolean success, ErrorResponse error, Instant timestamp) {
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
    }
    public DeleteProfileResponse(boolean success, ErrorResponse error, Instant timestamp, String message) {
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
        this.message = message;
    }
    public boolean isSuccess() { return success; }
    public ErrorResponse getError() { return error; }
    public Instant getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
}
