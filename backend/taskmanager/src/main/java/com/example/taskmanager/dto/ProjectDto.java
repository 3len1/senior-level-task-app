package com.example.taskmanager.dto;

import java.time.Instant;

public record ProjectDto(
        Long id,
        String name,
        String description,
        Instant createdDate
) {}
