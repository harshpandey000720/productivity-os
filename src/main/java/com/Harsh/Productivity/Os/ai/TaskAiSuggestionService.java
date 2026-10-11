package com.Harsh.Productivity.Os.ai;
import com.Harsh.Productivity.Os.ai.dto.PriorityRequest;
import com.Harsh.Productivity.Os.ai.dto.PrioritySuggestion;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanRequest;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanRequest.TaskInput;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanResponse;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanResponse.PlanStep;
import com.Harsh.Productivity.Os.ai.exception.AiResponseException;
import com.Harsh.Productivity.Os.ai.service.TaskPlanningAiService;
import com.Harsh.Productivity.Os.ai.service.TaskPriorityAiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
@Service
public class TaskAiSuggestionService {
    private static final Logger log = LoggerFactory.getLogger(TaskAiSuggestionService.class);
    private final TaskPriorityAiServices priorityAi;
    private final TaskPlanningAiService planningAi;
    public TaskAiSuggestionService(TaskPriorityAiServices priorityAi, TaskPlanningAiService planningAi) {
        this.priorityAi = priorityAi;
        this.planningAi = planningAi;
    }
    public PrioritySuggestion suggestPriority(PriorityRequest req) {
        PrioritySuggestion result = callSafely("priority suggestion", () ->
                priorityAi.suggestPriority(
                        req.title(),
                        blankToNone(req.description()),
                        req.deadline() == null ? "none" : req.deadline().toString(),
                        LocalDate.now().toString()));
        if (result == null || result.priority() == null
                || result.reasoning() == null || result.reasoning().isBlank()) {
            throw reject("The AI returned an incomplete priority suggestion", "incomplete priority", result);
        }
        return result;
    }
    public TaskPlanResponse planTasks(TaskPlanRequest req) {
        String taskText = formatTasks(req.tasks());
        TaskPlanResponse raw = callSafely("task plan", () ->
                planningAi.planTasks(LocalDate.now().toString(), taskText));
        return validateAndNormalize(raw, req.tasks());
    }
    private TaskPlanResponse validateAndNormalize(TaskPlanResponse raw, List<TaskInput> input) {
        if (raw == null || raw.steps() == null || raw.steps().isEmpty()) {
            throw reject("The AI returned an empty task plan", "empty plan", raw);
        }
        if (raw.steps().size() != input.size()) {
            throw reject("The AI plan does not cover every task",
                    "expected " + input.size() + " steps but got " + raw.steps().size(), raw);
        }
        Set<String> remaining = input.stream()
                .map(t -> normalize(t.title()))
                .collect(Collectors.toCollection(HashSet::new));
        List<PlanStep> normalized = new ArrayList<>();
        for (PlanStep step : raw.steps()) {
            if (step == null || step.taskTitle() == null) {
                throw reject("The AI plan contained an empty step", "null step or title", raw);
            }
            if (!remaining.remove(normalize(step.taskTitle()))) {
                throw reject("The AI plan referenced an unknown or duplicate task",
                        "title not matched: '" + step.taskTitle() + "' (still unmatched: " + remaining + ")", raw);
            }
            if (step.estimatedMinutes() < 5 || step.estimatedMinutes() > 480) {
                throw reject("The AI plan contained an unrealistic time estimate",
                        "estimatedMinutes=" + step.estimatedMinutes() + " for '" + step.taskTitle() + "'", raw);
            }
            // Renumber ourselves rather than trusting the model's own numbering
            normalized.add(new PlanStep(step.taskTitle(), normalized.size() + 1,
                    step.estimatedMinutes(), step.reason()));
        }
        return new TaskPlanResponse(raw.summary(), normalized);
    }
    private String formatTasks(List<TaskInput> tasks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInput t = tasks.get(i);
            sb.append(i + 1).append(". ").append(t.title())
                    .append(" | deadline: ").append(t.deadline() == null ? "none" : t.deadline())
                    .append(" | details: ").append(blankToNone(t.description()))
                    .append("\n");
        }
        return sb.toString();
    }
    private <T> T callSafely(String operation, Supplier<T> call) {
        try {
            return call.get();
        } catch (RuntimeException e) {
            log.warn("AI {} call failed", operation, e); // full stack trace + cause chain
            throw new AiResponseException("The AI could not produce a valid " + operation, e);
        }
    }
    // Logs the real reason server-side; the client only gets the safe public message
    private AiResponseException reject(String publicMessage, String detail, Object raw) {
        log.warn("AI output rejected: {} | raw={}", detail, raw);
        return new AiResponseException(publicMessage);
    }
    private String blankToNone(String s) { return (s == null || s.isBlank()) ? "none" : s; }
    private String normalize(String s) { return s.trim().toLowerCase(); }
}