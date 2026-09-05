package com.codexa.analysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@Tag(name = "Root", description = "Root Service Discovery Endpoint")
public class RootController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/api")
    @ResponseBody
    @Operation(summary = "Service Discovery", description = "Returns service information, health, and Swagger documentation links.")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("service", "Codexa Security & Production Readiness API");
        info.put("version", "1.0.0");
        info.put("swaggerDocs", "/swagger-ui.html");
        info.put("apiHealth", "/api/v1/health");
        info.put("apiLimits", "/api/v1/config/limits");
        return ResponseEntity.ok(info);
    }
}
