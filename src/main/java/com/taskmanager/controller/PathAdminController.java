package com.taskmanager.controller;

import com.taskmanager.entity.*;
import com.taskmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

// ─── ADMIN ONLY — to enter path tasks and questions ──────────────
// Access: POST /api/admin/paths/tasks  — add a task
//         POST /api/admin/paths/questions — add a question
//         GET  /api/admin/paths/tasks?pathId=1 — list tasks
// This is how YOU build the curriculum without touching DB directly

@RestController
@RequestMapping("/api/admin/paths")
@CrossOrigin
public class PathAdminController {

    @Autowired private ExamPathRepository examPathRepo;
    @Autowired private PathPhaseRepository pathPhaseRepo;
    @Autowired private PathTaskRepository pathTaskRepo;
    @Autowired private PathQuestionRepository pathQuestionRepo;
    @Autowired private UserRepository userRepo;

    private void requireAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow();
        if (!"ADMIN".equals(user.getRole() != null ? user.getRole().name() : "")) {
            throw new RuntimeException("Admin access required");
        }
    }

    // ─── Add a Task ──────────────────────────────────────────────
    @PostMapping("/tasks")
    public ResponseEntity<?> addTask(@RequestBody Map<String, Object> body) {
        requireAdmin();

        Long pathId = Long.parseLong(body.get("pathId").toString());
        Long phaseId = Long.parseLong(body.get("phaseId").toString());

        ExamPath path = examPathRepo.findById(pathId).orElseThrow();
        PathPhase phase = pathPhaseRepo.findById(phaseId).orElseThrow();

        PathTask task = new PathTask();
        task.setExamPath(path);
        task.setPhase(phase);
        task.setTaskNumber(Integer.parseInt(body.get("taskNumber").toString()));
        task.setWeekNumber(Integer.parseInt(body.get("weekNumber").toString()));
        task.setPhaseNumber(Integer.parseInt(body.get("phaseNumber").toString()));
        task.setDayNumber(body.get("dayNumber") != null ? Integer.parseInt(body.get("dayNumber").toString()) : 1);
        task.setSubject(body.get("subject").toString());
        task.setTopic(body.get("topic").toString());
        task.setSubtopic(body.get("subtopic") != null ? body.get("subtopic").toString() : null);
        task.setTaskType(body.get("taskType").toString());
        task.setInstruction(body.get("instruction").toString());
        task.setResource(body.get("resource").toString());
        task.setResourceUrl(body.get("resourceUrl") != null ? body.get("resourceUrl").toString() : null);
        task.setEstimatedMinutes(body.get("estimatedMinutes") != null ? Integer.parseInt(body.get("estimatedMinutes").toString()) : 45);
        task.setDifficulty(body.get("difficulty") != null ? body.get("difficulty").toString() : "MEDIUM");
        task.setWeightagePercent(body.get("weightagePercent") != null ? Integer.parseInt(body.get("weightagePercent").toString()) : 5);
        task.setIcon(body.get("icon") != null ? body.get("icon").toString() : "📖");
        task.setTotalQuestions(0);

        pathTaskRepo.save(task);

        // Update path total tasks count
        long total = pathTaskRepo.findByExamPathOrderByTaskNumber(path).size();
        path.setTotalTasks((int) total);
        examPathRepo.save(path);

        return ResponseEntity.ok(Map.of("taskId", task.getId(), "message", "Task created"));
    }

    // ─── Add a Question to a Task ────────────────────────────────
    @PostMapping("/questions")
    public ResponseEntity<?> addQuestion(@RequestBody Map<String, Object> body) {
        requireAdmin();

        Long taskId = Long.parseLong(body.get("taskId").toString());
        PathTask task = pathTaskRepo.findById(taskId).orElseThrow();

        PathQuestion q = new PathQuestion();
        q.setPathTask(task);
        q.setQuestionNumber(Integer.parseInt(body.get("questionNumber").toString()));
        q.setQuestionText(body.get("questionText").toString());
        q.setOptionA(body.get("optionA").toString());
        q.setOptionB(body.get("optionB").toString());
        q.setOptionC(body.get("optionC").toString());
        q.setOptionD(body.get("optionD").toString());
        q.setCorrectOption(body.get("correctOption").toString());
        q.setExplanation(body.get("explanation") != null ? body.get("explanation").toString() : null);
        q.setSource(body.get("source") != null ? body.get("source").toString() : null);
        q.setYearAsked(body.get("yearAsked") != null ? Integer.parseInt(body.get("yearAsked").toString()) : null);
        q.setDifficulty(body.get("difficulty") != null ? body.get("difficulty").toString() : "MEDIUM");

        pathQuestionRepo.save(q);

        // Update task question count
        long count = pathQuestionRepo.findByPathTaskOrderByQuestionNumber(task).size();
        task.setTotalQuestions((int) count);
        pathTaskRepo.save(task);

        return ResponseEntity.ok(Map.of("questionId", q.getId(), "message", "Question added"));
    }

    // ─── List all tasks in a path ────────────────────────────────
    @GetMapping("/tasks")
    public ResponseEntity<?> listTasks(@RequestParam Long pathId) {
        requireAdmin();
        ExamPath path = examPathRepo.findById(pathId).orElseThrow();
        List<PathTask> tasks = pathTaskRepo.findByExamPathOrderByTaskNumber(path);
        return ResponseEntity.ok(Map.of("tasks", tasks, "total", tasks.size()));
    }

    // ─── List all phases in a path ───────────────────────────────
    @GetMapping("/phases")
    public ResponseEntity<?> listPhases(@RequestParam Long pathId) {
        requireAdmin();
        ExamPath path = examPathRepo.findById(pathId).orElseThrow();
        List<PathPhase> phases = pathPhaseRepo.findByExamPathOrderByPhaseNumber(path);
        return ResponseEntity.ok(phases);
    }

    // ─── List all exam paths ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> listPaths() {
        requireAdmin();
        return ResponseEntity.ok(examPathRepo.findAll());
    }
}