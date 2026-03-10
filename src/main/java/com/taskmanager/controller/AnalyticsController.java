package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public AnalyticsController(TaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Full advanced analytics ───────────────────────────
    @GetMapping("/advanced")
    public ResponseEntity<?> advanced(Authentication auth) {
        User user   = getUser(auth);
        List<Task> all = taskRepo.findByUserId(user.getId());
        LocalDateTime now = LocalDateTime.now();

        // ── Productivity score (0-100) ──────────────────
        long total     = all.size();
        long done      = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long overdue   = all.stream().filter(t -> t.getDueDate() != null
            && t.getDueDate().isBefore(now) && t.getStatus() != TaskStatus.DONE).count();
        long highDone  = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE
            && t.getPriority() == TaskPriority.HIGH).count();
        long highTotal = all.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();

        int completionScore = total > 0 ? (int)(done * 100 / total) : 0;
        int overduepenalty  = total > 0 ? (int)(overdue * 20 / Math.max(total,1)) : 0;
        int highPrioBonus   = highTotal > 0 ? (int)(highDone * 20 / highTotal) : 0;
        int streak          = user.getStreak() != null ? Math.min(user.getStreak() * 2, 20) : 0;
        int productivityScore = Math.max(0, Math.min(100, completionScore - overduepenalty + highPrioBonus + streak));

        // ── Heatmap: tasks completed per day last 12 weeks ──
        LocalDate today   = LocalDate.now();
        LocalDate from84  = today.minusDays(83);
        Map<String, Integer> heatmap = new LinkedHashMap<>();
        for (int i = 83; i >= 0; i--) {
            heatmap.put(today.minusDays(i).toString(), 0);
        }
        all.stream()
            .filter(t -> t.getCompletedAt() != null)
            .filter(t -> t.getCompletedAt().toLocalDate().isAfter(from84.minusDays(1)))
            .forEach(t -> {
                String key = t.getCompletedAt().toLocalDate().toString();
                heatmap.merge(key, 1, Integer::sum);
            });

        // ── Daily trend: last 30 days created vs completed ──
        List<Map<String,Object>> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long created   = all.stream().filter(t -> t.getCreatedAt() != null
                && t.getCreatedAt().toLocalDate().equals(d)).count();
            long completed = all.stream().filter(t -> t.getCompletedAt() != null
                && t.getCompletedAt().toLocalDate().equals(d)).count();
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("date",      d.format(DateTimeFormatter.ofPattern("MMM d")));
            m.put("created",   created);
            m.put("completed", completed);
            trend.add(m);
        }

        // ── Completion by day of week ──────────────────────
        int[] byDow = new int[7];
        all.stream().filter(t -> t.getCompletedAt() != null).forEach(t -> {
            byDow[t.getCompletedAt().getDayOfWeek().getValue() % 7]++;
        });
        String[] dowLabels = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        List<Map<String,Object>> dowData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dowData.add(Map.of("day", dowLabels[i], "count", byDow[i]));
        }

        // ── Completion by hour of day ──────────────────────
        int[] byHour = new int[24];
        all.stream().filter(t -> t.getCompletedAt() != null).forEach(t -> {
            byHour[t.getCompletedAt().getHour()]++;
        });
        List<Map<String,Object>> hourData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String label = i == 0 ? "12am" : i < 12 ? i+"am" : i == 12 ? "12pm" : (i-12)+"pm";
            hourData.add(Map.of("hour", label, "count", byHour[i]));
        }

        // ── Priority breakdown ─────────────────────────────
        Map<String,Long> byPriority = new LinkedHashMap<>();
        byPriority.put("HIGH",   all.stream().filter(t->t.getPriority()==TaskPriority.HIGH).count());
        byPriority.put("MEDIUM", all.stream().filter(t->t.getPriority()==TaskPriority.MEDIUM).count());
        byPriority.put("LOW",    all.stream().filter(t->t.getPriority()==TaskPriority.LOW).count());

        // ── Avg completion time (hours) ───────────────────
        OptionalDouble avgHrs = all.stream()
            .filter(t -> t.getCompletedAt() != null && t.getCreatedAt() != null)
            .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toHours())
            .average();

        // ── Weekly velocity (tasks/week last 4 weeks) ─────
        List<Map<String,Object>> velocity = new ArrayList<>();
        for (int w = 3; w >= 0; w--) {
            LocalDate ws = today.minusDays((long)(w+1)*7);
            LocalDate we = today.minusDays((long)w*7);
            long cnt = all.stream().filter(t -> t.getCompletedAt() != null
                && !t.getCompletedAt().toLocalDate().isBefore(ws)
                && t.getCompletedAt().toLocalDate().isBefore(we)).count();
            velocity.add(Map.of("week","W-"+(w+1),"completed",cnt));
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("productivityScore", productivityScore);
        result.put("total",             total);
        result.put("done",              done);
        result.put("overdue",           overdue);
        result.put("completionRate",    completionScore);
        result.put("avgCompletionHrs",  avgHrs.isPresent() ? Math.round(avgHrs.getAsDouble()) : 0);
        result.put("heatmap",           heatmap);
        result.put("trend",             trend);
        result.put("byDayOfWeek",       dowData);
        result.put("byHourOfDay",       hourData);
        result.put("byPriority",        byPriority);
        result.put("velocity",          velocity);
        result.put("streak",            user.getStreak());
        result.put("focusScore",        user.getFocusScore());

        return ResponseEntity.ok(result);
    }
}