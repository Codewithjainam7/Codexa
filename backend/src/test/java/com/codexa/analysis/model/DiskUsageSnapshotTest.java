package com.codexa.analysis.model;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;

class DiskUsageSnapshotTest {

    @Test
    void shouldCaptureDiskUsageForCurrentDir() {
        File cur = new File(".");
        DiskUsageSnapshot snapshot = DiskUsageSnapshot.forDirectory(cur);
        assertThat(snapshot.totalSpaceBytes()).isGreaterThan(0L);
        assertThat(snapshot.getUsablePercentage()).isGreaterThan(0.0);
    }

    @Test
    void shouldHandleNullDirectorySafely() {
        DiskUsageSnapshot snapshot = DiskUsageSnapshot.forDirectory(null);
        assertThat(snapshot.totalSpaceBytes()).isEqualTo(0L);
        assertThat(snapshot.getUsablePercentage()).isEqualTo(0.0);
    }
}
