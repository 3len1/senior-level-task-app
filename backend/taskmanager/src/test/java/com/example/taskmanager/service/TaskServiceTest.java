package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskCreateDto;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskUpdateDto;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectService projectService;
    @Mock com.example.taskmanager.repository.UserRepository userRepository;
    @Mock SimpMessagingTemplate broker;
    @Mock TaskMapper taskMapper;

    @InjectMocks TaskService taskService;

    @Test
    void findByProject_returnsTasks_andChecksProjectExists() {
        when(projectService.getOr404(1L)).thenReturn(Project.builder().id(1L).build());
        var entity = Task.builder().id(7L).title("t").status(TaskStatus.TODO).build();
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(entity));
        var dto = new TaskDto(7L, "t", null, TaskStatus.TODO, null, 1L, null, null);
        when(taskMapper.toDto(entity)).thenReturn(dto);

        var result = taskService.findByProject(1L);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).id());
        verify(projectService).getOr404(1L);
        verify(taskRepository).findByProjectId(1L);
        verify(taskMapper).toDto(entity);
    }

    @Test
    void create_savesTask_andBroadcasts() {
        var p = Project.builder().id(1L).build();
        when(projectService.getOr404(1L)).thenReturn(p);
        var req = new TaskCreateDto("New", "x", TaskStatus.TODO, null, null);
        var saved = Task.builder().id(11L).title("New").status(TaskStatus.TODO).project(p).build();
        when(taskRepository.save(any(Task.class))).thenReturn(saved);
        var dto = new TaskDto(11L, "New", "x", TaskStatus.TODO, null, 1L, null, null);
        when(taskMapper.toDto(saved)).thenReturn(dto);

        var result = taskService.create(1L, req);

        assertEquals(11L, result.id());
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toDto(saved);
        verify(broker).convertAndSend(eq("/topic/projects/1/tasks"), eq(dto));
    }

    @Test
    void update_whenExists_updatesAndBroadcasts() {
        var p = Project.builder().id(2L).build();
        var existing = Task.builder().id(33L).title("Old").description("x").status(TaskStatus.TODO).project(p).build();
        var incoming = new TaskUpdateDto("Upd", "y", TaskStatus.IN_PROGRESS, null, null);
        var saved = Task.builder().id(33L).title("Upd").description("y").status(TaskStatus.IN_PROGRESS).project(p).build();

        when(taskRepository.findById(33L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(saved);
        var dto = new TaskDto(33L, "Upd", "y", TaskStatus.IN_PROGRESS, null, 2L, null, null);
        when(taskMapper.toDto(saved)).thenReturn(dto);

        var result = taskService.update(33L, incoming);

        assertEquals(TaskStatus.IN_PROGRESS, result.status());
        assertEquals("Upd", result.title());
        verify(taskRepository).save(existing);
        verify(taskMapper).toDto(saved);
        verify(broker).convertAndSend(eq("/topic/projects/2/tasks"), eq(dto));
    }

    @Test
    void update_whenMissing_throws404() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> taskService.update(99L, new TaskUpdateDto("t","d", TaskStatus.TODO, null, null)));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void delete_removes_andBroadcastsDeletion() {
        var p = Project.builder().id(9L).build();
        var existing = Task.builder().id(44L).project(p).build();
        when(taskRepository.findById(44L)).thenReturn(Optional.of(existing));

        taskService.delete(44L);

        verify(taskRepository).delete(existing);
        verify(broker).convertAndSend(eq("/topic/projects/9/tasks"),
                argThat((Map<String, Object> m) -> m.containsKey("deletedId") && m.get("deletedId").equals(44L)));
    }

    @Test
    void delete_whenMissing_throws404() {
        when(taskRepository.findById(123L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> taskService.delete(123L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
