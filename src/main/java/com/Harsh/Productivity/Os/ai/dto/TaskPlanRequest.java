package com.Harsh.Productivity.Os.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TaskPlanRequest(
        @NotEmpty @Size(max = 10) List<@Valid TaskInput> tasks) {

    public record TaskInput(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            LocalDate deadline) {}
}