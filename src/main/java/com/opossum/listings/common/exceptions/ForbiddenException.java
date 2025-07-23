package com.opossum.listings.common.exceptions;

public class ForbiddenException extends RuntimeException {
    private final transient java.util.Map<String, Object> errorBody;
    public ForbiddenException(java.util.Map<String, Object> errorBody) {
        super("Forbidden");
        this.errorBody = errorBody;
    }
    public java.util.Map<String, Object> getErrorBody() {
        return errorBody;
    }
}
