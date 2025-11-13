package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectService projects;
    private final UserRepository users;
    private final SimpMessagingTemplate broker; // WebSocket broadcasts

    public TaskService(TaskRepository tasks, ProjectService projects, UserRepository users, SimpMessagingTemplate broker) {
        this.tasks = tasks;
        this.projects = projects;
        this.users = users;
        this.broker = broker;
    }

    public List<Task> findByProject(Long projectId) {
        projects.getOr404(projectId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && isOnlyUser(auth.getAuthorities())) {
            return tasks.findByProjectIdAndAssignee_Username(projectId, auth.getName());
        }
        return tasks.findByProjectId(projectId);
    }

    public Task create(Long projectId, Task t) {
        Project p = projects.getOr404(projectId);
        t.setProject(p);
        // Resolve assignee if provided with id
        if (t.getAssignee() != null && t.getAssignee().getId() != null) {
            User assignee = users.findById(t.getAssignee().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
            t.setAssignee(assignee);
        }
        Task saved = tasks.save(t);
        broker.convertAndSend("/topic/projects/" + projectId + "/tasks", saved);
        return saved;
    }

    public Task update(Long id, Task incoming) {
        Task existing = tasks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setStatus(incoming.getStatus());
        existing.setDeadline(incoming.getDeadline());
        if (incoming.getAssignee() != null) {
            if (incoming.getAssignee().getId() != null) {
                User assignee = users.findById(incoming.getAssignee().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
                existing.setAssignee(assignee);
            } else {
                existing.setAssignee(null);
            }
        }
        Task saved = tasks.save(existing);
        broker.convertAndSend("/topic/projects/" + saved.getProject().getId() + "/tasks", saved);
        return saved;
    }

    public void delete(Long id) {
        Task existing = tasks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Long projectId = existing.getProject().getId();
        tasks.delete(existing);
        broker.convertAndSend("/topic/projects/" + projectId + "/tasks", Map.of("deletedId", id));
    }

    private boolean isOnlyUser(Collection<? extends GrantedAuthority> authorities) {
        boolean hasUser = false;
        boolean hasAdminOrMod = false;
        for (GrantedAuthority ga : authorities) {
            String a = ga.getAuthority();
            if (Objects.equals(a, "ROLE_USER")) hasUser = true;
            if (Objects.equals(a, "ROLE_ADMIN") || Objects.equals(a, "ROLE_MODERATOR")) hasAdminOrMod = true;
        }
        return hasUser && !hasAdminOrMod;
    }
}
