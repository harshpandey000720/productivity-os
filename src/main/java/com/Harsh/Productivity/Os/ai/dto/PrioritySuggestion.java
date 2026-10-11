package com.Harsh.Productivity.Os.ai.dto;
import dev.langchain4j.model.output.structured.Description;
public record PrioritySuggestion(@Description("Suggested priority, exactly one of: LOW, MEDIUM, HIGH")
                          Priority priority,
                          @Description("One or two short sentences explaining the choice")
                          String reasoning) {
    public enum Priority {LOW,MEDIUM,HIGH}
}
