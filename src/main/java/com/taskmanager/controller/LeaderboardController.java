package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.Team;
import com.taskmanager.entity.TeamMember;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.TeamMemberRepository;
import com.taskmanager.repository.TeamRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public LeaderboardController(UserRepository userRepository,
                                  TaskRepository taskRepository,
                                  TeamRepository teamRepository,
                                  TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Global leaderboard (top 20 users by focus score) ──
    @GetMapping("/global")
    public ResponseEntity<?> globalLeaderboard(Authentication auth) {
        User me = getUser(auth);
        List<User> all = userRepository.findAll();

        List<Map<String, Object>> board = all.stream()
            .sorted(Comparator.comparingInt((User u) ->
                u.getFocusScore() != null ? u.getFocusScore() : 0).reversed())
            .limit(20)
            .map(u -> buildEntry(u, me))
            .collect(Collectors.toList());

        // Find my rank
        int myRank = 1;
        for (User u : all.stream()
                .sorted(Comparator.comparingInt((User u2) ->
                    u2.getFocusScore() != null ? u2.getFocusScore() : 0).reversed())
                .collect(Collectors.toList())) {
            if (u.getId().equals(me.getId())) break;
            myRank++;
        }

        return ResponseEntity.ok(Map.of(
            "board",  board,
            "myRank", myRank,
            "total",  all.size()
        ));
    }

    // ── Team leaderboard ──────────────────────────────────
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> teamLeaderboard(@PathVariable Long teamId, Authentication auth) {
        User me = getUser(auth);

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        List<Map<String, Object>> board = members.stream()
            .map(tm -> tm.getUser())
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt((User u) ->
                u.getFocusScore() != null ? u.getFocusScore() : 0).reversed())
            .map(u -> buildEntry(u, me))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("board", board, "teamId", teamId));
    }

    // ── Weekly leaderboard (tasks completed this week) ───
    @GetMapping("/weekly")
    public ResponseEntity<?> weeklyLeaderboard(Authentication auth) {
        User me = getUser(auth);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<User> all = userRepository.findAll();

        List<Map<String, Object>> board = all.stream()
            .map(u -> {
                List<Task> tasks = taskRepository.findByUserId(u.getId());
                long weeklyDone = tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().isAfter(weekAgo))
                    .count();
                Map<String, Object> entry = buildEntry(u, me);
                entry.put("weeklyCompleted", weeklyDone);
                return entry;
            })
            .sorted(Comparator.comparingLong((Map<String,Object> e) ->
                (Long) e.get("weeklyCompleted")).reversed())
            .limit(20)
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("board", board));
    }

    private Map<String, Object> buildEntry(User u, User me) {
        int score  = u.getFocusScore() != null ? u.getFocusScore() : 0;
        int streak = u.getStreak()     != null ? u.getStreak()     : 0;
        int level  = Math.max(1, score / 100 + 1);
        String color = u.getAvatarColor() != null ? u.getAvatarColor() : "#7c3aed";

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          u.getId());
        m.put("name",        u.getName() != null ? u.getName() : "Anonymous");
        m.put("focusScore",  score);
        m.put("streak",      streak);
        m.put("level",       Math.min(level, 10));
        m.put("isPro",       u.getIsPro() != null && u.getIsPro());
        m.put("avatarColor", color);
        m.put("isMe",        u.getId().equals(me.getId()));
        return m;
    }
}