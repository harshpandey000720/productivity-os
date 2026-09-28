package com.Harsh.Productivity.Os.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class LangChainAiService implements AiService {
    private final ChatModel chatModel;
    public LangChainAiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
    @Override
    public String generateResponse(String prompt) {
        if (prompt == null||prompt.isBlank()) {
            return "Prompt cannot be empty";
        }
        return chatModel.chat(prompt);
    }
}
