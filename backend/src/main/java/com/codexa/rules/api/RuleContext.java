package com.codexa.rules.api;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.security.ast.ParsedJavaFile;

import java.nio.file.Path;
import java.util.List;

public class RuleContext {

    private final ParsedJavaFile parsedJavaFile;
    private final PipelineContext pipelineContext;

    public RuleContext(ParsedJavaFile parsedJavaFile, PipelineContext pipelineContext) {
        this.parsedJavaFile = parsedJavaFile;
        this.pipelineContext = pipelineContext;
    }

    public ParsedJavaFile getParsedJavaFile() {
        return parsedJavaFile;
    }

    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    public Path getStagingDirectory() {
        return pipelineContext.getStagingDirectory();
    }

    public List<Path> getAllSourceFiles() {
        return pipelineContext.getSourceFiles();
    }

    public List<ParsedJavaFile> getAllParsedJavaFiles() {
        return pipelineContext.getParsedJavaFiles();
    }
}
