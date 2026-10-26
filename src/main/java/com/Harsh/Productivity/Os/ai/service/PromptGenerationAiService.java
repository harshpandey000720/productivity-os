package com.Harsh.Productivity.Os.ai.service;

import dev.langchain4j.service.UserMessage;

public interface PromptGenerationAiService {
    String generateResponse(@UserMessage String prompt);
}