package com.helpdesk.ticket.security.service;

import com.helpdesk.ticket.security.JwtUtil;
import com.helpdesk.ticket.security.dto.LoginRequest;
import com.helpdesk.ticket.security.dto.LoginResponse;
import com.helpdesk.ticket.security.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new BadCredentialsException("Invalid username or password");
                });

        if (!user.isEnabled()) {
            log.warn("User account disabled: {}", request.getUsername());
            throw new BadCredentialsException("User account is disabled");
        }

        if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles(), user.getEmail());

        log.info("User authenticated successfully: {}", request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles())
                .expiresAt(jwtUtil.extractExpiration(token))
                .build();
    }
}