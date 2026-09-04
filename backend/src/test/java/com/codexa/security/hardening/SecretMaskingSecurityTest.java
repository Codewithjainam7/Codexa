package com.codexa.security.hardening;

import com.codexa.ai.mask.SecretMasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecretMaskingSecurityTest {

    @Test
    void shouldMaskAwsAccessKeyId() {
        String snippet = "String awsKey = \"AKIAIOSFODNN7EXAMPLE\";";
        String masked = SecretMasker.maskSecrets(snippet);

        assertThat(masked).doesNotContain("AKIAIOSFODNN7EXAMPLE");
        assertThat(masked).contains("AKIAIOSFODNN7*******");
    }

    @Test
    void shouldMaskGitHubTokens() {
        String snippet = "String token = \"ghp_1234567890abcdef1234567890abcdef1234\";";
        String masked = SecretMasker.maskSecrets(snippet);

        assertThat(masked).doesNotContain("ghp_1234567890abcdef1234567890abcdef1234");
        assertThat(masked).contains("ghp_1234567890");
        assertThat(masked).contains("****");
    }

    @Test
    void shouldMaskOpenAiAndGenericApiKeys() {
        String snippet = "String apiKey = \"sk-proj-abc123456789012345678901234567890\";";
        String masked = SecretMasker.maskSecrets(snippet);

        assertThat(masked).doesNotContain("sk-proj-abc123456789012345678901234567890");
        assertThat(masked).contains("****");
    }

    @Test
    void shouldMaskJwtBearerTokens() {
        String snippet = "String auth = \"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\";";
        String masked = SecretMasker.maskSecrets(snippet);

        assertThat(masked).doesNotContain("eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
        assertThat(masked).contains("eyJhbGciOiJIUzI1");
        assertThat(masked).contains("****");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password = \"SuperSecretPassword123!\"",
            "db_pass = \"Admin@2024$\"",
            "client_secret = \"x8K9mLP2048fjsdlkfj2048\"",
            "private_key = \"-----BEGIN PRIVATE KEY-----\""
    })
    void shouldMaskPasswordAndSecretAssignments(String secretLine) {
        String masked = SecretMasker.maskSecrets(secretLine);
        assertThat(masked).isNotEqualTo(secretLine);
        assertThat(masked).contains("***");
    }
}
