package com.codexa.analysis.service;

/**
 * Sanitizes CSV field values to prevent CSV Formula Injection (CWE-1236) in spreadsheet tools.
 */
public final class CsvFormulaSanitizer {

    private CsvFormulaSanitizer() {}

    private static final String DANGEROUS_CHARS = "=+-@\t\r";

    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        char firstChar = input.charAt(0);
        if (DANGEROUS_CHARS.indexOf(firstChar) >= 0) {
            return "'" + input;
        }
        return input;
    }
}
