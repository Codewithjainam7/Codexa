package com.codexa.security.filter;

import com.codexa.config.CodexaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

/**
 * Optional API key gate protecting analysis submission and detail endpoints.
 * Operates in pass-through mode when codexa.security.api-key is blank or unset.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class ApiKeyAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final CodexaProperties properties;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(CodexaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI();
        String expectedKey = properties.security() != null ? properties.security().apiKey() : null;

        // Only enforce when api-key is configured and request targets analysis endpoints
        if (expectedKey != null && !expectedKey.isBlank() && path.startsWith("/api/v1/analyses")) {
            // Allow pre-flight CORS OPTIONS requests without API key
            if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

            String providedKey = httpRequest.getHeader("X-Api-Key");

            boolean isValid = providedKey != null && MessageDigest.isEqual(
                    providedKey.getBytes(StandardCharsets.UTF_8),
                    expectedKey.getBytes(StandardCharsets.UTF_8)
            );

            if (!isValid) {
                log.warn("Unauthorized API key access attempt on endpoint: {} from remote: {}", path, httpRequest.getRemoteAddr());
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                Map<String, Object> errorBody = Map.of(
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "UNAUTHORIZED",
                        "message", "Invalid or missing API key. Provide a valid key in the X-Api-Key header.",
                        "timestamp", Instant.now().toString()
                );
                objectMapper.writeValue(httpResponse.getOutputStream(), errorBody);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
