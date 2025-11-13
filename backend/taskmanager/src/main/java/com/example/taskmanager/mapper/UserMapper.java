package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDto toDto(User user);
}