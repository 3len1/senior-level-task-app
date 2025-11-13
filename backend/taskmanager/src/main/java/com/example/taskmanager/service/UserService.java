package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // Create user (defaults to ROLE_USER if null)
    public User register(String username, String rawPassword, UserRole role) {
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User u = User.builder()
                .username(username)
                .password(encoder.encode(rawPassword))
                .role(role == null ? UserRole.ROLE_USER : role)
                .build();
        return users.save(u);
    }

    public User create(String username, String rawPassword, UserRole role) {
        return register(username, rawPassword, role);
    }

    public void delete(Long id) {
        if (!users.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        users.deleteById(id);
    }

    public User changePassword(Long id, String newPassword) {
        User u = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        u.setPassword(encoder.encode(newPassword));
        return users.save(u);
    }

    public boolean passwordMatches(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }

    public List<User> list() { return users.findAll(); }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                List.of(new SimpleGrantedAuthority(u.getRole().name()))
        );
    }
}
