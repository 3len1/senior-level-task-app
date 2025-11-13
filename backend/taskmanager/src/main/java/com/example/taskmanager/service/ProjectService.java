package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    public List<Project> findAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return projects.findAll();
        }
        if (hasRole(auth.getAuthorities(), "ROLE_USER") && !hasRole(auth.getAuthorities(), "ROLE_ADMIN") && !hasRole(auth.getAuthorities(), "ROLE_MODERATOR")) {
            return projects.findDistinctByTasks_Assignee_Username(auth.getName());
        }
        return projects.findAll();
    }

    public Project create(Project p) {
        return projects.save(p);
    }

    public Project getOr404(Long id) {
        return projects.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        for (GrantedAuthority ga : authorities) {
            if (role.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
