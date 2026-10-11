package com.Harsh.Productivity.Os.ai.dto;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

public record TaskPlanDraft(
        @Description("One-sentence summary of the overall plan")
        String summary,
        @Description("Every input task exactly once, in the order they should be done")
        List<DraftStep> steps) {

    public record DraftStep(
            @Description("The task id exactly as given in the input, for example T1")
            String taskId,
            @Description("Estimated minutes needed, a whole number between 5 and 480")
            int estimatedMinutes,
            @Description("Short reason for this position")
            String reason) {}
}