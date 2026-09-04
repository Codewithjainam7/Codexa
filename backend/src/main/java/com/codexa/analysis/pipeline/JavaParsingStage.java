package com.codexa.analysis.pipeline;

import com.codexa.ingestion.service.FileFilterService;
import com.codexa.security.ast.JavaAstParserService;
import com.codexa.security.ast.ParsedJavaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(10)
public class JavaParsingStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(JavaParsingStage.class);

    private final JavaAstParserService astParserService;
    private final FileFilterService fileFilterService;

    public JavaParsingStage(JavaAstParserService astParserService, FileFilterService fileFilterService) {
        this.astParserService = astParserService;
        this.fileFilterService = fileFilterService;
    }

    @Override
    public String getStageName() {
        return "JAVA_AST_PARSING";
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("Executing Java AST Parsing stage for jobId={}", context.getJobId());

        List<Path> javaSourceFiles = context.getSourceFiles().stream()
                .filter(p -> fileFilterService.isJavaSourceFile(p.getFileName().toString()))
                .collect(Collectors.toList());

        List<ParsedJavaFile> parsedFiles = astParserService.parseAll(javaSourceFiles, context.getStagingDirectory());
        context.setParsedJavaFiles(parsedFiles);
        int totalSource = context.getSourceFiles() != null ? context.getSourceFiles().size() : parsedFiles.size();
        context.setAnalyzedFiles(totalSource);

        log.info("Job {} parsed {} Java source files (total {} source files) for analysis.", context.getJobId(), parsedFiles.size(), totalSource);
    }
}
