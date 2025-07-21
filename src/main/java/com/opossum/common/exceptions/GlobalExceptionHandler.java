package com.opossum.common.exceptions;

// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest()
            .header("Content-Type", "application/json")
            .body(body);
    }
}