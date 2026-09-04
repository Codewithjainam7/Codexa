package com.codexa.ingestion.github;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GitHubAnalysisRequest(
        @NotBlank(message = "GitHub repository URL cannot be blank")
        @Pattern(
                regexp = "^https:\\/\\/github\\.com\\/[a-zA-Z0-9_.-]+\\/[a-zA-Z0-9_.-]+(?:\\.git)?$",
                message = "Must be a valid public GitHub HTTPS repository URL (e.g. https://github.com/owner/repo)"
        )
        String repoUrl
) {
}
