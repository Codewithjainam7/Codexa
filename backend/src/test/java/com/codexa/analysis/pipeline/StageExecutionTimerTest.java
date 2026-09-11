package com.codexa.analysis.pipeline;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StageExecutionTimerTest {

    @Test
    void shouldRecordStageDurationsAccurately() throws InterruptedException {
        StageExecutionTimer timer = new StageExecutionTimer();
        timer.startStage("INGESTION");
        Thread.sleep(10);
        timer.startStage("AST_PARSING"); // automatically stops previous stage
        Thread.sleep(10);
        timer.stopStage();

        assertThat(timer.getStageDurations()).containsKey("INGESTION");
        assertThat(timer.getStageDurations()).containsKey("AST_PARSING");
        assertThat(timer.getTotalDurationMs()).isGreaterThanOrEqualTo(20);
    }
}
