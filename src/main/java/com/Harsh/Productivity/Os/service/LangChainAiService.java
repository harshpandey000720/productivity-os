package com.Harsh.Productivity.Os.service;

import com.Harsh.Productivity.Os.ai.gateway.AiGateway;
import com.Harsh.Productivity.Os.ai.service.PromptGenerationAiService;
import org.springframework.stereotype.Service;

@Service
public class LangChainAiService implements AiService {
    private final AiGateway aiGateway;

    public LangChainAiService(AiGateway aiGateway) {
        this.aiGateway = aiGateway;
    }

    @Override
    public String generateResponse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Prompt cannot be empty";
        }

        return aiGateway.call(
                PromptGenerationAiService.class,
                ai -> ai.generateResponse(prompt)
        );
    }
}