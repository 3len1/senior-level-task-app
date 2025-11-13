package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectCreateDto(
        @NotBlank String name,
        String description
) {}
