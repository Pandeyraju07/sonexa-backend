package com.sonexa.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping({"/health", "/status", "/api/v1/health/live"})
    public Map<String, Object> liveness() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("check", "liveness");
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }

    @GetMapping("/api/v1/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("check", "readiness");
        body.put("timestamp", LocalDateTime.now().toString());
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            body.put("database", valid ? "UP" : "DOWN");
            body.put("status", valid ? "UP" : "DOWN");
            return ResponseEntity.status(valid ? 200 : 503).body(body);
        } catch (Exception e) {
            body.put("database", "DOWN");
            body.put("status", "DOWN");
            return ResponseEntity.status(503).body(body);
        }
    }
}
