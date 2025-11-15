package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskCreateDto;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskUpdateDto;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectService projects;
    private final SimpMessagingTemplate broker; // WebSocket broadcasts
    private final TaskMapper mapper;

    public TaskService(TaskRepository tasks, ProjectService projects, SimpMessagingTemplate broker, TaskMapper mapper) {
        this.tasks = tasks;
        this.projects = projects;
        this.broker = broker;
        this.mapper = mapper;
    }

    // Return all tasks for a project for any authenticated user (visibility widened per request)
    public List<TaskDto> findByProject(Long projectId) {
        projects.getOr404(projectId);
        return tasks.findByProjectId(projectId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public TaskDto create(Long projectId, TaskCreateDto dto) {
        Project p = projects.getOr404(projectId);
        Task t = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .deadline(dto.deadline())
                .project(p)
                .build();
        Task saved = tasks.save(t);
        TaskDto out = mapper.toDto(saved);
        // Broadcast to project-specific topic
        broker.convertAndSend("/topic/projects/" + projectId + "/tasks", out);
        // Also broadcast globally so everyone can receive notifications on task creation
        broker.convertAndSend("/topic/tasks", out);
        return out;
    }

    public TaskDto update(Long id, TaskUpdateDto incoming) {
        Task existing = tasks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        existing.setTitle(incoming.title());
        existing.setDescription(incoming.description());
        existing.setStatus(incoming.status());
        existing.setDeadline(incoming.deadline());
        Task saved = tasks.save(existing);
        TaskDto out = mapper.toDto(saved);
        broker.convertAndSend("/topic/projects/" + saved.getProject().getId() + "/tasks", out);
        return out;
    }

    public void delete(Long id) {
        Task existing = tasks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        Long projectId = existing.getProject().getId();
        tasks.delete(existing);
        broker.convertAndSend("/topic/projects/" + projectId + "/tasks", Map.of("deletedId", id));
    }

    public TaskDto get(Long id) {
        Task t = tasks.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return mapper.toDto(t);
    }
}
