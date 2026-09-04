package com.codexa.security.ast;

import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ParsedJavaFile {

    private final Path absolutePath;
    private final String relativePath;
    private final String rawContent;
    private final List<String> lines;
    private final CompilationUnit compilationUnit;
    private final boolean parseSuccessful;
    private final List<String> parseErrors;

    public ParsedJavaFile(
            Path absolutePath,
            String relativePath,
            String rawContent,
            List<String> lines,
            CompilationUnit compilationUnit,
            boolean parseSuccessful,
            List<String> parseErrors
    ) {
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.rawContent = rawContent;
        this.lines = lines;
        this.compilationUnit = compilationUnit;
        this.parseSuccessful = parseSuccessful;
        this.parseErrors = parseErrors != null ? parseErrors : List.of();
    }

    public Path getAbsolutePath() {
        return absolutePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getRawContent() {
        return rawContent;
    }

    public List<String> getLines() {
        return lines;
    }

    public Optional<CompilationUnit> getCompilationUnit() {
        return Optional.ofNullable(compilationUnit);
    }

    public boolean isParseSuccessful() {
        return parseSuccessful;
    }

    public List<String> getParseErrors() {
        return parseErrors;
    }

    public String getSnippet(int startLine, int endLine) {
        if (lines == null || lines.isEmpty() || startLine <= 0) {
            return "";
        }
        int from = Math.max(0, startLine - 1);
        int to = Math.min(lines.size(), Math.max(startLine, endLine));
        if (from >= to) {
            return "";
        }
        return String.join(System.lineSeparator(), lines.subList(from, to));
    }
}
