package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.service.ProjectService;
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
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Manage projects")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class ProjectController {

    private final ProjectService projects;
    public ProjectController(ProjectService projects) { this.projects = projects; }

    @Operation(summary = "List all projects")
    @ApiResponse(responseCode = "200", description = "Projects retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectDto.class))))
    @GetMapping
    public List<ProjectDto> getAll() { return projects.findAll(); }

    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@RequestBody @Valid ProjectCreateDto p) { return projects.create(p); }
}
