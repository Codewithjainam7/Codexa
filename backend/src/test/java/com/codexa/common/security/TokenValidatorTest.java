package com.codexa.common.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TokenValidatorTest {

    @Test
    void shouldValidateEqualTokens() {
        assertThat(TokenValidator.constantTimeEquals("secret-token-123", "secret-token-123")).isTrue();
    }

    @Test
    void shouldRejectUnequalTokens() {
        assertThat(TokenValidator.constantTimeEquals("secret-token-123", "secret-token-wrong")).isFalse();
    }

    @Test
    void shouldHandleNullsSafely() {
        assertThat(TokenValidator.constantTimeEquals(null, "secret-token-123")).isFalse();
        assertThat(TokenValidator.constantTimeEquals("secret-token-123", null)).isFalse();
        assertThat(TokenValidator.constantTimeEquals(null, null)).isFalse();
    }
}
