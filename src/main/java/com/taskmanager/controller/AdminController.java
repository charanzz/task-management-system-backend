package com.taskmanager.controller;

import com.taskmanager.entity.User;
import com.taskmanager.entity.Role;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    private void checkAdmin(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.ADMIN)
            throw new RuntimeException("Access denied");
    }

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication auth) {
        try {
            checkAdmin(auth);
            List<User> allUsers = userRepository.findAll();
            long totalUsers = allUsers.size();
            long activeUsers7d = allUsers.stream()
                .filter(u -> u.getLastLoginAt() != null &&
                    u.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(7)))
                .count();
            long activeUsers30d = allUsers.stream()
                .filter(u -> u.getLastLoginAt() != null &&
                    u.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(30)))
                .count();
            long proUsers = allUsers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsPro())).count();
            long bannedUsers = allUsers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsBanned())).count();
            long totalTasks = taskRepository.count();

            // User growth last 30 days
            List<Map<String, Object>> userGrowth = new ArrayList<>();
            for (int i = 29; i >= 0; i--) {
                LocalDateTime day = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime dayEnd = day.plusDays(1);
                long count = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null &&
                        u.getCreatedAt().isAfter(day) &&
                        u.getCreatedAt().isBefore(dayEnd))
                    .count();
                Map<String, Object> pt = new HashMap<>();
                pt.put("date", day.toLocalDate().toString());
                pt.put("count", count);
                userGrowth.add(pt);
            }

            // Tasks per day last 14 days
            List<Map<String, Object>> tasksPerDay = new ArrayList<>();
            for (int i = 13; i >= 0; i--) {
                LocalDateTime day = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime dayEnd = day.plusDays(1);
                long count = taskRepository.countByCreatedAtBetween(day, dayEnd);
                Map<String, Object> pt = new HashMap<>();
                pt.put("date", day.toLocalDate().toString());
                pt.put("count", count);
                tasksPerDay.add(pt);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalUsers", totalUsers);
            result.put("activeUsers7d", activeUsers7d);
            result.put("activeUsers30d", activeUsers30d);
            result.put("proUsers", proUsers);
            result.put("bannedUsers", bannedUsers);
            result.put("totalTasks", totalTasks);
            result.put("userGrowth", userGrowth);
            result.put("tasksPerDay", tasksPerDay);
            result.put("estimatedRevenue", proUsers * 9);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(Authentication auth) {
        try {
            checkAdmin(auth);
            List<Map<String, Object>> users = userRepository.findAll().stream()
                .sorted(Comparator.comparing(u -> u.getCreatedAt() != null ? u.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole() != null ? u.getRole().name() : "USER");
                    m.put("isPro", Boolean.TRUE.equals(u.getIsPro()));
                    m.put("isBanned", Boolean.TRUE.equals(u.getIsBanned()));
                    m.put("focusScore", u.getFocusScore());
                    m.put("streak", u.getStreak());
                    m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    m.put("lastLoginAt", u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : null);
                    m.put("taskCount", taskRepository.countByUserId(u.getId()));
                    return m;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/admin/users/{id}/ban
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long id, Authentication auth) {
        try {
            checkAdmin(auth);
            User target = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            if (target.getRole() == Role.ADMIN) return ResponseEntity.badRequest().body(Map.of("error", "Cannot ban admin"));
            target.setIsBanned(!Boolean.TRUE.equals(target.getIsBanned()));
            userRepository.save(target);
            return ResponseEntity.ok(Map.of("banned", target.getIsBanned(), "message",
                Boolean.TRUE.equals(target.getIsBanned()) ? "User banned" : "User unbanned"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/admin/users/{id}/make-admin
    @PostMapping("/users/{id}/make-admin")
    public ResponseEntity<?> makeAdmin(@PathVariable Long id, Authentication auth) {
        try {
            checkAdmin(auth);
            User target = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            target.setRole(target.getRole() == Role.ADMIN ? Role.USER : Role.ADMIN);
            userRepository.save(target);
            return ResponseEntity.ok(Map.of("role", target.getRole().name()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/admin/users/{id}/pro
    @PostMapping("/users/{id}/pro")
    public ResponseEntity<?> togglePro(@PathVariable Long id, Authentication auth) {
        try {
            checkAdmin(auth);
            User target = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            target.setIsPro(!Boolean.TRUE.equals(target.getIsPro()));
            userRepository.save(target);
            return ResponseEntity.ok(Map.of("isPro", target.getIsPro()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}