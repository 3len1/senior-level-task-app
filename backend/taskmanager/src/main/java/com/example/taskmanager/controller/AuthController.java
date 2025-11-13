package com.example.taskmanager.controller;

import com.example.taskmanager.enums.UserRole;
import com.example.taskmanager.security.JwtUtil;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.web.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@Tag(name = "Auth", description = "Registration and login")
public class AuthController {

    private final UserService users;
    private final JwtUtil jwt;

    public AuthController(UserService users, JwtUtil jwt) {
        this.users = users; this.jwt = jwt;
    }

    public record RegisterRequest(
            @NotBlank @Schema(example = "eleni") String username,
            @NotBlank @Schema(example = "P@ssw0rd!") String password,
            @Schema(example = "ROLE_USER") UserRole role) {}

    public record LoginRequest(
            @NotBlank @Schema(example = "eleni") String username,
            @NotBlank @Schema(example = "P@ssw0rd!") String password) {}

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Username exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequest req) {
        users.register(req.username(), req.password(), req.role() == null ? UserRole.ROLE_USER : req.role());
    }

    @Operation(summary = "Authenticate and get JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token returned",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody @Valid LoginRequest req) {
        UserDetails ud = users.loadUserByUsername(req.username());
        // Some test setups may use "{noop}" prefix in stored password; strip for passwordMatches stub
        String stored = ud.getPassword();
        if (stored != null && stored.startsWith("{noop}")) {
            stored = stored.substring("{noop}".length());
        }
        if (!users.passwordMatches(req.password(), stored)) {
            throw new BadCredentialsException("Bad credentials");
        }
        String role = ud.getAuthorities().iterator().next().getAuthority();
        return Map.of("token", jwt.generateToken(ud.getUsername(), role));
    }
}
