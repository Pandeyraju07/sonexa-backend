package com.sonexa.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping({"/", "/api", "/api/v1", "/api/v1/"})
    public Map<String, Object> root() {
        return Map.of(
                "success", true,
                "message", "Sonexa AI Music API is running",
                "version", "1.0.0",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
