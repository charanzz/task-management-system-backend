package com.taskmanager.controller;
import jakarta.transaction.Transactional;
import com.taskmanager.entity.PasswordResetToken;
import com.taskmanager.entity.User;
import com.taskmanager.repository.PasswordResetTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // POST /api/auth/forgot-password  { "email": "user@example.com"
    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());

        // Always return success to prevent email enumeration
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Delete any existing token for this user
            tokenRepository.deleteByUserId(user.getId());
            tokenRepository.flush();

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            tokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user, token);
        }

        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    // POST /api/auth/reset-password  { "token": "...", "newPassword": "..." }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token and new password are required"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired reset link"));
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Reset link has expired. Please request a new one."));
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully! You can now log in."));
    }

    // GET /api/auth/validate-reset-token?token=...
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isUsed() || tokenOpt.get().isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Invalid or expired link"));
        }
        return ResponseEntity.ok(Map.of("valid", true, "email", tokenOpt.get().getUser().getEmail()));
    }

    // GET /api/auth/test-email?email=test@gmail.com  (debug only — remove in production)
    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found with email: " + email));
            }
            emailService.sendBadgeEmail(userOpt.get(), "Test Badge", "🧪");
            return ResponseEntity.ok(Map.of("message", "Test email sent to " + email + " — check your inbox and spam!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage(), "cause", e.getCause() != null ? e.getCause().getMessage() : "unknown"));
        }
    }
}