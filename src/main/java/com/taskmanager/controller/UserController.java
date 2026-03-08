package com.taskmanager.controller;

import com.taskmanager.dto.LoginRequest;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.dto.UserStats;
import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.service.EmailService;
import com.taskmanager.service.GamificationService;
import com.taskmanager.service.TaskService;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.entity.Task;
import com.taskmanager.service.UserService;
import com.taskmanager.config.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TaskService taskService;
    private final GamificationService gamificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          JwtUtil jwtUtil,
                          TaskService taskService,
                          GamificationService gamificationService,
                          EmailService emailService,
                          UserRepository userRepository,
                          TaskRepository taskRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.taskService = taskService;
        this.gamificationService = gamificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
            if (request.getEmail() == null || request.getEmail().trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            if (request.getPassword() == null || request.getPassword().length() < 6)
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));

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
        User user = userService.login(request.getEmail(), request.getPassword());

        // ✅ Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userService.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", user.getEmail(),
            "name", user.getName(),
            "id", user.getId(),
            "role", user.getRole() != null ? user.getRole().name() : "USER",
            "isPro", Boolean.TRUE.equals(user.getIsPro())
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole() != null ? user.getRole().name() : "USER",
            "isPro", Boolean.TRUE.equals(user.getIsPro())
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getMyStats(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        UserStats stats = taskService.getUserStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/badges")
    public ResponseEntity<?> getMyBadges(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(gamificationService.getUserBadges(user.getId()));
    }

    @GetMapping("/level")
    public ResponseEntity<?> getMyLevel(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        UserStats stats = taskService.getUserStats(user.getId());
        int level = gamificationService.getUserLevel(stats.getFocusScore());
        return ResponseEntity.ok(Map.of(
            "level", level,
            "focusScore", stats.getFocusScore(),
            "nextLevelAt", level < 10 ? (level * 100) : 1000
        ));
    }

    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            emailService.sendBadgeEmail(user, "Test Badge", "🧪");
            return ResponseEntity.ok(Map.of("message", "Test email sent to " + user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test")
    public String test() {
        return "Users API working!";
    }

    // ── GET full profile ──────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        long total     = tasks.size();
        long completed = tasks.stream().filter(t -> "DONE".equals(String.valueOf(t.getStatus()))).count();
        long overdue   = tasks.stream().filter(t -> t.getDueDate() != null
            && t.getDueDate().isBefore(java.time.LocalDateTime.now())
            && !"DONE".equals(String.valueOf(t.getStatus()))).count();

        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("id",             user.getId());
        resp.put("name",           user.getName() != null ? user.getName() : "");
        resp.put("email",          user.getEmail());
        resp.put("bio",            user.getBio() != null ? user.getBio() : "");
        resp.put("avatarColor",    user.getAvatarColor() != null ? user.getAvatarColor() : "#7c3aed");
        resp.put("timezone",       user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata");
        resp.put("isPro",          user.getIsPro());
        resp.put("role",           user.getRole() != null ? user.getRole().name() : "USER");
        resp.put("focusScore",     user.getFocusScore());
        resp.put("streak",         user.getStreak());
        resp.put("createdAt",      user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        resp.put("lastLoginAt",    user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : "");
        resp.put("totalTasks",     total);
        resp.put("completedTasks", completed);
        resp.put("overdueTasks",   overdue);
        return ResponseEntity.ok(resp);
    }

    // ── UPDATE profile (name, bio, avatarColor, timezone) ─
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.containsKey("name") && !body.get("name").isBlank())
            user.setName(body.get("name").trim());
        if (body.containsKey("bio"))
            user.setBio(body.get("bio").trim());
        if (body.containsKey("avatarColor"))
            user.setAvatarColor(body.get("avatarColor"));
        if (body.containsKey("timezone"))
            user.setTimezone(body.get("timezone"));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated!", "name", user.getName()));
    }

    // ── CHANGE password ───────────────────────────────────
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String current = body.get("currentPassword");
        String newPass = body.get("newPassword");

        if (current == null || newPass == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Both passwords required"));
        if (newPass.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters"));
        if (!passwordEncoder.matches(current, user.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));

        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully!"));
    }

    // ── Onboarding status ────────────────────────────────
    @GetMapping("/onboarding-status")
    public ResponseEntity<?> onboardingStatus(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Map.of(
            "done", user.getOnboardingDone(),
            "name", user.getName() != null ? user.getName() : "",
            "hasAvatar", user.getAvatarColor() != null
        ));
    }

    @PostMapping("/complete-onboarding")
    public ResponseEntity<?> completeOnboarding(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.containsKey("name") && body.get("name") != null)
            user.setName(body.get("name").toString().trim());
        if (body.containsKey("avatarColor"))
            user.setAvatarColor(body.get("avatarColor").toString());
        if (body.containsKey("timezone"))
            user.setTimezone(body.get("timezone").toString());
        if (body.containsKey("bio"))
            user.setBio(body.get("bio").toString());

        user.setOnboardingDone(true);
        userRepository.save(user);

        // Create welcome task
        if (body.containsKey("firstTask") && body.get("firstTask") != null) {
            String taskTitle = body.get("firstTask").toString().trim();
            if (!taskTitle.isBlank()) {
                Task t = new Task();
                t.setTitle(taskTitle);
                t.setPriority(com.taskmanager.entity.TaskPriority.HIGH);
                t.setStatus(com.taskmanager.entity.TaskStatus.TODO);
                t.setUser(user);
                taskRepository.save(t);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Onboarding complete!", "name", user.getName()));
    }

    // ── DELETE account ────────────────────────────────────
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String password = body.get("password");
        if (password == null || !passwordEncoder.matches(password, user.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Incorrect password"));

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }

}