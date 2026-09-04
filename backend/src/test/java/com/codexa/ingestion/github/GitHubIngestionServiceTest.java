package com.codexa.ingestion.github;

import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.service.FileFilterService;
import com.codexa.ingestion.zip.SecureZipExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubIngestionServiceTest {

    private GitHubIngestionService service;

    @BeforeEach
    void setUp() {
        CodexaProperties properties = new CodexaProperties(
                new CodexaProperties.Limits(25, 100, 1000, 15, 5),
                new CodexaProperties.Staging(".staging", true),
                new CodexaProperties.Ai(false, "none", "", "gemini-1.5-pro", 15000)
        );
        SecureZipExtractor extractor = new SecureZipExtractor(properties, new FileFilterService());
        service = new GitHubIngestionService(properties, extractor);
    }

    @Test
    void parseCoordinatesValidUrlsShouldExtractOwnerAndRepo() {
        var coords1 = service.parseCoordinates("https://github.com/spring-projects/spring-petclinic");
        assertEquals("spring-projects", coords1.owner());
        assertEquals("spring-petclinic", coords1.repo());

        var coords2 = service.parseCoordinates("https://github.com/owner/my-repo.git");
        assertEquals("owner", coords2.owner());
        assertEquals("my-repo", coords2.repo());
    }

    @Test
    void parseCoordinatesInvalidUrlsShouldThrowApiException() {
        assertThrows(ApiException.class, () -> service.parseCoordinates("http://github.com/insecure/repo"));
        assertThrows(ApiException.class, () -> service.parseCoordinates("https://gitlab.com/other/repo"));
        assertThrows(ApiException.class, () -> service.parseCoordinates("https://github.com/"));
        assertThrows(ApiException.class, () -> service.parseCoordinates("not-a-url"));
    }
}
