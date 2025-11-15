package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskCreateDto;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskUpdateDto;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.security.JwtAuthFilter;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Resource
    private MockMvc mvc;

    @Resource
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    // Mock security filter to avoid loading full security context in slice tests
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void list_by_project_returns_tasks() throws Exception {
        var t = new TaskDto(10L, "Do it", null, TaskStatus.TODO, null, 1L);
        Mockito.when(taskService.findByProject(1L)).thenReturn(List.of(t));

        mvc.perform(get("/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Do it"));
    }

    @Test
    void create_task_returns201() throws Exception {
        var req = new TaskCreateDto("New", "x", TaskStatus.TODO, null);
        var saved = new TaskDto(22L, "New", "x", TaskStatus.TODO, null, 1L);

        Mockito.when(taskService.create(eq(1L), any(TaskCreateDto.class))).thenReturn(saved);

        mvc.perform(post("/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(22))
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void update_task_returns200() throws Exception {
        var req = new TaskUpdateDto("Upd", "y", TaskStatus.IN_PROGRESS, null);
        var saved = new TaskDto(33L, "Upd", "y", TaskStatus.IN_PROGRESS, null, 1L);

        Mockito.when(taskService.update(eq(33L), any(TaskUpdateDto.class))).thenReturn(saved);

        mvc.perform(put("/tasks/33")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void delete_task_returns204() throws Exception {
        mvc.perform(delete("/tasks/44"))
                .andExpect(status().isNoContent());
        Mockito.verify(taskService).delete(44L);
    }

    @Test
    void list_by_project_404_when_project_missing() throws Exception {
        Mockito.when(taskService.findByProject(999L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Project not found"));

        mvc.perform(get("/projects/999/tasks"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_task_400_when_title_blank() throws Exception {
        // Requires @NotBlank on title and @Valid in controller
        var req = new TaskCreateDto("", "x", TaskStatus.TODO, null);

        mvc.perform(post("/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation")));
    }

    @Test
    void update_task_404_when_not_found() throws Exception {
        var req = new TaskUpdateDto("Upd", "y", TaskStatus.IN_PROGRESS, null);

        Mockito.when(taskService.update(eq(123L), Mockito.any(TaskUpdateDto.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Task not found"));

        mvc.perform(put("/tasks/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void delete_task_404_when_not_found() throws Exception {
        Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Task not found"))
                .when(taskService).delete(321L);

        mvc.perform(delete("/tasks/321"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

}
