package com.taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${ANTHROPIC_API_KEY:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    private String callClaude(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) throw new RuntimeException("ANTHROPIC_API_KEY not set");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 1024);
            body.put("system", systemPrompt);
            body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(API_URL, req, String.class);
            JsonNode root = mapper.readTree(res.getBody());
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            System.err.println("Claude API error: " + e.getMessage());
            throw new RuntimeException("AI unavailable: " + e.getMessage());
        }
    }

    // 1. Smart Priority Suggestion
    public Map<String, String> suggestPriority(String title, String description) {
        String system = """
            You are a productivity assistant. Analyze task titles/descriptions and suggest priority.
            Respond ONLY with valid JSON, no markdown, no extra text.
            Format: {"priority": "HIGH|MEDIUM|LOW", "reason": "one short sentence"}
            Rules:
            - HIGH: urgent, deadline, critical, bug, payment, security, client-facing
            - MEDIUM: important but not urgent, features, improvements
            - LOW: nice-to-have, learning, optional, someday
            """;
        try {
            String raw = callClaude(system,
                "Task: " + title + (description != null && !description.isBlank() ? "\nDesc: " + description : ""))
                .trim().replaceAll("```json|```", "").trim();
            JsonNode node = mapper.readTree(raw);
            return Map.of(
                "priority", node.path("priority").asText("MEDIUM"),
                "reason",   node.path("reason").asText("Based on task content")
            );
        } catch (Exception e) {
            return Map.of("priority", "MEDIUM", "reason", "Could not analyze task");
        }
    }

    // 2. Natural Language Task Parser
    public Map<String, Object> parseNaturalLanguage(String input) {
        String system = """
            You are a task parser. Convert natural language to structured task data.
            Respond ONLY with valid JSON, no markdown, no extra text.
            Format:
            {
              "title": "clean concise task title",
              "description": "any extra details or empty string",
              "priority": "HIGH|MEDIUM|LOW",
              "dueDate": "YYYY-MM-DDTHH:mm:ss or null",
              "status": "TODO"
            }
            Today: """ + java.time.LocalDate.now() + """

            Date rules:
            - "tomorrow" = tomorrow 09:00:00
            - "today" = today 23:59:00
            - "next monday" = next monday 09:00:00
            - "in X days" = X days from today 09:00:00
            - "at 3pm" = use that time
            - No date = null
            """;
        try {
            String raw = callClaude(system, "Parse: " + input)
                .trim().replaceAll("```json|```", "").trim();
            JsonNode node = mapper.readTree(raw);
            Map<String, Object> result = new HashMap<>();
            result.put("title",       node.path("title").asText(input));
            result.put("description", node.path("description").asText(""));
            result.put("priority",    node.path("priority").asText("MEDIUM"));
            result.put("status",      "TODO");
            String due = node.path("dueDate").isNull() ? null : node.path("dueDate").asText(null);
            result.put("dueDate", due);
            return result;
        } catch (Exception e) {
            return Map.of("title", input, "description", "", "priority", "MEDIUM", "status", "TODO", "dueDate", "");
        }
    }

    // 3. Daily Digest
    public String generateDailyDigest(User user, List<Task> tasks) {
        long done    = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        long pending = tasks.stream().filter(t -> !"DONE".equals(t.getStatus())).count();
        long overdue = tasks.stream().filter(t ->
            t.getDueDate() != null &&
            t.getDueDate().isBefore(java.time.LocalDateTime.now()) &&
            !"DONE".equals(t.getStatus())).count();
        long highPri = tasks.stream().filter(t ->
            t.getPriority() != null &&
            "HIGH".equals(t.getPriority().name()) &&
            !"DONE".equals(t.getStatus())).count();

        String taskList = tasks.stream()
            .filter(t -> !"DONE".equals(t.getStatus()))
            .limit(5)
            .map(t -> "- " + t.getTitle() + " [" + t.getPriority() + "]" +
                (t.getDueDate() != null ? " due " + t.getDueDate().toLocalDate() : ""))
            .reduce("", (a, b) -> a + "\n" + b);

        try {
            return callClaude("""
                You are a friendly productivity coach for TaskFlow.
                Write a short motivating daily digest. Be encouraging and actionable.
                Max 3 short paragraphs. Use emojis sparingly. Sound human.
                """,
                String.format("User: %s\nCompleted: %d\nPending: %d\nOverdue: %d\nHigh priority: %d\nTop tasks:%s",
                    user.getName(), done, pending, overdue, highPri, taskList));
        } catch (Exception e) {
            return "Great work today, " + user.getName() + "! You have " + pending + " tasks waiting. Keep going! 💪";
        }
    }

    // 4. Weekly Coach
    public String generateWeeklyCoach(User user, List<Task> tasks, int focusScore, int streak) {
        long completed = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        long highDone  = tasks.stream().filter(t ->
            "DONE".equals(t.getStatus()) && t.getPriority() != null &&
            "HIGH".equals(t.getPriority().name())).count();
        try {
            return callClaude("""
                You are an expert productivity coach. Give personalized weekly tips.
                Format your response with these 3 sections:
                1. 📊 Week Review (2-3 sentences)
                2. 💡 Top 3 Tips for Next Week (numbered)
                3. 🎯 Your Challenge (one specific goal)
                Be warm, specific, and motivating.
                """,
                String.format("User: %s\nCompleted this week: %d\nHigh priority done: %d\nFocus score: %d\nStreak: %d days\nTotal tasks: %d",
                    user.getName(), completed, highDone, focusScore, streak, tasks.size()));
        } catch (Exception e) {
            return "Great week " + user.getName() + "! Completed " + completed + " tasks. Keep the momentum! 🚀";
        }
    }
}