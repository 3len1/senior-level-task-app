package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatus;
import java.time.Instant;

public record TaskDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Instant deadline,
        Long projectId,
        Long assigneeId,
        String assigneeUsername
) {}
