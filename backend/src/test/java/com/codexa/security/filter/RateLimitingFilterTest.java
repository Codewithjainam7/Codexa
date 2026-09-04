package com.codexa.security.filter;

import com.codexa.config.CodexaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAllowRequestsUnderRateLimit() throws Exception {
        CodexaProperties props = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 1000, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "model", 10000),
                new CodexaProperties.Security(true, 5)
        );
        RateLimitingFilter filter = new RateLimitingFilter(props, objectMapper);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/analyses/zip");
            request.setRemoteAddr("192.168.1.100");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Test
    void shouldBlockRequestsExceedingRateLimitWith429() throws Exception {
        CodexaProperties props = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 1000, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "model", 10000),
                new CodexaProperties.Security(true, 2)
        );
        RateLimitingFilter filter = new RateLimitingFilter(props, objectMapper);

        // First 2 succeed
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/analyses/zip");
            req.setRemoteAddr("10.0.0.5");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 3rd attempt exceeds limit
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/v1/analyses/zip");
        blockedReq.setRemoteAddr("10.0.0.5");
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilter(blockedReq, blockedRes, new MockFilterChain());

        assertThat(blockedRes.getStatus()).isEqualTo(429);
        assertThat(blockedRes.getHeader("Retry-After")).isEqualTo("60");
        assertThat(blockedRes.getContentAsString()).contains("TOO_MANY_REQUESTS");
    }

    @Test
    void shouldNotRateLimitGetRequests() throws Exception {
        CodexaProperties props = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 1000, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "model", 10000),
                new CodexaProperties.Security(true, 1)
        );
        RateLimitingFilter filter = new RateLimitingFilter(props, objectMapper);

        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/analyses/some-job");
            req.setRemoteAddr("10.0.0.5");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }
    }
}
