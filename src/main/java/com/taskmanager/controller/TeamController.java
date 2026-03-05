package com.taskmanager.controller;

import com.taskmanager.entity.*;
import com.taskmanager.repository.*;
import com.taskmanager.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final EmailService emailService;

    public TeamController(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
        UserRepository userRepository, TaskRepository taskRepository,
        TeamInviteRepository teamInviteRepository, EmailService emailService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.teamInviteRepository = teamInviteRepository;
        this.emailService = emailService;
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET /api/teams
    @GetMapping
    public ResponseEntity<?> getMyTeams(Authentication auth) {
        User user = getUser(auth);
        List<TeamMember> memberships = teamMemberRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = memberships.stream().map(m -> {
            Team team = m.getTeam();
            List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
            Map<String, Object> t = new HashMap<>();
            t.put("id", team.getId());
            t.put("name", team.getName());
            t.put("description", team.getDescription());
            t.put("memberCount", members.size());
            t.put("myRole", m.getRole());
            t.put("createdAt", team.getCreatedAt() != null ? team.getCreatedAt().toString() : null);
            t.put("members", members.stream().map(mem -> {
                Map<String, Object> mp = new HashMap<>();
                mp.put("id", mem.getUser().getId());
                mp.put("name", mem.getUser().getName());
                mp.put("email", mem.getUser().getEmail());
                mp.put("role", mem.getRole());
                return mp;
            }).collect(Collectors.toList()));
            return t;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // POST /api/teams
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Map<String, String> body, Authentication auth) {
        User user = getUser(auth);
        Team team = new Team();
        team.setName(body.getOrDefault("name", "My Team"));
        team.setDescription(body.getOrDefault("description", ""));
        team.setOwner(user);
        team.setCreatedAt(LocalDateTime.now());
        teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setRole("OWNER");
        teamMemberRepository.save(member);

        return ResponseEntity.ok(Map.of("id", team.getId(), "name", team.getName(), "message", "Team created!"));
    }

    // POST /api/teams/{id}/invite
    @PostMapping("/{id}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable Long id,
            @RequestBody Map<String, String> body, Authentication auth) {
        User user = getUser(auth);
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email required"));

        String token = UUID.randomUUID().toString();
        TeamInvite invite = new TeamInvite();
        invite.setTeam(team);
        invite.setEmail(email);
        invite.setToken(token);
        invite.setInvitedBy(user);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        teamInviteRepository.save(invite);

        String inviteLink = "https://www.todoperks.online/join-team?token=" + token;
        emailService.sendEmail(email,
            "You're invited to join " + team.getName() + " on TaskFlow",
            "<div style='font-family:sans-serif;max-width:500px;margin:0 auto;padding:32px;background:#111118;color:#f0f0f8;border-radius:16px'>" +
            "<h2 style='color:#a855f7'>🤝 Team Invitation</h2>" +
            "<p><strong>" + user.getName() + "</strong> invited you to join <strong>" + team.getName() + "</strong> on TaskFlow.</p>" +
            "<a href='" + inviteLink + "' style='display:inline-block;margin-top:20px;padding:12px 28px;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;border-radius:10px;text-decoration:none;font-weight:700'>Accept Invitation →</a>" +
            "<p style='margin-top:20px;font-size:12px;color:#6b6b8a'>Expires in 7 days</p></div>");

        return ResponseEntity.ok(Map.of("message", "Invitation sent to " + email));
    }

    // GET /api/teams/join?token=xxx
    @GetMapping("/join")
    public ResponseEntity<?> joinTeam(@RequestParam String token, Authentication auth) {
        User user = getUser(auth);
        TeamInvite invite = teamInviteRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired invite"));
        if (invite.getExpiresAt().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body(Map.of("error", "Invite expired"));

        boolean alreadyMember = teamMemberRepository.existsByTeamIdAndUserId(invite.getTeam().getId(), user.getId());
        if (!alreadyMember) {
            TeamMember member = new TeamMember();
            member.setTeam(invite.getTeam());
            member.setUser(user);
            member.setRole("MEMBER");
            teamMemberRepository.save(member);
        }
        teamInviteRepository.delete(invite);
        return ResponseEntity.ok(Map.of(
            "teamId", invite.getTeam().getId(),
            "teamName", invite.getTeam().getName(),
            "message", "Joined team!"
        ));
    }

    // GET /api/teams/{id}/tasks
    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTeamTasks(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        if (!teamMemberRepository.existsByTeamIdAndUserId(id, user.getId()))
            return ResponseEntity.status(403).body(Map.of("error", "Not a team member"));
        List<Task> tasks = taskRepository.findByTeamId(id);
        return ResponseEntity.ok(tasks);
    }

    // POST /api/teams/{id}/tasks
    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> createTeamTask(@PathVariable Long id,
            @RequestBody Map<String, Object> body, Authentication auth) {
        User user = getUser(auth);
        if (!teamMemberRepository.existsByTeamIdAndUserId(id, user.getId()))
            return ResponseEntity.status(403).body(Map.of("error", "Not a team member"));

        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));

        Task task = new Task();
        task.setTitle((String) body.getOrDefault("title", "Untitled"));
        task.setDescription((String) body.getOrDefault("description", ""));

        // ✅ Use TaskPriority and TaskStatus (separate enums, not nested)
        String priorityStr = (String) body.getOrDefault("priority", "MEDIUM");
        task.setPriority(TaskPriority.valueOf(priorityStr));
        task.setStatus(TaskStatus.TODO);

        task.setUser(user);
        task.setTeam(team);

        if (body.get("assigneeId") != null) {
            Long assigneeId = Long.parseLong(body.get("assigneeId").toString());
            userRepository.findById(assigneeId).ifPresent(task::setAssignee);
        }

        taskRepository.save(task);
        return ResponseEntity.ok(Map.of(
            "id", task.getId(),
            "title", task.getTitle(),
            "message", "Task created!"
        ));
    }

    // DELETE /api/teams/{id}/leave
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Long id, Authentication auth) {
        User user = getUser(auth);
        teamMemberRepository.deleteByTeamIdAndUserId(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Left team"));
    }
}