package com.opossum.common.exceptions;

// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("error", java.util.Map.of(
                "code", String.valueOf(ex.getStatusCode()),
                "message", ex.getReason() != null ? ex.getReason() : "",
                "details", ex.getMessage() != null ? ex.getMessage() : ""
        ));
        body.put("timestamp", Instant.now());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("error", java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "",
                "details", ex.toString() != null ? ex.toString() : ""
        ));
        body.put("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        java.util.List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("error", java.util.Map.of(
                "code", "VALIDATION_ERROR",
                "message", "Erreur de validation des champs",
                "details", errors
        ));
        body.put("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("error", java.util.Map.of(
                "code", "INTERNAL_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "",
                "details", ex.toString() != null ? ex.toString() : ""
        ));
        body.put("timestamp", Instant.now());
        return ResponseEntity.internalServerError().body(body);
    }
}