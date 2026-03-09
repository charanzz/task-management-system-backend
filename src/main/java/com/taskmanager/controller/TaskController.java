package com.taskmanager.controller;

import com.taskmanager.entity.*;
import com.taskmanager.repository.*;
import com.taskmanager.service.NotificationService;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskCommentRepository commentRepository;
    private final NotificationService notificationService;

    public TaskController(TaskService taskService, UserService userService,
                          TaskRepository taskRepository,
                          SubTaskRepository subTaskRepository,
                          TaskCommentRepository commentRepository,
                          NotificationService notificationService) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    private User getUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName());
    }

    // GET all tasks
    @GetMapping
    public ResponseEntity<?> getTasks(Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(taskRepository.findByUserId(user.getId()));
    }

    // GET single task
    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        return taskRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET stats
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication auth) {
        User user = getUser(auth);
        return ResponseEntity.ok(taskService.getUserStats(user.getId()));
    }

    // CREATE task
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Map<String,Object> body, Authentication auth) {
        User user = getUser(auth);
        Task task = new Task();
        task.setUser(user);
        task.setTitle((String) body.get("title"));
        task.setDescription((String) body.get("description"));
        if (body.get("priority") != null)
            task.setPriority(TaskPriority.valueOf((String) body.get("priority")));
        if (body.get("dueDate") != null)
            task.setDueDate(LocalDateTime.parse((String) body.get("dueDate")));
        if (body.get("recurring") != null)
            task.setRecurring((Boolean) body.get("recurring"));
        if (body.get("recurringInterval") != null)
            task.setRecurringInterval((String) body.get("recurringInterval"));
        if (body.get("tags") != null)
            task.setTags((String) body.get("tags"));
        return ResponseEntity.ok(taskService.createTask(task));
    }

    // UPDATE task
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                        @RequestBody Map<String,Object> body,
                                        Authentication auth) {
        User user = getUser(auth);
        Task task = taskRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (body.get("title") != null) task.setTitle((String) body.get("title"));
        if (body.get("description") != null) task.setDescription((String) body.get("description"));
        if (body.get("priority") != null)
            task.setPriority(TaskPriority.valueOf((String) body.get("priority")));
        if (body.get("status") != null) {
            TaskStatus s = TaskStatus.valueOf((String) body.get("status"));
            boolean wasNotDone = task.getStatus() != TaskStatus.DONE;
            task.setStatus(s);
            if (s == TaskStatus.DONE && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
                // Fire in-app notification
                if (wasNotDone) {
                    int pts = task.getPriority() != null ? switch(task.getPriority()) {
                        case HIGH -> 30; case MEDIUM -> 15; default -> 5;
                    } : 15;
                    try { notificationService.taskCompleted(task.getUser(), task, pts); } catch (Exception ignored) {}
                }
            }
            // Auto-recreate if recurring and just completed
            if (s == TaskStatus.DONE && Boolean.TRUE.equals(task.getRecurring())) {
                Task next = new Task();
                next.setUser(task.getUser());
                next.setTitle(task.getTitle());
                next.setDescription(task.getDescription());
                next.setPriority(task.getPriority());
                next.setRecurring(true);
                next.setRecurringInterval(task.getRecurringInterval());
                LocalDateTime nextDue = computeNextDue(task.getDueDate(), task.getRecurringInterval());
                next.setDueDate(nextDue);
                taskRepository.save(next);
            }
        }
        if (body.get("dueDate") != null) {
            String d = (String) body.get("dueDate");
            task.setDueDate(d.isEmpty() ? null : LocalDateTime.parse(d));
        }
        if (body.get("recurring") != null) task.setRecurring((Boolean) body.get("recurring"));
        if (body.get("recurringInterval") != null)
            task.setRecurringInterval((String) body.get("recurringInterval"));
        if (body.get("tags") != null) task.setTags((String) body.get("tags"));

        return ResponseEntity.ok(taskRepository.save(task));
    }

    // PATCH status only (for kanban drag & drop)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String,String> body,
                                          Authentication auth) {
        User user = getUser(auth);
        Task task = taskRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("Task not found"));
        TaskStatus s = TaskStatus.valueOf(body.get("status"));
        task.setStatus(s);
        if (s == TaskStatus.DONE && task.getCompletedAt() == null)
            task.setCompletedAt(LocalDateTime.now());
        return ResponseEntity.ok(taskRepository.save(task));
    }

    // DELETE task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        Task task = taskRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
        return ResponseEntity.ok(Map.of("message", "Task deleted"));
    }

    // ── SUBTASKS ──────────────────────────────────────────

    @GetMapping("/{id}/subtasks")
    public ResponseEntity<?> getSubTasks(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(subTaskRepository.findByTaskId(id));
    }

    @PostMapping("/{id}/subtasks")
    public ResponseEntity<?> addSubTask(@PathVariable Long id,
                                        @RequestBody Map<String,String> body,
                                        Authentication auth) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        SubTask sub = new SubTask();
        sub.setTitle(body.get("title"));
        sub.setTask(task);
        return ResponseEntity.ok(subTaskRepository.save(sub));
    }

    @PatchMapping("/{taskId}/subtasks/{subId}/toggle")
    public ResponseEntity<?> toggleSubTask(@PathVariable Long taskId,
                                           @PathVariable Long subId,
                                           Authentication auth) {
        SubTask sub = subTaskRepository.findById(subId)
            .orElseThrow(() -> new RuntimeException("Subtask not found"));
        sub.setCompleted(!sub.isCompleted());
        return ResponseEntity.ok(subTaskRepository.save(sub));
    }

    @DeleteMapping("/{taskId}/subtasks/{subId}")
    public ResponseEntity<?> deleteSubTask(@PathVariable Long taskId,
                                           @PathVariable Long subId,
                                           Authentication auth) {
        subTaskRepository.deleteById(subId);
        return ResponseEntity.ok(Map.of("message", "Subtask deleted"));
    }

    // ── COMMENTS ──────────────────────────────────────────

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody Map<String,String> body,
                                        Authentication auth) {
        User user = getUser(auth);
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        TaskComment c = new TaskComment();
        c.setText(body.get("text"));
        c.setTask(task);
        c.setAuthor(user);
        return ResponseEntity.ok(commentRepository.save(c));
    }

    // ── Export tasks as CSV ──────────────────────────────
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv(Authentication auth) {
        User user = getUser(auth);
        List<Task> tasks = taskRepository.findByUserId(user.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Title,Status,Priority,Due Date,Tags,Created At\n");
        for (Task t : tasks) {
            sb.append(csv(t.getTitle())).append(",");
            sb.append(t.getStatus()).append(",");
            sb.append(t.getPriority()).append(",");
            sb.append(t.getDueDate() != null ? t.getDueDate().toLocalDate() : "").append(",");
            sb.append(csv(t.getTags() != null ? t.getTags() : "")).append(",");
            sb.append(t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : "").append("\n");
        }

        return ResponseEntity.ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=taskflow-tasks.csv")
            .body(sb.toString());
    }

    private String csv(String val) {
        if (val == null) return "";
        char q = '"';
        if (val.indexOf(',') >= 0 || val.indexOf(q) >= 0 || val.indexOf('\n') >= 0)
            return q + val.replace(String.valueOf(q), String.valueOf(q) + q) + q;
        return val;
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long taskId,
                                           @PathVariable Long commentId,
                                           Authentication auth) {
        User user = getUser(auth);
        TaskComment c = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!c.getAuthor().getId().equals(user.getId()))
            return ResponseEntity.status(403).body(Map.of("error", "Not your comment"));
        commentRepository.delete(c);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ── HELPERS ───────────────────────────────────────────

    private LocalDateTime computeNextDue(LocalDateTime due, String interval) {
        if (due == null) due = LocalDateTime.now();
        if ("WEEKLY".equals(interval)) return due.plusWeeks(1);
        if ("MONTHLY".equals(interval)) return due.plusMonths(1);
        return due.plusDays(1); // DAILY default
    }
}