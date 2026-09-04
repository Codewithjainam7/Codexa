package com.codexa.ingestion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ZipAnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private byte[] createSampleZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/main/java/com/example/Sample.java");
            zos.putNextEntry(entry);
            zos.write("public class Sample {}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void submitZipAnalysisValidArchiveShouldReturn202Accepted() throws Exception {
        byte[] content = createSampleZip();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-project.zip",
                "application/zip",
                content
        );

        mockMvc.perform(multipart("/api/v1/analyses/zip").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sourceType").value("ZIP"))
                .andExpect(jsonPath("$.sourceIdentifier").value("sample-project.zip"))
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    void submitInvalidFileTypeShouldReturn400BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "not-a-zip".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/analyses/zip").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE_TYPE"));
    }

    @Test
    void submitEmptyFileShouldReturn400BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.zip",
                "application/zip",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/analyses/zip").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("EMPTY_FILE"));
    }
}
