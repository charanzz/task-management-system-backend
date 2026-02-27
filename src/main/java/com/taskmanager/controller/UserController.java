package com.taskmanager.controller;

import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.dto.UserStats;
import com.taskmanager.entity.User;
import com.taskmanager.entity.Role;
import com.taskmanager.service.UserService;
import com.taskmanager.service.TaskService;
import com.taskmanager.config.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TaskService taskService;

    public UserController(UserService userService, JwtUtil jwtUtil, TaskService taskService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.taskService = taskService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
            }

            User user = new User();
            user.setName(request.getName().trim());
            user.setEmail(request.getEmail().trim());
            user.setPassword(request.getPassword());
            user.setRole(Role.USER);

            User saved = userService.registerUser(user);
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "email", saved.getEmail(),
                "name", saved.getName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.login(
                request.getEmail(),
                request.getPassword()
        );
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", user.getEmail(),
            "name", user.getName(),
            "id", user.getId()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getMyStats(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        UserStats stats = taskService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/test")
    public String test() {
        return "Users API working!";
    }
}