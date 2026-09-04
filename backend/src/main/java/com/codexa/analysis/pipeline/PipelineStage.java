package com.codexa.analysis.pipeline;

public interface PipelineStage {

    String getStageName();

    void execute(PipelineContext context);
}
