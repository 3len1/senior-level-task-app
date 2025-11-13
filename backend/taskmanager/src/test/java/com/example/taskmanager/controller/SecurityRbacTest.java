package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.dto.TaskCreateDto;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskUpdateDto;
import com.example.taskmanager.enums.TaskStatus;
import com.example.taskmanager.security.SecurityConfig;
import com.example.taskmanager.service.ProjectService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies role-based HTTP access rules from SecurityConfig without relying on JWT.
 * We authenticate using Spring Security test support (with(user(...).roles(...))).
 */
@WebMvcTest(controllers = { ProjectController.class, TaskController.class, UserController.class })
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        "app.jwt.secret=testsecret_please_change_me_0123456789"
})
@org.springframework.context.annotation.Import(SecurityConfig.class)
class SecurityRbacTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    // Mock application services used by controllers
    @MockBean ProjectService projectService;
    @MockBean TaskService taskService;
    @MockBean UserService userService;
    // Mock MapStruct mapper required by UserController in this slice context
    @MockBean com.example.taskmanager.mapper.UserMapper userMapper;

    // Ensure JWT filter doesn't short-circuit the chain in this RBAC test
    @MockBean com.example.taskmanager.security.JwtAuthFilter jwtAuthFilter;

    @org.junit.jupiter.api.BeforeEach
    void setupJwtPassThrough() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            jakarta.servlet.http.HttpServletRequest req = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse res = invocation.getArgument(1);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void post_projects_forbidden_for_USER_allowed_for_ADMIN() throws Exception {
        var body = new ProjectCreateDto("New", "d");
        var saved = new ProjectDto(10L, "New", "d", null);
        Mockito.when(projectService.create(any(ProjectCreateDto.class))).thenReturn(saved);

        // USER -> 403
        mvc.perform(post("/projects")
                        .with(SecurityMockMvcRequestPostProcessors.user("alice").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        // ADMIN -> 201
        mvc.perform(post("/projects")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void get_users_forbidden_for_USER_allowed_for_ADMIN_and_MODERATOR() throws Exception {
        Mockito.when(userService.list()).thenReturn(java.util.List.of());

        // USER -> 403
        mvc.perform(get("/users").with(SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(status().isForbidden());

        // ADMIN -> 200
        mvc.perform(get("/users").with(SecurityMockMvcRequestPostProcessors.user("a").roles("ADMIN")))
                .andExpect(status().isOk());

        // MODERATOR -> 200
        mvc.perform(get("/users").with(SecurityMockMvcRequestPostProcessors.user("m").roles("MODERATOR")))
                .andExpect(status().isOk());
    }

    @Test
    void post_project_tasks_forbidden_for_USER_allowed_for_ADMIN_and_MODERATOR() throws Exception {
        var req = new TaskCreateDto("T", null, TaskStatus.TODO, null, null);
        var saved = new TaskDto(5L, "T", null, TaskStatus.TODO, null, 1L, null, null);
        Mockito.when(taskService.create(eq(1L), any(TaskCreateDto.class))).thenReturn(saved);

        // USER -> 403
        mvc.perform(post("/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user("u").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // MODERATOR -> 201
        mvc.perform(post("/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user("mod").roles("MODERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));

        // ADMIN -> 201
        mvc.perform(post("/projects/1/tasks")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void put_and_delete_tasks_forbidden_for_USER_allowed_for_MODERATOR_and_ADMIN() throws Exception {
        var req = new TaskUpdateDto("Upd", null, TaskStatus.IN_PROGRESS, null, null);
        var saved = new TaskDto(9L, "Upd", null, TaskStatus.IN_PROGRESS, null, 1L, null, null);
        Mockito.when(taskService.update(eq(9L), any(TaskUpdateDto.class))).thenReturn(saved);

        // PUT as USER -> 403
        mvc.perform(put("/tasks/9")
                        .with(SecurityMockMvcRequestPostProcessors.user("u").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // PUT as MODERATOR -> 200
        mvc.perform(put("/tasks/9")
                        .with(SecurityMockMvcRequestPostProcessors.user("m").roles("MODERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));

        // DELETE as USER -> 403
        mvc.perform(delete("/tasks/9")
                        .with(SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(status().isForbidden());

        // DELETE as ADMIN -> 204
        mvc.perform(delete("/tasks/9")
                        .with(SecurityMockMvcRequestPostProcessors.user("a").roles("ADMIN")))
                .andExpect(status().isNoContent());
        Mockito.verify(taskService, Mockito.times(1)).delete(9L);
    }
}
