package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CR-CRYPTO-002: Insecure Symmetric Cipher Mode (ECB) & Static Initialization Vectors.
 * Detects AES in ECB mode, hardcoded static IVs in IvParameterSpec, and short RSA key sizes (<2048-bit).
 */
@Component
public class CipherModeAndIvRule implements AnalysisRule {

    private static final Pattern ECB_PATTERN = Pattern.compile("(?i)[\"'](?:AES|DES|RC2|Blowfish)/ECB");
    private static final Pattern STATIC_IV_PATTERN = Pattern.compile("(?i)new\\s+IvParameterSpec\\s*\\(\\s*(?:new\\s+byte\\[|\"[^\"]+\"\\.getBytes|STATIC_IV)");
    private static final Pattern SHORT_RSA_PATTERN = Pattern.compile("(?i)initialize\\s*\\(\\s*(?:512|1024)\\s*\\)");

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-CRYPTO-002";
    }

    @Override
    public String getName() {
        return "Insecure Cipher Mode (ECB), Static IV & Weak Key Size";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.HIGH;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A02:2021 - Cryptographic Failures";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String fullSnippet = call.toString();

            if (ECB_PATTERN.matcher(fullSnippet).find()) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Insecure ECB Cipher Mode Detected")
                        .description("Electronic Codebook (ECB) mode encrypts identical plaintext blocks into identical ciphertext blocks, failing to provide semantic security and leaking data structures.")
                        .impact("Plaintext recovery, cryptographic pattern leakage, and replay attacks.")
                        .remediation("Use authenticated encryption such as AES-GCM (AES/GCM/NoPadding) with a fresh, cryptographically secure 12-byte initialization vector (IV) generated per encryption.")
                        .suggestedFix("// Secure AES-GCM encryption:\nCipher cipher = Cipher.getInstance(\"AES/GCM/NoPadding\");\nbyte[] iv = new byte[12];\nnew SecureRandom().nextBytes(iv);\nGCMParameterSpec spec = new GCMParameterSpec(128, iv);\ncipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of(
                                "https://cwe.mitre.org/data/definitions/327.html",
                                "https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html"
                        ))
                        .build());
            }

            if (STATIC_IV_PATTERN.matcher(fullSnippet).find()) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Static or Hardcoded Cryptographic Initialization Vector (IV)")
                        .description("Initialization vectors must be unpredictable and unique for every encryption operation. Reusing a static IV compromises confidentiality (e.g. key stream reuse).")
                        .impact("Loss of ciphertext indistinguishability and potential recovery of original plaintexts.")
                        .remediation("Generate a cryptographically random IV for each encryption using SecureRandom, and prepend it to the ciphertext.")
                        .suggestedFix("byte[] iv = new byte[16];\nnew SecureRandom().nextBytes(iv);\nIvParameterSpec ivSpec = new IvParameterSpec(iv);")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://cwe.mitre.org/data/definitions/329.html"))
                        .build());
            }

            if (SHORT_RSA_PATTERN.matcher(fullSnippet).find()) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Weak Asymmetric Key Size (< 2048-bit RSA)")
                        .description("RSA keys shorter than 2048 bits (e.g. 512 or 1024 bits) are practically breakable using modern cloud compute and number field sieve factorization.")
                        .impact("Private key compromise, decrypted communications, and forged digital signatures.")
                        .remediation("Generate RSA keys of at least 2048 bits (3072 bits recommended), or migrate to Elliptic Curve Cryptography (ECC, Ed25519 or NIST P-256).")
                        .suggestedFix("KeyPairGenerator kpg = KeyPairGenerator.getInstance(\"RSA\");\nkpg.initialize(3072);")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://cwe.mitre.org/data/definitions/326.html"))
                        .build());
            }
        });

        return findings;
    }
}
