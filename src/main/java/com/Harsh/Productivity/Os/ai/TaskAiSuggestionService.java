package com.Harsh.Productivity.Os.ai;
import com.Harsh.Productivity.Os.ai.dto.PriorityRequest;
import com.Harsh.Productivity.Os.ai.dto.PrioritySuggestion;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanDraft;
import com.Harsh.Productivity.Os.ai.dto.TaskPlanDraft.DraftStep;
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
import java.time.temporal.ChronoUnit;
@Service
public class TaskAiSuggestionService {
    private static final Logger log = LoggerFactory.getLogger(TaskAiSuggestionService.class);
    private final TaskPriorityAiServices priorityAi;
    private final TaskPlanningAiService planningAi;
    public TaskAiSuggestionService(TaskPriorityAiServices priorityAi, TaskPlanningAiService planningAi) {
        this.priorityAi = priorityAi;
        this.planningAi = planningAi;
    }
    private String describeDeadline(LocalDate deadline, LocalDate today) {
        if (deadline == null) return "none";
        long days = ChronoUnit.DAYS.between(today, deadline);
        if (days < 0) return deadline + " (overdue by " + (-days) + " days)";
        if (days == 0) return deadline + " (due today)";
        return deadline + " (in " + days + " days)";
    }
    public PrioritySuggestion suggestPriority(PriorityRequest req) {
        LocalDate today = LocalDate.now();
        PrioritySuggestion result = callSafely("priority suggestion", () ->
                priorityAi.suggestPriority(
                        req.title(),
                        blankToNone(req.description()),
                        describeDeadline(req.deadline(), today),
                        today.toString()));

        if (result == null || result.priority() == null
                || result.reasoning() == null || result.reasoning().isBlank()) {
            throw reject("The AI returned an incomplete priority suggestion", "incomplete priority", result);
        }
        return result;
    }
    public TaskPlanResponse planTasks(TaskPlanRequest req) {
        LocalDate today = LocalDate.now();
        List<TaskInput> tasks = req.tasks();
        String taskText = formatTasks(tasks, today);
        TaskPlanDraft draft = callSafely("task plan", () ->
                planningAi.planTasks(today.toString(), taskText));
        return validateAndMap(draft, tasks);
    }
    private TaskPlanResponse validateAndMap(TaskPlanDraft draft, List<TaskInput> input) {
        if (draft == null || draft.steps() == null || draft.steps().isEmpty()) {
            throw reject("The AI returned an empty task plan", "empty plan", draft);
        }
        if (draft.steps().size() != input.size()) {
            throw reject("The AI plan does not cover every task",
                    "expected " + input.size() + " steps but got " + draft.steps().size(), draft);
        }
        Set<String> remaining = new HashSet<>();
        for (int i = 0; i < input.size(); i++) {
            remaining.add("T" + (i + 1));
        }
        List<PlanStep> steps = new ArrayList<>();
        for (DraftStep step : draft.steps()) {
            if (step == null || step.taskId() == null) {
                throw reject("The AI plan contained an empty step", "null step or id", draft);
            }
            String id = step.taskId().trim().toUpperCase();
            if (!remaining.remove(id)) {
                throw reject("The AI plan referenced an unknown or duplicate task",
                        "id not matched: '" + step.taskId() + "' (still unmatched: " + remaining + ")", draft);
            }
            if (step.estimatedMinutes() < 5 || step.estimatedMinutes() > 480) {
                throw reject("The AI plan contained an unrealistic time estimate",
                        "estimatedMinutes=" + step.estimatedMinutes() + " for " + id, draft);
            }
            // The title comes from OUR data, never from the model's text
            TaskInput task = input.get(Integer.parseInt(id.substring(1)) - 1);
            steps.add(new PlanStep(task.title(), steps.size() + 1,
                    step.estimatedMinutes(), step.reason()));
        }
        return new TaskPlanResponse(draft.summary(), steps);
    }
    private String formatTasks(List<TaskInput> tasks, LocalDate today) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInput t = tasks.get(i);
            sb.append("T").append(i + 1).append(": ").append(t.title())
                    .append(" | deadline: ").append(describeDeadline(t.deadline(), today))
                    .append(" | details: ").append(blankToNone(t.description()))
                    .append("\n");
        }
        return sb.toString();
    }
    private <T> T callSafely(String operation, Supplier<T> call) {
        try {
            return call.get();
        } catch (RuntimeException e) {
            log.warn("AI {} call failed", operation, e);
            throw new AiResponseException("The AI could not produce a valid " + operation, e);
        }
    }
    // Logs the real reason server-side; the client only gets the safe public message
    private AiResponseException reject(String publicMessage, String detail, Object raw) {
        log.warn("AI output rejected: {} | raw={}", detail, raw);
        return new AiResponseException(publicMessage);
    }
    private String blankToNone(String s) { return (s == null || s.isBlank()) ? "none" : s; }
}