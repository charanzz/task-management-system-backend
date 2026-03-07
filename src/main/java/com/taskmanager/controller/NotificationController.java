package com.taskmanager.controller;

import com.taskmanager.entity.Notification;
import com.taskmanager.entity.User;
import com.taskmanager.repository.NotificationRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repo;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET all notifications (latest 50)
    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth) {
        User user = getUser(auth);
        List<Notification> all = repo.findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(all.stream().limit(50).toList());
    }

    // GET unread count only (for bell badge — lightweight poll)
    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(Authentication auth) {
        User user = getUser(auth);
        long count = repo.countByUserIdAndReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // PATCH mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        return repo.findById(id)
            .filter(n -> n.getUser().getId().equals(user.getId()))
            .map(n -> {
                n.setRead(true);
                return ResponseEntity.ok(repo.save(n));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // PATCH mark ALL as read
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        User user = getUser(auth);
        repo.markAllReadForUser(user.getId());
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    // DELETE one notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        return repo.findById(id)
            .filter(n -> n.getUser().getId().equals(user.getId()))
            .map(n -> {
                repo.delete(n);
                return ResponseEntity.ok(Map.of("message", "Deleted"));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE all notifications
    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAll(Authentication auth) {
        User user = getUser(auth);
        List<Notification> all = repo.findByUserIdOrderByCreatedAtDesc(user.getId());
        repo.deleteAll(all);
        return ResponseEntity.ok(Map.of("message", "Cleared"));
    }
}