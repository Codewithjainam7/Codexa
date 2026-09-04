package com.codexa.security.hardening;

import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.github.GitHubIngestionService;
import com.codexa.ingestion.zip.SecureZipExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SsrfProtectionTest {

    private GitHubIngestionService gitHubIngestionService;

    @BeforeEach
    void setUp() {
        CodexaProperties properties = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 1000, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "model", 10000),
                new CodexaProperties.Security(true, 60)
        );
        gitHubIngestionService = new GitHubIngestionService(
                properties,
                new SecureZipExtractor(properties, new com.codexa.ingestion.service.FileFilterService())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://github.com/user/repo",
            "http://127.0.0.1/repo.git",
            "http://localhost:8080/exploit",
            "https://169.254.169.254/latest/meta-data/",
            "https://192.168.1.1/internal-repo",
            "https://10.0.0.1/org/repo",
            "ftp://github.com/user/repo",
            "file:///etc/passwd",
            "https://gitlab.com/user/repo",
            "https://bitbucket.org/user/repo",
            "https://evil-github.com/user/repo",
            "https://github.com/user/repo/../../../etc/passwd"
    })
    void shouldRejectInvalidOrSsrfSusceptibleUrls(String invalidUrl) {
        assertThat(gitHubIngestionService.isValidGitHubUrl(invalidUrl)).isFalse();

        assertThatThrownBy(() -> gitHubIngestionService.validateUrl(invalidUrl))
                .isInstanceOf(ApiException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/spring-projects/spring-boot",
            "https://github.com/Codewithjainam7/Codexa",
            "https://github.com/google/guava.git",
            "https://github.com/apache/commons-lang/"
    })
    void shouldAcceptLegitimateGitHubRepositoryUrls(String validUrl) {
        assertThat(gitHubIngestionService.isValidGitHubUrl(validUrl)).isTrue();
    }
}
