package com.qian.agent.agent.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptBuilder {

    public String systemPrompt() {
        try {
            return new ClassPathResource("prompts/system-agent.st.bak")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load system prompt template", e);
        }
    }
}
