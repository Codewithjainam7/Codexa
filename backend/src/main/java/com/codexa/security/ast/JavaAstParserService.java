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
                .setStoreTokens(true);
        return new JavaParser(configuration);
    });

    public JavaAstParserService() {
    }

    public ParsedJavaFile parseFile(Path filePath, Path rootStagingDir) {
        String relativePath = rootStagingDir.relativize(filePath).toString().replace('\\', '/');

        try {
            String rawContent = Files.readString(filePath, StandardCharsets.UTF_8);
            return parseContent(rawContent, filePath, relativePath);
        } catch (IOException e) {
            log.warn("Failed to read Java source file {}: {}", relativePath, e.getMessage());
            return new ParsedJavaFile(filePath, relativePath, "", List.of(), null, false, List.of("Read failure: " + e.getMessage()));
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
        List<ParsedJavaFile> results = javaFiles.parallelStream()
                .map(file -> parseFile(file, rootStagingDir))
                .toList();

        log.info("AST Parser analyzed {} Java files ({} successfully generated ASTs)",
                results.size(), results.stream().filter(ParsedJavaFile::isParseSuccessful).count());
        return results;
    }
}
