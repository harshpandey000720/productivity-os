package com.Harsh.Productivity.Os.controller;

import com.Harsh.Productivity.Os.dto.AiChatRequest;
import com.Harsh.Productivity.Os.dto.AiChatResponse;
import com.Harsh.Productivity.Os.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Matches your existing CORS config
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        String output = aiService.generateResponse(request.getPrompt());
        return ResponseEntity.ok(new AiChatResponse(output));
    }
}