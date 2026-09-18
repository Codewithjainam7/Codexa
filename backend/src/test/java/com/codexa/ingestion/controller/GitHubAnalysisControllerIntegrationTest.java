package com.codexa.ingestion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GitHubAnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitGitHubAnalysisValidUrlShouldReturn202AcceptedImmediately() throws Exception {
        String payload = """
                {
                    "repoUrl": "https://github.com/spring-projects/spring-petclinic"
                }
                """;

        mockMvc.perform(post("/api/v1/analyses/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sourceType").value("GITHUB"))
                .andExpect(jsonPath("$.sourceIdentifier").value("https://github.com/spring-projects/spring-petclinic"))
                .andExpect(jsonPath("$.status").value("EXTRACTING"));
    }

    @Test
    void submitInvalidGitHubUrlShouldReturn400BadRequest() throws Exception {
        String payload = """
                {
                    "repoUrl": "https://gitlab.com/insecure/repo"
                }
                """;

        mockMvc.perform(post("/api/v1/analyses/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void submitBlankUrlShouldReturn400BadRequest() throws Exception {
        String payload = """
                {
                    "repoUrl": ""
                }
                """;

        mockMvc.perform(post("/api/v1/analyses/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
