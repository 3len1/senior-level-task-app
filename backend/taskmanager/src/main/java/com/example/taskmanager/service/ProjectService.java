package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    // Return all projects for any authenticated user (visibility widened per request)
    public List<Project> findAll() {
        return projects.findAll();
    }

    public Project create(Project p) {
        return projects.save(p);
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
