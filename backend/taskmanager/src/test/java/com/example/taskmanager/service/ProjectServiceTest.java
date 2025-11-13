package com.example.taskmanager.service;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;

    @InjectMocks ProjectService projectService;

    @Test
    void findAll_returnsAllProjects() {
        var p = Project.builder().id(1L).name("Alpha").build();
        when(projectRepository.findAll()).thenReturn(List.of(p));

        var result = projectService.findAll();

        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).getName());
        verify(projectRepository).findAll();
    }

    @Test
    void create_savesAndReturns() {
        var req = Project.builder().name("New").build();
        var saved = Project.builder().id(10L).name("New").build();
        when(projectRepository.save(req)).thenReturn(saved);

        var result = projectService.create(req);

        assertEquals(10L, result.getId());
        verify(projectRepository).save(req);
    }

    @Test
    void getOr404_found() {
        var p = Project.builder().id(5L).name("P").build();
        when(projectRepository.findById(5L)).thenReturn(Optional.of(p));

        var result = projectService.getOr404(5L);

        assertEquals(5L, result.getId());
        verify(projectRepository).findById(5L);
    }

    @Test
    void getOr404_notFound_throws404() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class, () -> projectService.getOr404(99L));
        assertEquals(404, ex.getStatusCode().value());
    }
}
