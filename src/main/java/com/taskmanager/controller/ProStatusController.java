package com.taskmanager.controller;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Replaces StripeController — no Stripe needed
@RestController
@RequestMapping("/api/stripe")
public class ProStatusController {

    private final UserRepository userRepository;

    public ProStatusController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/stripe/status — PricingPage calls this to check if user is Pro
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(Map.of(
                "isPro", Boolean.TRUE.equals(user.getIsPro()),
                "proExpiresAt", user.getProExpiresAt() != null ? user.getProExpiresAt().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isPro", false, "proExpiresAt", ""));
        }
    }
}