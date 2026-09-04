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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding window rate limiting filter targeting analysis creation endpoints.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final CodexaProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, RequestCounter> clientRequestBuckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(CodexaProperties properties, ObjectMapper objectMapper) {
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
        String method = httpRequest.getMethod();

        // Apply rate limits exclusively to resource-intensive analysis submission endpoints
        boolean isAnalysisSubmission = "POST".equalsIgnoreCase(method) && 
                (path.startsWith("/api/v1/analyses/zip") || path.startsWith("/api/v1/analyses/github"));

        boolean isEnabled = properties.security() == null || properties.security().rateLimitEnabled();
        int maxRequests = properties.security() != null ? properties.security().rateLimitRequestsPerMinute() : 60;

        if (isEnabled && isAnalysisSubmission) {
            String clientIp = extractClientIp(httpRequest);
            long currentMinute = System.currentTimeMillis() / 60000;

            RequestCounter counter = clientRequestBuckets.compute(clientIp, (key, existing) -> {
                if (existing == null || existing.minuteBucket != currentMinute) {
                    return new RequestCounter(currentMinute, new AtomicInteger(1));
                }
                existing.count.incrementAndGet();
                return existing;
            });

            if (counter.count.get() > maxRequests) {
                log.warn("Rate limit exceeded for IP: {} on endpoint: {} (Count: {})", clientIp, path, counter.count.get());
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.setHeader("Retry-After", "60");

                Map<String, Object> errorBody = Map.of(
                        "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                        "error", "TOO_MANY_REQUESTS",
                        "message", "Rate limit exceeded (" + maxRequests + " requests/min). Please wait before submitting more codebase analyses.",
                        "timestamp", Instant.now().toString()
                );
                objectMapper.writeValue(httpResponse.getOutputStream(), errorBody);
                return;
            }

            // Periodically sweep buckets (when size exceeds 500 entries)
            if (clientRequestBuckets.size() > 500) {
                clientRequestBuckets.entrySet().removeIf(entry -> entry.getValue().minuteBucket < currentMinute - 1);
            }
        }

        chain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown-client";
    }

    private static class RequestCounter {
        final long minuteBucket;
        final AtomicInteger count;

        RequestCounter(long minuteBucket, AtomicInteger count) {
            this.minuteBucket = minuteBucket;
            this.count = count;
        }
    }
}
