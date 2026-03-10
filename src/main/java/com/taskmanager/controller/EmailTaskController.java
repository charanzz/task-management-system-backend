package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/email-tasks")
public class EmailTaskController {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final AIService aiService;

    public EmailTaskController(TaskRepository taskRepo, UserRepository userRepo, AIService aiService) {
        this.taskRepo  = taskRepo;
        this.userRepo  = userRepo;
        this.aiService = aiService;
    }

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── GET user's unique email-to-task address ───────────
    @GetMapping("/address")
    public ResponseEntity<?> getAddress(Authentication auth) {
        User user = getUser(auth);
        // Generate deterministic unique token from user id
        String token = "task-" + Long.toHexString(user.getId() * 0x9e3779b97f4a7c15L).substring(0, 8);
        String address = token + "@todoperks.online";
        return ResponseEntity.ok(Map.of(
            "address", address,
            "token",   token,
            "instructions", "Forward any email to this address to auto-create a task. The subject becomes the task title."
        ));
    }

    // ── Create task from email content (called from UI "paste email") ──
    @PostMapping("/parse")
    public ResponseEntity<?> parseEmail(@RequestBody Map<String,Object> body, Authentication auth) {
        User user = getUser(auth);

        String subject = body.getOrDefault("subject", "").toString().trim();
        String emailBody = body.getOrDefault("body", "").toString().trim();

        if (subject.isBlank() && emailBody.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Subject or body required"));

        // Use AI to parse the email into a task
        String prompt = "Parse this email and extract a task. Return JSON only with fields: " +
            "title (string), description (string, brief), priority (HIGH/MEDIUM/LOW), " +
            "dueDate (ISO date string or null).\n\n" +
            "Subject: " + subject + "\nBody: " + emailBody.substring(0, Math.min(emailBody.length(), 500));

        try {
            String aiResponse = aiService.callClaudeRaw(prompt);
            // Parse AI response
            aiResponse = aiResponse.replaceAll("```json|```", "").trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String,Object> parsed = mapper.readValue(aiResponse, Map.class);

            Task task = new Task();
            task.setTitle(parsed.getOrDefault("title", subject.isBlank() ? "Email Task" : subject).toString());
            task.setDescription(parsed.getOrDefault("description", emailBody.substring(0, Math.min(emailBody.length(), 300))).toString());
            task.setStatus(TaskStatus.TODO);
            task.setTags("email");

            String prio = parsed.getOrDefault("priority", "MEDIUM").toString();
            try { task.setPriority(TaskPriority.valueOf(prio)); }
            catch(Exception e) { task.setPriority(TaskPriority.MEDIUM); }

            Object dueDateObj = parsed.get("dueDate");
            if (dueDateObj != null && !dueDateObj.toString().equals("null")) {
                try { task.setDueDate(LocalDateTime.parse(dueDateObj.toString())); }
                catch(Exception e) {}
            }

            task.setUser(user);
            taskRepo.save(task);

            Map<String,Object> resp = new LinkedHashMap<>();
            resp.put("created",     true);
            resp.put("taskId",      task.getId());
            resp.put("title",       task.getTitle());
            resp.put("description", task.getDescription());
            resp.put("priority",    task.getPriority());
            resp.put("dueDate",     task.getDueDate());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            // Fallback: create task directly from subject
            Task task = new Task();
            task.setTitle(subject.isBlank() ? "Email Task" : subject);
            task.setDescription(emailBody.substring(0, Math.min(emailBody.length(), 500)));
            task.setStatus(TaskStatus.TODO);
            task.setPriority(TaskPriority.MEDIUM);
            task.setTags("email");
            task.setUser(user);
            taskRepo.save(task);
            return ResponseEntity.ok(Map.of("created", true, "taskId", task.getId(),
                "title", task.getTitle(), "fallback", true));
        }
    }

    // ── Webhook for inbound email (Resend/SendGrid webhook) ──
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody Map<String,Object> body) {
        try {
            String to      = body.getOrDefault("to", "").toString();
            String subject = body.getOrDefault("subject", "Email Task").toString();
            String text    = body.getOrDefault("text", "").toString();

            // Extract token from "to" address
            String token = to.split("@")[0]; // e.g. "task-a1b2c3d4"
            if (!token.startsWith("task-")) return ResponseEntity.ok(Map.of("ignored", true));

            // Find user by token (reverse engineer from hex)
            List<User> users = userRepo.findAll();
            User matched = null;
            for (User u : users) {
                String expected = "task-" + Long.toHexString(u.getId() * 0x9e3779b97f4a7c15L).substring(0, 8);
                if (expected.equals(token)) { matched = u; break; }
            }
            if (matched == null) return ResponseEntity.ok(Map.of("error", "User not found"));

            Task task = new Task();
            task.setTitle(subject.length() > 200 ? subject.substring(0, 200) : subject);
            task.setDescription(text.length() > 1000 ? text.substring(0, 1000) : text);
            task.setStatus(TaskStatus.TODO);
            task.setPriority(TaskPriority.MEDIUM);
            task.setTags("email,inbox");
            task.setUser(matched);
            taskRepo.save(task);

            return ResponseEntity.ok(Map.of("created", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }
}