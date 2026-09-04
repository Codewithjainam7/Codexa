package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.MessageDigest;
import java.util.Random;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // VULNERABLE: CR-SEC-001 (Hardcoded Secret)
    private static final String JWT_SECRET = "super-secret-jwt-key-991283819";

    public byte[] register(String username, String password) throws Exception {
        // VULNERABLE: CR-LOG-001 (Sensitive Logging)
        log.info("Registering user: {} with raw password: {}", username, password);

        // VULNERABLE: CR-PASS-001 (Weak MD5 Password Storage)
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(password.getBytes());
    }

    public int generateToken() {
        // VULNERABLE: CR-CRYPTO-001 (Insecure PRNG for Token)
        Random tokenRandom = new Random();
        return tokenRandom.nextInt(999999);
    }
}
