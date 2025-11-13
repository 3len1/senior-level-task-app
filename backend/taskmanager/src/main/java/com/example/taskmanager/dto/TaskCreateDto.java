package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record TaskCreateDto(
        @NotBlank String title,
        String description,
        @NotNull TaskStatus status,
        Instant deadline,
        Long assigneeId
) {}
