package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.ProjectCreateDto;
import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    ProjectDto toDto(Project entity);
    Project toEntity(ProjectCreateDto dto);
}
