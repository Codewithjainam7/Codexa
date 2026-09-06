package com.codexa.security.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter that applies OWASP-recommended security response headers to all incoming HTTP requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    public static final String CSP_POLICY = "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https://raw.githubusercontent.com https://api.dicebear.com; " +
            "connect-src 'self' http://localhost:* https://integrate.api.nvidia.com https://api.github.com;";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpServletResponse) {
            httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpServletResponse.setHeader("X-Frame-Options", "DENY");
            httpServletResponse.setHeader("X-XSS-Protection", "0");
            httpServletResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpServletResponse.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
            httpServletResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            httpServletResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            httpServletResponse.setHeader("Content-Security-Policy", CSP_POLICY);
        }
        chain.doFilter(request, response);
    }
}
