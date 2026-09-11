package com.codexa.analysis.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ScanThroughputTrackerTest {

    @Test
    void shouldCalculateThroughputCorrectly() {
        double lps = ScanThroughputTracker.calculateLinesPerSecond(100_000, 2_000);
        assertThat(lps).isEqualTo(50_000.0);

        double fps = ScanThroughputTracker.calculateFilesPerSecond(250, 500);
        assertThat(fps).isEqualTo(500.0);
    }

    @Test
    void shouldReturnZeroForZeroDuration() {
        assertThat(ScanThroughputTracker.calculateLinesPerSecond(1000, 0)).isEqualTo(0.0);
        assertThat(ScanThroughputTracker.calculateFilesPerSecond(10, 0)).isEqualTo(0.0);
    }
}
