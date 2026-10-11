package com.Harsh.Productivity.Os.controller;

import com.Harsh.Productivity.Os.ai.TaskAiSuggestionService;
import com.Harsh.Productivity.Os.ai.dto.PriorityRequest;
import com.Harsh.Productivity.Os.ai.dto.PrioritySuggestion;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanRequest;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanResponse;
import com.Harsh.Productivity.Os.dto.AiChatRequest;
import com.Harsh.Productivity.Os.dto.AiChatResponse;
import com.Harsh.Productivity.Os.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Matches your existing CORS config
public class AiController {

    private final AiService aiService;
    private final TaskAiSuggestionService taskAiSuggestionService;
    public AiController(AiService aiService, TaskAiSuggestionService taskAiSuggestionService) {
        this.aiService = aiService;
        this.taskAiSuggestionService = taskAiSuggestionService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        String output = aiService.generateResponse(request.getPrompt());
        return ResponseEntity.ok(new AiChatResponse(output));
    }
    @PostMapping("/priority-suggestion")
    public PrioritySuggestion suggestPriority(@Valid @RequestBody PriorityRequest request) {
        return taskAiSuggestionService.suggestPriority(request);
    }

    @PostMapping("/task-plan")
    public TaskPlanResponse planTasks(@Valid @RequestBody TaskPlanRequest request) {
        return taskAiSuggestionService.planTasks(request);
    }
}