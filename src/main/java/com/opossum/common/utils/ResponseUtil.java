package com.opossum.common.utils;

import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.Map;

public class ResponseUtil {
    public static ResponseEntity<Map<String, Object>> success(Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", data,
            "timestamp", Instant.now()
        ));
    }

    public static ResponseEntity<Map<String, Object>> successWithCookie(Object data, String cookieHeader) {
        return ResponseEntity.ok()
            .header("Set-Cookie", cookieHeader)
            .body(Map.of(
                "success", true,
                "data", data,
                "timestamp", Instant.now()
            ));
    }

    public static ResponseEntity<Map<String, Object>> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
            "success", false,
            "error", Map.of(
                "code", code,
                "message", message
            ),
            "timestamp", Instant.now()
        ));
    }

    public static ResponseEntity<Map<String, Object>> errorWithCookie(int status, String code, String message, String cookieHeader) {
        return ResponseEntity.status(status)
            .header("Set-Cookie", cookieHeader)
            .body(Map.of(
                "success", false,
                "error", Map.of(
                    "code", code,
                    "message", message
                ),
                "timestamp", Instant.now()
            ));
    }
}
