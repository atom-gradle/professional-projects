package com.qian.agent.config;

import com.qian.agent.agent.tools.ComplianceCheckTool;
import com.qian.agent.agent.tools.RiskKeywordTool;
import com.qian.agent.agent.tools.TaxEstimateTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            ComplianceCheckTool complianceCheckTool,
            RiskKeywordTool riskKeywordTool,
            TaxEstimateTool taxEstimateTool
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(complianceCheckTool, riskKeywordTool, taxEstimateTool)
                .build();
    }

    @Bean
    public ChatClient chatClient(
            OpenAiChatModel chatModel,
            ToolCallbackProvider toolCallbackProvider
    ) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}