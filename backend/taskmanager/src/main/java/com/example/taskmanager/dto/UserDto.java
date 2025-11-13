package com.example.taskmanager.dto;

import com.example.taskmanager.enums.UserRole;

public record UserDto(
        Long id,
        String username,
        UserRole role
) {}
