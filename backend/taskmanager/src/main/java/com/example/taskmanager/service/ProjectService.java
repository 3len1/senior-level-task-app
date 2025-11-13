package com.example.taskmanager.service;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.mapper.ProjectMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository projects, ProjectMapper mapper) {
        this.projects = projects;
        this.mapper = mapper;
    }

    // Return all projects for any authenticated user (visibility widened per request)
    public List<ProjectDto> findAll() {
        return projects.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ProjectDto create(ProjectCreateDto dto) {
        Project entity = mapper.toEntity(dto);
        Project saved = projects.save(entity);
        return mapper.toDto(saved);
    }

    public Project getOr404(Long id) {
        return projects.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public void delete(Long id) {
        Project p = projects.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        projects.delete(p); // cascade removes tasks due to orphanRemoval = true
    }
}
