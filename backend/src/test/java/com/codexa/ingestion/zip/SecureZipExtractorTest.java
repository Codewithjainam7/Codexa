package com.codexa.ingestion.zip;

import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.service.FileFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class SecureZipExtractorTest {

    private SecureZipExtractor extractor;
    private FileFilterService fileFilterService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileFilterService = new FileFilterService();
        CodexaProperties properties = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 10, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "gemini-1.5-pro", 15000)
        );
        extractor = new SecureZipExtractor(properties, fileFilterService);
    }

    private byte[] createTestZip(String... entryNamesWithContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < entryNamesWithContent.length; i += 2) {
                String name = entryNamesWithContent[i];
                String content = entryNamesWithContent[i + 1];
                ZipEntry entry = new ZipEntry(name);
                zos.putNextEntry(entry);
                zos.write(content.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    @Test
    void extractValidZipShouldExtractSourceFilesOnly() throws IOException {
        byte[] zipBytes = createTestZip(
                "src/main/java/com/example/App.java", "public class App {}",
                "pom.xml", "<project></project>",
                "target/App.class", "bytecode",
                "image.png", "pngdata",
                "node_modules/package.json", "{}"
        );

        Path stagingDir = tempDir.resolve("staging-valid");
        Files.createDirectories(stagingDir);

        ExtractionResult result = extractor.extract(new ByteArrayInputStream(zipBytes), stagingDir);

        assertEquals(2, result.extractedSourceFiles().size());
        assertTrue(Files.exists(stagingDir.resolve("src/main/java/com/example/App.java")));
        assertTrue(Files.exists(stagingDir.resolve("pom.xml")));
        // Ignored files should not be written to disk
        assertFalse(Files.exists(stagingDir.resolve("target/App.class")));
        assertFalse(Files.exists(stagingDir.resolve("image.png")));
        assertFalse(Files.exists(stagingDir.resolve("node_modules/package.json")));
    }

    @Test
    void extractZipSlipShouldThrowApiException() throws IOException {
        byte[] zipBytes = createTestZip(
                "../../outside.txt", "malicious payload"
        );

        Path stagingDir = tempDir.resolve("staging-zipslip");
        Files.createDirectories(stagingDir);

        ApiException ex = assertThrows(ApiException.class, () ->
                extractor.extract(new ByteArrayInputStream(zipBytes), stagingDir)
        );

        assertEquals("ZIP_SLIP_DETECTED", ex.getErrorCode());
    }

    @Test
    void extractExceedingMaxDepthShouldThrowApiException() throws IOException {
        String deepPath = "a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/DeepFile.java";
        byte[] zipBytes = createTestZip(deepPath, "public class DeepFile {}");

        Path stagingDir = tempDir.resolve("staging-deep");
        Files.createDirectories(stagingDir);

        ApiException ex = assertThrows(ApiException.class, () ->
                extractor.extract(new ByteArrayInputStream(zipBytes), stagingDir)
        );

        assertEquals("ZIP_DEPTH_EXCEEDED", ex.getErrorCode());
    }

    @Test
    void extractExceedingMaxFileCountShouldThrowApiException() throws IOException {
        // Limit is set to 10 in test setup
        String[] entries = new String[24];
        for (int i = 0; i < 12; i++) {
            entries[i * 2] = "File" + i + ".java";
            entries[i * 2 + 1] = "public class File" + i + " {}";
        }

        byte[] zipBytes = createTestZip(entries);
        Path stagingDir = tempDir.resolve("staging-bomb");
        Files.createDirectories(stagingDir);

        ApiException ex = assertThrows(ApiException.class, () ->
                extractor.extract(new ByteArrayInputStream(zipBytes), stagingDir)
        );

        assertEquals("ZIP_BOMB_FILE_COUNT_EXCEEDED", ex.getErrorCode());
    }
}
