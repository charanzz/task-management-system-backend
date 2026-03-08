package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.AIService;
import com.taskmanager.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public AIController(AIService aiService, UserRepository userRepository,
                        TaskRepository taskRepository, EmailService emailService) {
        this.aiService = aiService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // POST /api/ai/suggest-priority
    @PostMapping("/suggest-priority")
    public ResponseEntity<?> suggestPriority(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            String title = body.getOrDefault("title", "");
            if (title.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Title required"));
            return ResponseEntity.ok(aiService.suggestPriority(title, body.getOrDefault("description", "")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/parse-task
    @PostMapping("/parse-task")
    public ResponseEntity<?> parseTask(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            String input = body.getOrDefault("input", "");
            if (input.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Input required"));
            return ResponseEntity.ok(aiService.parseNaturalLanguage(input));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/ai/daily-digest
    @GetMapping("/daily-digest")
    public ResponseEntity<?> getDailyDigest(Authentication auth) {
        try {
            User user = getUser(auth);
            List<Task> tasks = taskRepository.findByUserId(user.getId());
            return ResponseEntity.ok(Map.of("digest", aiService.generateDailyDigest(user, tasks)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/send-daily-digest
    @PostMapping("/send-daily-digest")
    public ResponseEntity<?> sendDailyDigest(Authentication auth) {
        try {
            User user = getUser(auth);
            List<Task> tasks = taskRepository.findByUserId(user.getId());
            String digest = aiService.generateDailyDigest(user, tasks);
            emailService.sendAIDigestEmail(user, digest, "Daily");
            return ResponseEntity.ok(Map.of("message", "Daily digest sent to " + user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/ai/weekly-coach
    @GetMapping("/weekly-coach")
    public ResponseEntity<?> getWeeklyCoach(Authentication auth) {
        try {
            User user = getUser(auth);
            List<Task> tasks = taskRepository.findByUserId(user.getId());
            int focusScore = user.getFocusScore() != null ? user.getFocusScore() : 0;
            int streak = user.getStreak() != null ? user.getStreak() : 0;
            return ResponseEntity.ok(Map.of("tips", aiService.generateWeeklyCoach(user, tasks, focusScore, streak)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/send-weekly-coach
    @PostMapping("/send-weekly-coach")
    public ResponseEntity<?> sendWeeklyCoach(Authentication auth) {
        try {
            User user = getUser(auth);
            List<Task> tasks = taskRepository.findByUserId(user.getId());
            int focusScore = user.getFocusScore() != null ? user.getFocusScore() : 0;
            int streak = user.getStreak() != null ? user.getStreak() : 0;
            String tips = aiService.generateWeeklyCoach(user, tasks, focusScore, streak);
            emailService.sendAIDigestEmail(user, tips, "Weekly Coach");
            return ResponseEntity.ok(Map.of("message", "Weekly coach sent to " + user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Daily Focus Mode ─────────────────────────────────
    // GET /api/ai/daily-focus
    @GetMapping("/daily-focus")
    public ResponseEntity<?> getDailyFocus(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        List<Map<String,Object>> focus = aiService.getDailyFocusTasks(user, tasks);
        return ResponseEntity.ok(Map.of(
            "date",       java.time.LocalDate.now().toString(),
            "userName",   user.getName() != null ? user.getName() : "there",
            "focusTasks", focus,
            "streak",     user.getStreak() != null ? user.getStreak() : 0,
            "focusScore", user.getFocusScore() != null ? user.getFocusScore() : 0
        ));
    }

    // ── Weekly Review data ───────────────────────────────
    // GET /api/ai/weekly-review
    @GetMapping("/weekly-review")
    public ResponseEntity<?> getWeeklyReview(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        Map<String,Object> data = aiService.generateWeeklyReviewData(user, tasks);
        return ResponseEntity.ok(data);
    }

    // ── Natural Language task parse (already exists as /parse-task) ──
    // POST /api/ai/parse-task  { "input": "call mom tomorrow 3pm high priority" }
    // → already implemented, frontend just needs UI upgrade

}