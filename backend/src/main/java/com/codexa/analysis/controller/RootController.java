package com.codexa.analysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Root", description = "Root Service Discovery Endpoint")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Service Discovery", description = "Returns service information, health, and Swagger documentation links.")
    public ResponseEntity<Map<String, Object>> getRoot() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("service", "Codexa Security & Production Readiness API");
        info.put("version", "1.0.0");
        info.put("frontendUrl", "http://localhost:5173");
        info.put("swaggerDocs", "http://localhost:8080/swagger-ui.html");
        info.put("apiHealth", "http://localhost:8080/api/v1/health");
        info.put("apiLimits", "http://localhost:8080/api/v1/config/limits");
        return ResponseEntity.ok(info);
    }
}
