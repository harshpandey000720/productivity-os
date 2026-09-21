package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.service.AiService;
import org.springframework.stereotype.Service;
@Service
public class MockAiService implements AiService {
    @Override
    public String generateResponse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "yes running";
        }
        String lower = prompt.toLowerCase();
        if (lower.contains("roadmap")) {
            return "{\"status\": \"mock\", \"reply\": \"yes running - Roadmap generator placeholder initialized.\"}";
        }
        return "yes running";
    }
}
