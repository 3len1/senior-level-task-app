package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.security.JwtAuthFilter;
import com.example.taskmanager.service.ProjectService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Resource
    private MockMvc mvc;

    @Resource
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    // Mock security filter to avoid loading full security context in slice tests
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getAll_returns_list() throws Exception {
        var p = new ProjectDto(1L, "Alpha", "desc", null);
        Mockito.when(projectService.findAll()).thenReturn(List.of(p));

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alpha"));
    }

    @Test
    void create_returns201_and_body() throws Exception {
        var req = new ProjectCreateDto("New", "d");
        var saved = new ProjectDto(5L, "New", "d", null);

        Mockito.when(projectService.create(any(ProjectCreateDto.class))).thenReturn(saved);

        mvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        Mockito.when(projectService.findAll()).thenReturn(java.util.Collections.emptyList());

        mvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_400_when_name_blank() throws Exception {
        // Needs @Valid on controller method and @NotBlank on ProjectCreateDto.name
        var req = new ProjectCreateDto("", "d");

        mvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation")));
    }

    @Test
    void create_500_when_service_throws_unexpected() throws Exception {
        var req = new ProjectCreateDto("Boom", null);
        Mockito.when(projectService.create(Mockito.any(ProjectCreateDto.class)))
                .thenThrow(new RuntimeException("DB down"));

        mvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

}
