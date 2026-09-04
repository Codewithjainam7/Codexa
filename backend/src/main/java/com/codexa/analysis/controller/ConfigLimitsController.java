package com.codexa.analysis.controller;

import com.codexa.analysis.model.LimitsResponse;
import com.codexa.config.CodexaProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config/limits")
@Tag(name = "Configuration", description = "Public upload and analysis limits")
public class ConfigLimitsController {

    private final CodexaProperties properties;

    public ConfigLimitsController(CodexaProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    @Operation(summary = "Get upload and scan limits", description = "Returns system limits enforced for ZIP uploads and repository scans.")
    public ResponseEntity<LimitsResponse> getLimits() {
        CodexaProperties.Limits limits = properties.limits();
        return ResponseEntity.ok(new LimitsResponse(
                limits.maxCompressedSizeMb(),
                limits.maxExtractedSizeMb(),
                limits.maxFileCount(),
                limits.maxPathDepth(),
                limits.maxSingleFileSizeMb()
        ));
    }
}
