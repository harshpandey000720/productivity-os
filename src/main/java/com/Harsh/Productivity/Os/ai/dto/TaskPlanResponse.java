package com.Harsh.Productivity.Os.ai.dto;
import dev.langchain4j.model.output.structured.Description;
import java.util.List;
public record TaskPlanResponse(
        @Description("One-sentence summary of the overall plan")
        String summary,
        @Description("Every input task exactly once, in the order they should be done")
        List<PlanStep> steps) {
    public record PlanStep(
            @Description("The exact title of the task, copied from the input")
            String taskTitle,
            @Description("Position in the plan, starting at 1")
            int order,
            @Description("Estimated minutes needed, a whole number between 5 and 480")
            int estimatedMinutes,
            @Description("Short reason for this position")
            String reason) {}
}