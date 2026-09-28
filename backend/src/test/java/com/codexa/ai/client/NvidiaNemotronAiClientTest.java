package com.codexa.ai.client;

import com.codexa.config.CodexaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NvidiaNemotronAiClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Local Ollama mode should be active and configured without external API key")
    void testLocalOllamaConfigurationWithoutApiKey() {
        CodexaProperties.Ai aiConfig = new CodexaProperties.Ai(
                true,
                "ollama",
                "",
                "qwen2.5-coder:7b",
                "deepseek-coder:6.7b",
                "http://localhost:11434/v1/chat/completions",
                5000
        );
        CodexaProperties properties = new CodexaProperties(null, null, aiConfig, null);
        NvidiaNemotronAiClient client = new NvidiaNemotronAiClient(properties, objectMapper);

        assertTrue(client.isLocalOllama(), "isLocalOllama() should return true for provider=ollama");
        assertTrue(client.isConfigured(), "isConfigured() should return true for local Ollama even without API key");
    }

    @Test
    @DisplayName("Cloud OpenRouter mode should require API key to be considered configured")
    void testCloudProviderRequiresApiKey() {
        CodexaProperties.Ai aiConfig = new CodexaProperties.Ai(
                true,
                "openrouter",
                "",
                "meta-llama/llama-3.3-70b-instruct:free",
                "mistralai/mistral-small-24b-instruct-2501:free",
                "https://openrouter.ai/api/v1/chat/completions",
                5000
        );
        CodexaProperties properties = new CodexaProperties(null, null, aiConfig, null);
        NvidiaNemotronAiClient client = new NvidiaNemotronAiClient(properties, objectMapper);

        assertFalse(client.isLocalOllama(), "isLocalOllama() should return false for provider=openrouter");
        // Unless environment variables are present in runtime, isConfigured() checks API key
        if (System.getenv("OPENROUTER_API_KEY") == null && System.getenv("NVIDIA_API_KEY") == null) {
            assertFalse(client.isConfigured(), "isConfigured() should be false without API key");
        }
    }
}
