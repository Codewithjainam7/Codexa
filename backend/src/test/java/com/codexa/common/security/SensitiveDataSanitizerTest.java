package com.codexa.common.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataSanitizerTest {

    @Test
    void shouldMaskBearerToken() {
        String log = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String masked = SensitiveDataSanitizer.maskBearerToken(log);
        assertThat(masked).isEqualTo("Authorization: Bearer [REDACTED]");
    }

    @Test
    void shouldMaskApiKeyAssignment() {
        String log = "Connecting with api_key=ak_live_999888777666 and user=admin";
        String masked = SensitiveDataSanitizer.maskCredentials(log);
        assertThat(masked).contains("api_key=***REDACTED***");
        assertThat(masked).contains("user=admin");
    }

    @Test
    void shouldHandleNullInputGracefully() {
        assertThat(SensitiveDataSanitizer.maskAll(null)).isNull();
    }
}
