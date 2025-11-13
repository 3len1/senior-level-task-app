package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    @Test
    void register_whenNew_encodesPassword_andSaves() {
        when(userRepository.existsByUsername("eleni")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("ENC");
        var saved = User.builder().id(1L).username("eleni").password("ENC").role(UserRole.ROLE_USER).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        var result = userService.register("eleni", "P@ssw0rd!", UserRole.ROLE_USER);

        assertEquals(1L, result.getId());
        assertEquals("ENC", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenExists_throws409() {
        when(userRepository.existsByUsername("eleni")).thenReturn(true);

        var ex = assertThrows(ResponseStatusException.class,
                () -> userService.register("eleni", "x", UserRole.ROLE_USER));
        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode().value());
    }

    @Test
    void passwordMatches_delegatesToEncoder() {
        when(passwordEncoder.matches("raw", "enc")).thenReturn(true);
        assertTrue(userService.passwordMatches("raw", "enc"));
    }

    @Test
    void list_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(User.builder().id(1L).build()));
        var users = userService.list();
        assertEquals(1, users.size());
    }

    @Test
    void loadUserByUsername_found_buildsUserDetails() {
        var u = User.builder().username("eleni").password("ENC").role(UserRole.ROLE_ADMIN).build();
        when(userRepository.findByUsername("eleni")).thenReturn(Optional.of(u));

        UserDetails ud = userService.loadUserByUsername("eleni");

        assertEquals("eleni", ud.getUsername());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_missing_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost"));
    }
}
