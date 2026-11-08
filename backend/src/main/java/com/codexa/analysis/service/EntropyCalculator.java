package com.codexa.analysis.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Shannon Entropy Calculation Engine.
 * Evaluates informational randomness of token strings to detect cryptographic keys and passwords.
 */
@Service
public class EntropyCalculator {

    /**
     * Calculates Shannon entropy in bits per character.
     * Maximum entropy for standard ASCII (95 printable characters) is ~6.57 bits/char.
     * Base64 encoded keys typically exhibit H > 4.5 bits/char.
     * Hexadecimal tokens typically exhibit H > 3.0 bits/char.
     */
    public double calculateShannonEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }

        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int len = input.length();

        for (int count : frequencyMap.values()) {
            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2));
        }

        return Math.round(entropy * 1000.0) / 1000.0;
    }

    public boolean isHighEntropySecret(String token) {
        if (token == null || token.length() < 16) {
            return false;
        }

        double entropy = calculateShannonEntropy(token);

        // Hexadecimal pattern
        if (token.matches("^[0-9a-fA-F]+$")) {
            return entropy >= 3.2 && token.length() >= 24;
        }

        // Base64 or mixed alphanumeric
        return entropy >= 4.5 && token.length() >= 20;
    }
}
