package com.taskmanager.controller;

import com.taskmanager.entity.Habit;
import com.taskmanager.entity.HabitLog;
import com.taskmanager.entity.User;
import com.taskmanager.repository.HabitLogRepository;
import com.taskmanager.repository.HabitRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepo;
    private final HabitLogRepository logRepo;
    private final UserRepository userRepo;

    public HabitController(HabitRepository habitRepo, HabitLogRepository logRepo, UserRepository userRepo) {
        this.habitRepo = habitRepo;
        this.logRepo   = logRepo;
        this.userRepo  = userRepo;
    }

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── GET all habits with today's status & last 7 days ──
    @GetMapping
    public ResponseEntity<?> getHabits(Authentication auth) {
        User user = getUser(auth);
        List<Habit> habits = habitRepo.findByUserIdAndArchivedFalse(user.getId());
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        List<Map<String, Object>> result = habits.stream().map(h -> {
            List<HabitLog> logs = logRepo.findByHabitIdAndLogDateBetween(h.getId(), weekAgo, today);
            Set<LocalDate> doneDates = logs.stream().map(HabitLog::getLogDate).collect(Collectors.toSet());

            boolean doneToday = doneDates.contains(today);

            // Build last 7 days array
            List<Map<String, Object>> week = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", d.toString());
                day.put("done", doneDates.contains(d));
                week.add(day);
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            h.getId());
            m.put("title",         h.getTitle());
            m.put("emoji",         h.getEmoji() != null ? h.getEmoji() : "✅");
            m.put("color",         h.getColor() != null ? h.getColor() : "#7c3aed");
            m.put("frequency",     h.getFrequency());
            m.put("currentStreak", h.getCurrentStreak());
            m.put("longestStreak", h.getLongestStreak());
            m.put("doneToday",     doneToday);
            m.put("week",          week);
            m.put("totalDone",     logRepo.findByHabitId(h.getId()).size());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── CREATE habit ──────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = getUser(auth);
        Habit h = new Habit();
        h.setTitle(body.getOrDefault("title","New Habit").toString());
        h.setEmoji(body.getOrDefault("emoji","✅").toString());
        h.setColor(body.getOrDefault("color","#7c3aed").toString());
        h.setFrequency(body.getOrDefault("frequency","DAILY").toString());
        h.setUser(user);
        habitRepo.save(h);
        return ResponseEntity.ok(Map.of("id", h.getId(), "title", h.getTitle()));
    }

    // ── TOGGLE today's completion ─────────────────────────
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        if (!habit.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Forbidden");

        LocalDate today = LocalDate.now();
        Optional<HabitLog> existing = logRepo.findByHabitIdAndLogDate(id, today);

        boolean nowDone;
        if (existing.isPresent()) {
            logRepo.delete(existing.get());
            nowDone = false;
        } else {
            HabitLog log = new HabitLog();
            log.setLogDate(today);
            log.setHabit(habit);
            logRepo.save(log);
            nowDone = true;
        }

        // Recalculate streak
        recalcStreak(habit);
        habitRepo.save(habit);

        return ResponseEntity.ok(Map.of("doneToday", nowDone, "streak", habit.getCurrentStreak()));
    }

    // ── DELETE habit ──────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Habit not found"));
        if (!habit.getUser().getId().equals(user.getId()))
            return ResponseEntity.status(403).body("Forbidden");
        habit.setArchived(true);
        habitRepo.save(habit);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── Streak recalculation ──────────────────────────────
    private void recalcStreak(Habit habit) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate check = today;
        while (true) {
            Optional<HabitLog> log = logRepo.findByHabitIdAndLogDate(habit.getId(), check);
            if (log.isEmpty()) break;
            streak++;
            check = check.minusDays(1);
        }
        habit.setCurrentStreak(streak);
        if (streak > (habit.getLongestStreak() != null ? habit.getLongestStreak() : 0)) {
            habit.setLongestStreak(streak);
        }
    }
}