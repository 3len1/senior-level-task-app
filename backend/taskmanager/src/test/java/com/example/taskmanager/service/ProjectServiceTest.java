package com.example.taskmanager.service;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.mapper.ProjectMapper;
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
    @Mock ProjectMapper projectMapper;

    @InjectMocks ProjectService projectService;

    @Test
    void findAll_returnsAllProjects() {
        var p = Project.builder().id(1L).name("Alpha").build();
        when(projectRepository.findAll()).thenReturn(List.of(p));
        when(projectMapper.toDto(p)).thenReturn(new ProjectDto(1L, "Alpha", null, null));

        var result = projectService.findAll();

        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).name());
        verify(projectRepository).findAll();
        verify(projectMapper).toDto(p);
    }

    @Test
    void create_savesAndReturns() {
        var req = new ProjectCreateDto("New", null);
        var toSave = Project.builder().name("New").build();
        var saved = Project.builder().id(10L).name("New").build();
        when(projectMapper.toEntity(req)).thenReturn(toSave);
        when(projectRepository.save(toSave)).thenReturn(saved);
        when(projectMapper.toDto(saved)).thenReturn(new ProjectDto(10L, "New", null, null));

        var result = projectService.create(req);

        assertEquals(10L, result.id());
        assertEquals("New", result.name());
        verify(projectMapper).toEntity(req);
        verify(projectRepository).save(toSave);
        verify(projectMapper).toDto(saved);
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
