package com.codexa.analysis.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigLimitsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void limitsEndpointShouldReturnConfiguredLimits() throws Exception {
        mockMvc.perform(get("/api/v1/config/limits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxCompressedSizeMb").value(25))
                .andExpect(jsonPath("$.maxExtractedSizeMb").value(100))
                .andExpect(jsonPath("$.maxFileCount").value(1000))
                .andExpect(jsonPath("$.maxPathDepth").value(15))
                .andExpect(jsonPath("$.maxSingleFileSizeMb").value(5));
    }
}
