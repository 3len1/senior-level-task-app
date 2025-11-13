package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.security.JwtAuthFilter;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Resource
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    // Mock security filter to avoid loading full security context in slice tests
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void list_users_returns200() throws Exception {
        var u = User.builder().id(1L).username("eleni").role(UserRole.ROLE_USER).build();
        Mockito.when(userService.list()).thenReturn(List.of(u));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("eleni"));
    }

    @Test
    void list_users_returnsEmptyArray() throws Exception {
        Mockito.when(userService.list()).thenReturn(java.util.Collections.emptyList());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

}
