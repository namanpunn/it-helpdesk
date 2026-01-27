package com.helpdesk.status.security.controller;

import com.helpdesk.status.dto.ApiResponse;
import com.helpdesk.status.security.dto.LoginRequest;
import com.helpdesk.status.security.dto.LoginResponse;
import com.helpdesk.status.security.model.User;
import com.helpdesk.status.security.service.AuthenticationService;
import com.helpdesk.status.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and JWT token management")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());

        try {
            LoginResponse loginResponse = authenticationService.authenticate(request);

            ApiResponse<LoginResponse> response = ApiResponse.success(
                    "Login successful",
                    loginResponse
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - {}", request.getUsername(), e.getMessage());

            ApiResponse<LoginResponse> response = ApiResponse.error(
                    "Login failed",
                    e.getMessage()
            );

            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get list of all demo users (for testing purposes)")
    public ResponseEntity<ApiResponse<List<UserInfo>>> getAllUsers() {
        List<User> users = userService.getAllUsers();

        List<UserInfo> userInfos = users.stream()
                .map(user -> new UserInfo(
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRoles()
                ))
                .collect(Collectors.toList());

        ApiResponse<List<UserInfo>> response = ApiResponse.success(
                "Users retrieved successfully",
                userInfos
        );

        return ResponseEntity.ok(response);
    }

    public record UserInfo(String username, String email, String fullName, java.util.Set<String> roles) {
    }
}