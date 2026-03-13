package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.SubTask;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ai-breakdown")
@CrossOrigin
public class AITaskBreakdownController {

    @Autowired private AIService aiService;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<?> breakdown(@RequestBody Map<String,String> body, Authentication auth) {
        String goal = body.getOrDefault("goal", "").trim();
        if (goal.isBlank()) return ResponseEntity.badRequest().body(Map.of("error","Goal is required"));

        String prompt = """
            You are a productivity assistant. The user wants to accomplish this goal:
            "%s"

            Break this down into 5-8 clear, actionable subtasks.
            Each subtask should be specific and completable in 1-2 hours.

            Respond ONLY with a JSON array. No explanation, no markdown, no backticks.
            Format:
            [
              {"title":"subtask title","priority":"HIGH","estimatedHours":1},
              ...
            ]
            Priority must be exactly: HIGH, MEDIUM, or LOW
            """.formatted(goal);

        try {
            String raw = aiService.callClaudeRaw(prompt);
            raw = raw.replaceAll("```json|```", "").trim();
            return ResponseEntity.ok(Map.of("subtasks", raw, "goal", goal));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "AI service error: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Map<String,Object> body, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        String goal = (String) body.getOrDefault("goal", "New Goal");

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> subtasks = (List<Map<String,Object>>) body.get("subtasks");

        // Create parent task
        Task parent = new Task();
        parent.setTitle(goal);
        parent.setUser(user);
        parent.setPriority(TaskPriority.HIGH);
        parent.setStatus(TaskStatus.TODO);
        parent.setDueDate(LocalDateTime.now().plusDays(7));
        parent.setDescription("AI-generated breakdown — "
            + (subtasks != null ? subtasks.size() : 0) + " steps");
        taskRepository.save(parent);

        // Save subtasks as SubTask entities linked to parent
        if (subtasks != null) {
            List<SubTask> subList = new ArrayList<>();
            for (Map<String,Object> st : subtasks) {
                SubTask sub = new SubTask();
                sub.setTitle((String) st.getOrDefault("title", "Subtask"));
                sub.setCompleted(false);
                sub.setTask(parent);
                subList.add(sub);
            }
            parent.setSubTasks(subList);
            taskRepository.save(parent);
        }

        return ResponseEntity.ok(Map.of("message", "Saved successfully", "taskId", parent.getId()));
    }
}