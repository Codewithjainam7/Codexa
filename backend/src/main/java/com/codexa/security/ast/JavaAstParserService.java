package com.codexa.security.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JavaAstParserService {

    private static final Logger log = LoggerFactory.getLogger(JavaAstParserService.class);

    private final ThreadLocal<JavaParser> threadLocalParser = ThreadLocal.withInitial(() -> {
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setAttributeComments(true)
                .setStoreTokens(false); // Memory optimization: avoid retaining millions of lexer tokens
        return new JavaParser(configuration);
    });

    public JavaAstParserService() {
    }

    public ParsedJavaFile parseFile(Path filePath, Path rootStagingDir) {
        String relativePath = rootStagingDir.relativize(filePath).toString().replace('\\', '/');

        try {
            String rawContent = readSourceFileString(filePath);
            return parseContent(rawContent, filePath, relativePath);
        } catch (Exception e) {
            log.warn("Failed to read Java source file {}: {}", relativePath, e.getMessage());
            return new ParsedJavaFile(filePath, relativePath, "", List.of(), null, false, List.of("Read failure: " + e.getMessage()));
        }
    }

    private String readSourceFileString(Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        // 1. Detect UTF-16LE BOM
        if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        // 2. Detect UTF-16BE BOM
        if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        // 3. Detect UTF-8 BOM
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        // 4. Detect UTF-16LE without BOM (ASCII characters with 0x00 at odd indices)
        if (bytes.length >= 4 && bytes[1] == 0 && bytes[3] == 0 && bytes[0] != 0 && bytes[2] != 0) {
            return new String(bytes, StandardCharsets.UTF_16LE);
        }
        // 5. Default UTF-8 with ISO-8859-1 fallback
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
    }

    public ParsedJavaFile parseContent(String content, Path filePath, String relativePath) {
        List<String> lines = Arrays.asList(content.split("\\r?\\n", -1));
        List<String> parseErrors = new ArrayList<>();

        try {
            ParseResult<CompilationUnit> parseResult = threadLocalParser.get().parse(content);

            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();
                return new ParsedJavaFile(filePath, relativePath, content, lines, cu, true, List.of());
            } else {
                parseResult.getProblems().forEach(p -> parseErrors.add(p.getMessage()));
                log.debug("Partial or failed AST parse for {}: {} problems", relativePath, parseErrors.size());
                CompilationUnit fallbackCu = parseResult.getResult().orElse(null);
                return new ParsedJavaFile(filePath, relativePath, content, lines, fallbackCu, fallbackCu != null, parseErrors);
            }
        } catch (ParseProblemException e) {
            log.debug("JavaParser parse problem in {}: {}", relativePath, e.getMessage());
            return new ParsedJavaFile(filePath, relativePath, content, lines, null, false, List.of(e.getMessage()));
        } catch (Exception e) {
            log.warn("Unexpected AST parser exception for {}: {}", relativePath, e.getMessage());
            return new ParsedJavaFile(filePath, relativePath, content, lines, null, false, List.of(e.getMessage()));
        }
    }

    public List<ParsedJavaFile> parseAll(List<Path> javaFiles, Path rootStagingDir) {
        if (javaFiles == null || javaFiles.isEmpty()) {
            return List.of();
        }

        // Bound concurrency to prevent thread storms and memory spikes on constrained containers
        int maxThreads = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors()));
        List<ParsedJavaFile> results;

        if (maxThreads <= 1 || javaFiles.size() < 10) {
            results = javaFiles.stream()
                    .map(file -> parseFile(file, rootStagingDir))
                    .toList();
        } else {
            java.util.concurrent.ForkJoinPool customPool = new java.util.concurrent.ForkJoinPool(maxThreads);
            try {
                results = customPool.submit(() ->
                        javaFiles.parallelStream()
                                .map(file -> parseFile(file, rootStagingDir))
                                .toList()
                ).get();
            } catch (Exception e) {
                log.warn("Parallel AST parsing pool interrupted, falling back to sequential stream: {}", e.getMessage());
                results = javaFiles.stream()
                        .map(file -> parseFile(file, rootStagingDir))
                        .toList();
            } finally {
                customPool.shutdown();
            }
        }

        log.info("AST Parser analyzed {} Java files ({} successfully generated ASTs)",
                results.size(), results.stream().filter(ParsedJavaFile::isParseSuccessful).count());
        return results;
    }

    @jakarta.annotation.PreDestroy
    public void cleanup() {
        try {
            threadLocalParser.remove();
        } catch (Exception e) {
            log.warn("Failed to clean up thread local parser: {}", e.getMessage());
        }
    }
}
