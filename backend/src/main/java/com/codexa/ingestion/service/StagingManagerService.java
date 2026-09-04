package com.codexa.ingestion.service;

import com.codexa.config.CodexaProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class StagingManagerService {

    private static final Logger log = LoggerFactory.getLogger(StagingManagerService.class);

    private final Path stagingRoot;
    private final boolean cleanupOnCompletion;

    public StagingManagerService(CodexaProperties properties) {
        this.stagingRoot = Paths.get(properties.staging().baseDir()).toAbsolutePath().normalize();
        this.cleanupOnCompletion = properties.staging().cleanupOnCompletion();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(stagingRoot);
            log.info("Initialized analysis staging root directory: {}", stagingRoot);
        } catch (IOException e) {
            log.error("Failed to create staging root directory: {}", stagingRoot, e);
        }
    }

    public Path createStagingDirectory(UUID jobId) throws IOException {
        Path jobDir = stagingRoot.resolve(jobId.toString()).normalize();
        if (Files.exists(jobDir)) {
            cleanDirectory(jobDir);
        }
        Files.createDirectories(jobDir);
        return jobDir;
    }

    public void cleanDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        // Safety verification: must be within stagingRoot
        if (!directory.toAbsolutePath().normalize().startsWith(stagingRoot)) {
            log.warn("Attempted to delete directory outside staging root: {}", directory);
            return;
        }

        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Successfully cleaned staging directory: {}", directory);
        } catch (IOException e) {
            log.warn("Failed to clean staging directory {}: {}", directory, e.getMessage());
        }
    }

    public boolean isCleanupOnCompletion() {
        return cleanupOnCompletion;
    }

    /**
     * Scheduled cleanup running every hour to delete orphaned directories older than 2 hours.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void cleanupOrphanedStagingDirectories() {
        if (!Files.exists(stagingRoot)) return;

        Instant cutoff = Instant.now().minus(Duration.ofHours(2));
        log.debug("Running scheduled sweep of orphaned staging directories older than {}", cutoff);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(stagingRoot)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                    if (attrs.creationTime().toInstant().isBefore(cutoff)) {
                        log.info("Purging orphaned staging directory: {}", entry);
                        cleanDirectory(entry);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Error during scheduled staging sweep: {}", e.getMessage());
        }
    }
}
