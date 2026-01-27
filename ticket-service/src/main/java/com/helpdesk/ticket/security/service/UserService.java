package com.helpdesk.ticket.security.service;

import com.helpdesk.ticket.security.model.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final Map<String, User> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void init() {
        createUser("naman.punn", "password123", "naman.punn@gmail.com", "Naman Punn",
                Set.of("ROLE_USER"));

        createUser("admin", "admin123", "admin@company.com", "Admin User",
                Set.of("ROLE_USER", "ROLE_ADMIN"));

        createUser("it.support", "support123", "it.support@company.com", "IT Support",
                Set.of("ROLE_USER", "ROLE_SUPPORT"));

        log.info("Initialized {} demo users", users.size());
        log.info("Available users: john.doe, jane.smith, admin, it.support");
        log.info("Password for all demo users: password123 (admin: admin123, it.support: support123)");
    }

    private void createUser(String username, String password, String email, String fullName, Set<String> roles) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .enabled(true)
                .build();

        users.put(username, user);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}