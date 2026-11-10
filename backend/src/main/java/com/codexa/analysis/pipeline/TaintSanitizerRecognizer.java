package com.codexa.analysis.pipeline;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Recognizes data sanitization methods, escaping functions, type-casting guards,
 * and boundary validation predicates to neutralize false positive taint paths.
 */
@Service
public class TaintSanitizerRecognizer {

    private static final Set<String> KNOWN_SANITIZER_METHODS = Set.of(
            "escapeHtml4",
            "escapeXml11",
            "escapeSql",
            "encodeForHTML",
            "encodeForJavaScript",
            "encodeForSQL",
            "encode",
            "sanitize",
            "clean",
            "stripTags",
            "parseInt",
            "parseLong",
            "parseDouble",
            "parseFloat",
            "valueOf",
            "matches"
    );

    private static final Set<String> KNOWN_SANITIZER_CLASSES = Set.of(
            "StringEscapeUtils",
            "ESAPI",
            "Encoder",
            "HtmlUtils",
            "URLEncoder",
            "Pattern",
            "Integer",
            "Long",
            "Double",
            "Float",
            "UUID",
            "Jsoup"
    );

    private static final Pattern VALIDATION_METHOD_PATTERN = Pattern.compile(
            "(?i).*(?:validate|sanitize|escape|isAlphanumeric|cleanse|filter).*"
    );

    /**
     * Determines whether a method call expression represents a valid sanitizer or escaping function.
     *
     * @param className  The target class/scope or variable calling the method
     * @param methodName The invoked method name
     * @return true if the call neutralizes the taint
     */
    public boolean isSanitizer(String className, String methodName) {
        if (methodName == null) {
            return false;
        }

        if (KNOWN_SANITIZER_METHODS.contains(methodName)) {
            return true;
        }

        if (className != null && KNOWN_SANITIZER_CLASSES.contains(className)) {
            return true;
        }

        return VALIDATION_METHOD_PATTERN.matcher(methodName).matches();
    }

    /**
     * Checks if a variable has been sanitized by an assignment expression.
     */
    public boolean isExpressionSanitized(String expressionText) {
        if (expressionText == null || expressionText.isBlank()) {
            return false;
        }

        for (String sanitizer : KNOWN_SANITIZER_METHODS) {
            if (expressionText.contains(sanitizer + "(")) {
                return true;
            }
        }

        for (String clazz : KNOWN_SANITIZER_CLASSES) {
            if (expressionText.contains(clazz + ".")) {
                return true;
            }
        }

        return false;
    }
}
