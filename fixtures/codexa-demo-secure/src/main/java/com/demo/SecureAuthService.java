package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class SecureAuthService {

    private static final Logger log = LoggerFactory.getLogger(SecureAuthService.class);

    // SECURE: Externalized configuration value
    @Value("${JWT_SECRET:default_dev_key}")
    private String jwtSecret;

    public void register(String username, String rawPassword) {
        // SECURE: Structured logging without sensitive password argument
        log.info("User registered successfully with username: {}", username);
    }

    public int generateSecureToken() {
        // SECURE: Cryptographically secure pseudo-random generator
        SecureRandom random = new SecureRandom();
        return random.nextInt(999999);
    }
}
