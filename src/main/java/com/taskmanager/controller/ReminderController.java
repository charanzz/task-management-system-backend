package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public ReminderController(TaskRepository taskRepo, UserRepository userRepo, EmailService emailService) {
        this.taskRepo   = taskRepo;
        this.userRepo   = userRepo;
        this.emailService = emailService;
    }

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── GET upcoming reminders for user ──────────────────
    @GetMapping
    public ResponseEntity<?> getReminders(Authentication auth) {
        User user = getUser(auth);
        LocalDateTime now  = LocalDateTime.now();
        LocalDateTime in7d = now.plusDays(7);

        List<Task> upcoming = taskRepo.findByUserId(user.getId()).stream()
            .filter(t -> t.getDueDate() != null)
            .filter(t -> t.getStatus() != com.taskmanager.entity.TaskStatus.DONE)
            .filter(t -> t.getDueDate().isAfter(now.minusDays(1)))
            .filter(t -> t.getDueDate().isBefore(in7d))
            .sorted(Comparator.comparing(Task::getDueDate))
            .collect(Collectors.toList());

        List<Task> overdue = taskRepo.findByUserId(user.getId()).stream()
            .filter(t -> t.getDueDate() != null)
            .filter(t -> t.getStatus() != com.taskmanager.entity.TaskStatus.DONE)
            .filter(t -> t.getDueDate().isBefore(now))
            .sorted(Comparator.comparing(Task::getDueDate))
            .collect(Collectors.toList());

        List<Map<String,Object>> upcomingList = upcoming.stream().map(t -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",       t.getId());
            m.put("title",    t.getTitle());
            m.put("dueDate",  t.getDueDate().toString());
            m.put("priority", t.getPriority());
            m.put("status",   t.getStatus());
            long hoursLeft = java.time.Duration.between(now, t.getDueDate()).toHours();
            m.put("hoursLeft", hoursLeft);
            m.put("urgency", hoursLeft < 24 ? "TODAY" : hoursLeft < 48 ? "TOMORROW" : "UPCOMING");
            return m;
        }).collect(Collectors.toList());

        List<Map<String,Object>> overdueList = overdue.stream().map(t -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",       t.getId());
            m.put("title",    t.getTitle());
            m.put("dueDate",  t.getDueDate().toString());
            m.put("priority", t.getPriority());
            m.put("status",   t.getStatus());
            long daysOverdue = java.time.Duration.between(t.getDueDate(), now).toDays();
            m.put("daysOverdue", daysOverdue);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "upcoming", upcomingList,
            "overdue",  overdueList,
            "totalDue", upcoming.size(),
            "totalOverdue", overdue.size()
        ));
    }

    // ── Send immediate reminder email ─────────────────────
    @PostMapping("/send-now")
    public ResponseEntity<?> sendNow(@RequestBody Map<String,Object> body, Authentication auth) {
        User user = getUser(auth);
        Long taskId = Long.valueOf(body.get("taskId").toString());
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        String html = buildReminderEmail(user.getName(), task);
        emailService.sendEmail(user.getEmail(), "⏰ Reminder: " + task.getTitle(), html);

        return ResponseEntity.ok(Map.of("sent", true, "to", user.getEmail()));
    }

    // ── Set snooze (update dueDate) ───────────────────────
    @PostMapping("/{taskId}/snooze")
    public ResponseEntity<?> snooze(@PathVariable Long taskId,
                                     @RequestBody Map<String,Object> body,
                                     Authentication auth) {
        User user = getUser(auth);
        Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Forbidden");

        int hours = Integer.parseInt(body.getOrDefault("hours","1").toString());
        LocalDateTime newDue = LocalDateTime.now().plusHours(hours);
        task.setDueDate(newDue);
        taskRepo.save(task);

        return ResponseEntity.ok(Map.of("snoozed", true, "newDueDate", newDue.toString()));
    }

    private String buildReminderEmail(String name, Task task) {
        String due = task.getDueDate() != null
            ? task.getDueDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))
            : "No due date";
        return "<div style=\"font-family:sans-serif;max-width:480px;margin:0 auto;background:#0a0a0f;color:#f0f0f8;padding:32px;border-radius:16px\">"
            + "<h2 style=\"color:#a855f7;margin:0 0 8px\">⏰ Task Reminder</h2>"
            + "<p style=\"color:#9ca3af;margin:0 0 24px\">Hey " + name + "! Don\'t forget:</p>"
            + "<div style=\"background:#111118;border:1px solid rgba(168,85,247,.2);border-radius:12px;padding:20px;margin-bottom:24px\">"
            + "<h3 style=\"margin:0 0 8px;color:#f0f0f8\">" + task.getTitle() + "</h3>"
            + "<p style=\"margin:0;color:#9ca3af;font-size:14px\">Due: " + due + "</p>"
            + "<p style=\"margin:4px 0 0;color:#9ca3af;font-size:14px\">Priority: " + task.getPriority() + "</p>"
            + "</div>"
            + "<a href=\"https://www.todoperks.online\" style=\"display:inline-block;padding:12px 24px;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;text-decoration:none;border-radius:10px;font-weight:700\">Open TaskFlow</a>"
            + "</div>";
    }
}