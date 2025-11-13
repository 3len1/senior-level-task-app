package com.example.taskmanager.controller;

import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.security.JwtAuthFilter;
import com.example.taskmanager.security.JwtUtil;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for slice test
class AuthControllerTest {

    @Resource
    private MockMvc mvc;

    @Resource
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    // Mock security filter to satisfy context if SecurityConfig is loaded
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_returns201() throws Exception {
        var body = new AuthController.RegisterRequest("eleni", "P@ssw0rd!", UserRole.ROLE_USER);

        Mockito.when(userService.register(eq("eleni"), eq("P@ssw0rd!"), eq(UserRole.ROLE_USER)))
                .thenReturn(null);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_returns_token() throws Exception {
        var body = new AuthController.LoginRequest("eleni", "P@ssw0rd!");
        UserDetails ud = new User("eleni", "{noop}ENCODED", List.of(() -> "ROLE_USER"));

        Mockito.when(userService.loadUserByUsername("eleni")).thenReturn(ud);
        Mockito.when(userService.passwordMatches("P@ssw0rd!", "ENCODED")).thenReturn(true);
        Mockito.when(jwtUtil.generateToken("eleni", "ROLE_USER")).thenReturn("fake.jwt.token");

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake.jwt.token"));
    }

    @Test
    void login_401() throws Exception {
        var body = new AuthController.LoginRequest("eleni", "bad");
        UserDetails ud = new User("eleni", "{noop}ENCODED", List.of(() -> "ROLE_USER"));

        Mockito.when(userService.loadUserByUsername("eleni")).thenReturn(ud);
        Mockito.when(userService.passwordMatches("bad", "ENCODED")).thenReturn(false);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_409_when_username_exists() throws Exception {
        var body = new AuthController.RegisterRequest("eleni", "P@ssw0rd!", UserRole.ROLE_USER);

        // Service signals conflict (username taken)
        Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Username already exists"))
                .when(userService).register("eleni", "P@ssw0rd!", UserRole.ROLE_USER);

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Username")));
    }

    @Test
    void register_400_when_body_invalid() throws Exception {
        // Missing username / bad payload -> validation or JSON mapping error -> 400 via advice
        var invalidJson = """
      {"username":"","password":""}
    """;

        mvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

}
