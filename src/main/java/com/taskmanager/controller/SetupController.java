package com.taskmanager.controller;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// ⚠️ TEMPORARY — delete this file after making yourself admin!
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final UserRepository userRepository;

    public SetupController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/setup/make-admin?email=your@email.com&secret=taskflow123
    @GetMapping("/make-admin")
    public ResponseEntity<?> makeAdmin(
            @RequestParam String email,
            @RequestParam String secret) {

        // Simple secret to prevent anyone else using this
        if (!"taskflow123".equals(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Wrong secret"));

        return userRepository.findByEmail(email).map(user -> {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                "message", "✅ " + user.getName() + " is now ADMIN!",
                "email", user.getEmail(),
                "role", user.getRole().name()
            ));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "User not found: " + email)));
    }
}