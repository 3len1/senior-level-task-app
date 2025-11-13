package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskCreateDto;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskUpdateDto;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.web.ApiError;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@Tag(name = "Tasks", description = "Manage tasks within projects")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class TaskController {

    private final TaskService tasks;
    public TaskController(TaskService tasks) { this.tasks = tasks; }

    @Operation(summary = "List tasks for a project")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))))
    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskDto> byProject(@Parameter(description = "Project ID") @PathVariable Long projectId) {
        return tasks.findByProject(projectId);
    }

    @Operation(summary = "Create task in a project")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created",
                content = @Content(schema = @Schema(implementation = TaskDto.class))),
        @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@PathVariable Long projectId, @RequestBody @Valid TaskCreateDto t) {
        return tasks.create(projectId, t);
    }

    @Operation(summary = "Update a task")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated",
                content = @Content(schema = @Schema(implementation = TaskDto.class))),
        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/tasks/{id}")
    public TaskDto update(@PathVariable Long id, @RequestBody @Valid TaskUpdateDto t) {
        return tasks.update(id, t);
    }

    @Operation(summary = "Delete a task")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted"),
        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tasks.delete(id);
    }
}
