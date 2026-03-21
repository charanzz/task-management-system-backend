package com.taskmanager.controller;

import com.taskmanager.entity.*;
import com.taskmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paths")
@CrossOrigin
public class PathController {

    @Autowired private ExamPathRepository examPathRepo;
    @Autowired private PathPhaseRepository pathPhaseRepo;
    @Autowired private PathTaskRepository pathTaskRepo;
    @Autowired private PathQuestionRepository pathQuestionRepo;
    @Autowired private UserPathProgressRepository progressRepo;
    @Autowired private UserTaskResponseRepository responseRepo;
    @Autowired private UserRepository userRepo;

    // ─── Get current user ───────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow();
    }

    // ─── Check trial / pro access ───────────────────────────────
    private boolean hasPathAccess(User user) {
        if (Boolean.TRUE.equals(user.getIsPro())) return true;
        if (user.getCreatedAt() == null) return true;
        long daysSinceJoin = java.time.temporal.ChronoUnit.DAYS.between(
            user.getCreatedAt().toLocalDate(), LocalDate.now()
        );
        return daysSinceJoin <= 15;
    }

    // ─── GET /api/paths ─ list all available exam paths ─────────
    @GetMapping
    public ResponseEntity<?> getAllPaths() {
        User user = getCurrentUser();
        boolean hasAccess = hasPathAccess(user);

        List<ExamPath> paths = examPathRepo.findByIsActiveTrue();

        // Get user's active path if any
        Optional<UserPathProgress> activeProgress = progressRepo
            .findByUserAndStatus(user, "ACTIVE").stream().findFirst();

        List<Map<String, Object>> result = paths.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("examType", p.getExamType());
            map.put("title", p.getTitle());
            map.put("description", p.getDescription());
            map.put("icon", p.getIcon());
            map.put("bannerColor", p.getBannerColor());
            map.put("totalTasks", p.getTotalTasks());
            map.put("totalWeeks", p.getTotalWeeks());
            map.put("difficulty", p.getDifficulty());
            map.put("targetAudience", p.getTargetAudience());
            map.put("examBody", p.getExamBody());
            map.put("language", p.getLanguage());

            // Is user already enrolled in this path?
            boolean enrolled = activeProgress.isPresent() &&
                activeProgress.get().getExamPath().getId().equals(p.getId());
            map.put("enrolled", enrolled);

            if (enrolled) {
                UserPathProgress prog = activeProgress.get();
                map.put("currentTask", prog.getCurrentTaskNumber());
                map.put("tasksCompleted", prog.getTasksCompleted());
                map.put("progressPercent",
                    p.getTotalTasks() > 0
                        ? (prog.getTasksCompleted() * 100 / p.getTotalTasks())
                        : 0);
                map.put("currentPhase", prog.getCurrentPhase());
                map.put("currentWeek", prog.getCurrentWeek());
                map.put("pathStreakDays", prog.getPathStreakDays());
            }

            return map;
        }).collect(Collectors.toList());

        // Days left in trial
        long daysSinceJoin = user.getCreatedAt() != null
            ? java.time.temporal.ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now())
            : 0;
        int trialDaysLeft = (int) Math.max(0, 15 - daysSinceJoin);

        return ResponseEntity.ok(Map.of(
            "paths", result,
            "hasAccess", hasAccess,
            "isPro", Boolean.TRUE.equals(user.getIsPro()),
            "trialDaysLeft", trialDaysLeft
        ));
    }

    // ─── GET /api/paths/{pathId}/roadmap ─ full path structure ──
    @GetMapping("/{pathId}/roadmap")
    public ResponseEntity<?> getRoadmap(@PathVariable Long pathId) {
        User user = getCurrentUser();
        if (!hasPathAccess(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Trial expired. Upgrade to Pro."));
        }

        ExamPath path = examPathRepo.findById(pathId)
            .orElseThrow(() -> new RuntimeException("Path not found"));

        List<PathPhase> phases = pathPhaseRepo.findByExamPathOrderByPhaseNumber(path);

        Optional<UserPathProgress> progressOpt = progressRepo
            .findByUserAndExamPath(user, path);

        int currentTask = progressOpt.map(UserPathProgress::getCurrentTaskNumber).orElse(1);
        int tasksCompleted = progressOpt.map(UserPathProgress::getTasksCompleted).orElse(0);

        // Get completed task IDs for this user
        Set<Long> completedTaskIds = responseRepo
            .findByUserAndCompleted(user, true)
            .stream()
            .map(r -> r.getPathTask().getId())
            .collect(Collectors.toSet());

        List<Map<String, Object>> phaseList = phases.stream().map(phase -> {
            Map<String, Object> phaseMap = new LinkedHashMap<>();
            phaseMap.put("id", phase.getId());
            phaseMap.put("phaseNumber", phase.getPhaseNumber());
            phaseMap.put("title", phase.getTitle());
            phaseMap.put("description", phase.getDescription());
            phaseMap.put("goal", phase.getGoal());
            phaseMap.put("startWeek", phase.getStartWeek());
            phaseMap.put("endWeek", phase.getEndWeek());
            phaseMap.put("color", phase.getColor());
            phaseMap.put("icon", phase.getIcon());
            phaseMap.put("totalTasks", phase.getTotalTasks());

            // Tasks in this phase
            List<PathTask> tasks = pathTaskRepo.findByPhaseOrderByTaskNumber(phase);
            long phaseCompleted = tasks.stream()
                .filter(t -> completedTaskIds.contains(t.getId())).count();
            phaseMap.put("tasksCompleted", (int) phaseCompleted);

            List<Map<String, Object>> taskList = tasks.stream().map(task -> {
                Map<String, Object> taskMap = new LinkedHashMap<>();
                taskMap.put("id", task.getId());
                taskMap.put("taskNumber", task.getTaskNumber());
                taskMap.put("weekNumber", task.getWeekNumber());
                taskMap.put("subject", task.getSubject());
                taskMap.put("topic", task.getTopic());
                taskMap.put("subtopic", task.getSubtopic());
                taskMap.put("taskType", task.getTaskType());
                taskMap.put("estimatedMinutes", task.getEstimatedMinutes());
                taskMap.put("difficulty", task.getDifficulty());
                taskMap.put("totalQuestions", task.getTotalQuestions());
                taskMap.put("icon", task.getIcon());
                taskMap.put("weightagePercent", task.getWeightagePercent());

                boolean isCompleted = completedTaskIds.contains(task.getId());
                boolean isCurrent = task.getTaskNumber().equals(currentTask);
                boolean isLocked = task.getTaskNumber() > currentTask;

                taskMap.put("completed", isCompleted);
                taskMap.put("current", isCurrent);
                taskMap.put("locked", isLocked && !isCompleted);

                return taskMap;
            }).collect(Collectors.toList());

            phaseMap.put("tasks", taskList);
            return phaseMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", Map.of(
            "id", path.getId(),
            "title", path.getTitle(),
            "examType", path.getExamType(),
            "totalTasks", path.getTotalTasks(),
            "totalWeeks", path.getTotalWeeks()
        ));
        response.put("phases", phaseList);
        response.put("currentTask", currentTask);
        response.put("tasksCompleted", tasksCompleted);
        response.put("progressPercent", path.getTotalTasks() > 0
            ? (tasksCompleted * 100 / path.getTotalTasks()) : 0);

        if (progressOpt.isPresent()) {
            UserPathProgress prog = progressOpt.get();
            response.put("pathStreakDays", prog.getPathStreakDays());
            response.put("totalXpEarned", prog.getTotalXpEarned());
            response.put("totalMinutesStudied", prog.getTotalMinutesStudied());
            response.put("weakSubjects", prog.getWeakSubjects());
        }

        return ResponseEntity.ok(response);
    }

    // ─── POST /api/paths/{pathId}/enroll ─ start a path ─────────
    @PostMapping("/{pathId}/enroll")
    public ResponseEntity<?> enroll(@PathVariable Long pathId,
                                    @RequestBody(required = false) Map<String, String> body) {
        User user = getCurrentUser();
        if (!hasPathAccess(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Trial expired. Upgrade to Pro."));
        }

        ExamPath path = examPathRepo.findById(pathId)
            .orElseThrow(() -> new RuntimeException("Path not found"));

        // Check if already enrolled
        Optional<UserPathProgress> existing = progressRepo.findByUserAndExamPath(user, path);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Already enrolled", "progress", existing.get().getId()));
        }

        UserPathProgress progress = new UserPathProgress();
        progress.setUser(user);
        progress.setExamPath(path);
        if (body != null && body.get("targetExamDate") != null) {
            progress.setTargetExamDate(LocalDate.parse(body.get("targetExamDate")));
        }

        progressRepo.save(progress);
        return ResponseEntity.ok(Map.of("message", "Enrolled successfully", "pathId", pathId));
    }

    // ─── GET /api/paths/tasks/{taskId} ─ get task detail + questions
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable Long taskId) {
        User user = getCurrentUser();
        if (!hasPathAccess(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Trial expired."));
        }

        PathTask task = pathTaskRepo.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        List<PathQuestion> questions = pathQuestionRepo.findByPathTaskOrderByQuestionNumber(task);

        // Check if already completed
        Optional<UserTaskResponse> prevResponse = responseRepo.findByUserAndPathTask(user, task);

        Map<String, Object> taskDetail = new LinkedHashMap<>();
        taskDetail.put("id", task.getId());
        taskDetail.put("taskNumber", task.getTaskNumber());
        taskDetail.put("weekNumber", task.getWeekNumber());
        taskDetail.put("phaseNumber", task.getPhaseNumber());
        taskDetail.put("subject", task.getSubject());
        taskDetail.put("topic", task.getTopic());
        taskDetail.put("subtopic", task.getSubtopic());
        taskDetail.put("taskType", task.getTaskType());
        taskDetail.put("instruction", task.getInstruction());
        taskDetail.put("resource", task.getResource());
        taskDetail.put("resourceUrl", task.getResourceUrl());
        taskDetail.put("estimatedMinutes", task.getEstimatedMinutes());
        taskDetail.put("difficulty", task.getDifficulty());
        taskDetail.put("icon", task.getIcon());
        taskDetail.put("weightagePercent", task.getWeightagePercent());
        taskDetail.put("alreadyCompleted", prevResponse.isPresent() && prevResponse.get().getCompleted());

        if (prevResponse.isPresent()) {
            taskDetail.put("previousScore", prevResponse.get().getScorePercent());
            taskDetail.put("needsRevision", prevResponse.get().getNeedsRevision());
        }

        // Questions — don't send correct answers upfront
        List<Map<String, Object>> qList = questions.stream().map(q -> {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionNumber", q.getQuestionNumber());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("optionA", q.getOptionA());
            qMap.put("optionB", q.getOptionB());
            qMap.put("optionC", q.getOptionC());
            qMap.put("optionD", q.getOptionD());
            qMap.put("difficulty", q.getDifficulty());
            qMap.put("source", q.getSource());
            // No correct answer sent — sent after submission
            return qMap;
        }).collect(Collectors.toList());

        taskDetail.put("questions", qList);
        taskDetail.put("totalQuestions", qList.size());

        return ResponseEntity.ok(taskDetail);
    }

    // ─── POST /api/paths/tasks/{taskId}/submit ─ submit answers ─
    @PostMapping("/tasks/{taskId}/submit")
    public ResponseEntity<?> submitTask(@PathVariable Long taskId,
                                        @RequestBody Map<String, Object> body) {
        User user = getCurrentUser();
        if (!hasPathAccess(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Trial expired."));
        }

        PathTask task = pathTaskRepo.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        // Get submitted answers: {"questionId": "A", "questionId2": "B"}
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.get("answers");
        Integer timeTaken = body.get("timeTaken") != null
            ? Integer.parseInt(body.get("timeTaken").toString()) : 0;

        List<PathQuestion> questions = pathQuestionRepo.findByPathTaskOrderByQuestionNumber(task);

        // Calculate score
        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>();

        for (PathQuestion q : questions) {
            String submitted = answers != null ? answers.get(q.getId().toString()) : null;
            boolean isCorrect = q.getCorrectOption().equals(submitted);
            if (isCorrect) correct++;

            Map<String, Object> qResult = new LinkedHashMap<>();
            qResult.put("questionId", q.getId());
            qResult.put("questionNumber", q.getQuestionNumber());
            qResult.put("questionText", q.getQuestionText());
            qResult.put("optionA", q.getOptionA());
            qResult.put("optionB", q.getOptionB());
            qResult.put("optionC", q.getOptionC());
            qResult.put("optionD", q.getOptionD());
            qResult.put("correctOption", q.getCorrectOption());
            qResult.put("selectedOption", submitted);
            qResult.put("isCorrect", isCorrect);
            qResult.put("explanation", q.getExplanation());
            qResult.put("source", q.getSource());
            results.add(qResult);
        }

        int total = questions.size();
        int scorePercent = total > 0 ? (correct * 100 / total) : 100;
        boolean needsRevision = scorePercent < 60;
        int xpEarned = needsRevision ? 10 : (scorePercent >= 80 ? 30 : 20);

        // Save response
        UserTaskResponse response = new UserTaskResponse();
        response.setUser(user);
        response.setPathTask(task);
        response.setCorrectAnswers(correct);
        response.setTotalQuestions(total);
        response.setScorePercent(scorePercent);
        response.setTimeTakenMinutes(timeTaken);
        response.setNeedsRevision(needsRevision);
        response.setCompleted(true);
        response.setXpEarned(xpEarned);
        response.setCompletedAt(LocalDateTime.now());
        responseRepo.save(response);

        // Update user path progress
        Optional<UserPathProgress> progressOpt = progressRepo
            .findByUserAndExamPath(user, task.getExamPath());

        if (progressOpt.isPresent()) {
            UserPathProgress progress = progressOpt.get();
            progress.setTasksCompleted(progress.getTasksCompleted() + 1);
            progress.setTotalQuestionsAttempted(progress.getTotalQuestionsAttempted() + total);
            progress.setTotalCorrectAnswers(progress.getTotalCorrectAnswers() + correct);
            progress.setTotalXpEarned(progress.getTotalXpEarned() + xpEarned);
            progress.setTotalMinutesStudied(progress.getTotalMinutesStudied() + timeTaken);
            progress.setLastActivityAt(LocalDateTime.now());

            // Advance to next task
            int nextTask = task.getTaskNumber() + 1;
            if (nextTask > progress.getCurrentTaskNumber()) {
                progress.setCurrentTaskNumber(nextTask);
                progress.setCurrentWeek(task.getWeekNumber());
                progress.setCurrentPhase(task.getPhaseNumber());
            }

            // Check if path complete
            ExamPath path = task.getExamPath();
            if (progress.getTasksCompleted() >= path.getTotalTasks()) {
                progress.setStatus("COMPLETED");
                progress.setCompletedAt(LocalDateTime.now());
            }

            // Update weak subjects
            if (needsRevision) {
                String weak = progress.getWeakSubjects();
                if (weak == null) weak = "";
                if (!weak.contains(task.getSubject())) {
                    weak = weak.isEmpty() ? task.getSubject() : weak + "," + task.getSubject();
                    progress.setWeakSubjects(weak);
                }
            }

            progressRepo.save(progress);
        }

        // Get next task info
        Optional<PathTask> nextTaskOpt = pathTaskRepo
            .findByExamPathAndTaskNumber(task.getExamPath(), task.getTaskNumber() + 1);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("scorePercent", scorePercent);
        resp.put("correctAnswers", correct);
        resp.put("totalQuestions", total);
        resp.put("needsRevision", needsRevision);
        resp.put("xpEarned", xpEarned);
        resp.put("results", results);
        resp.put("taskCompleted", true);
        resp.put("pathComplete", !nextTaskOpt.isPresent());

        nextTaskOpt.ifPresent(nt -> {
            resp.put("nextTaskId", nt.getId());
            resp.put("nextTaskNumber", nt.getTaskNumber());
            resp.put("nextTaskTopic", nt.getTopic());
            resp.put("nextTaskSubject", nt.getSubject());
        });

        return ResponseEntity.ok(resp);
    }

    // ─── GET /api/paths/my-progress ─ user's active path summary ─
    @GetMapping("/my-progress")
    public ResponseEntity<?> getMyProgress() {
        User user = getCurrentUser();
        List<UserPathProgress> progresses = progressRepo.findByUser(user);

        if (progresses.isEmpty()) {
            return ResponseEntity.ok(Map.of("enrolled", false));
        }

        UserPathProgress prog = progresses.stream()
            .filter(p -> "ACTIVE".equals(p.getStatus()))
            .findFirst()
            .orElse(progresses.get(0));

        ExamPath path = prog.getExamPath();
        int totalTasks = path.getTotalTasks() != null ? path.getTotalTasks() : 1;
        int completed = prog.getTasksCompleted() != null ? prog.getTasksCompleted() : 0;

        // Get current task detail
        Optional<PathTask> currentTaskOpt = pathTaskRepo
            .findByExamPathAndTaskNumber(path, prog.getCurrentTaskNumber());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("enrolled", true);
        resp.put("pathId", path.getId());
        resp.put("pathTitle", path.getTitle());
        resp.put("pathIcon", path.getIcon());
        resp.put("status", prog.getStatus());
        resp.put("tasksCompleted", completed);
        resp.put("totalTasks", totalTasks);
        resp.put("progressPercent", completed * 100 / totalTasks);
        resp.put("currentPhase", prog.getCurrentPhase());
        resp.put("currentWeek", prog.getCurrentWeek());
        resp.put("pathStreakDays", prog.getPathStreakDays());
        resp.put("totalXpEarned", prog.getTotalXpEarned());
        resp.put("totalMinutesStudied", prog.getTotalMinutesStudied());
        resp.put("weakSubjects", prog.getWeakSubjects());
        resp.put("startedAt", prog.getStartedAt());
        resp.put("targetExamDate", prog.getTargetExamDate());

        // Overall accuracy
        int attempted = prog.getTotalQuestionsAttempted() != null ? prog.getTotalQuestionsAttempted() : 0;
        int correct = prog.getTotalCorrectAnswers() != null ? prog.getTotalCorrectAnswers() : 0;
        resp.put("overallAccuracy", attempted > 0 ? (correct * 100 / attempted) : 0);

        currentTaskOpt.ifPresent(ct -> {
            resp.put("currentTaskId", ct.getId());
            resp.put("currentTaskNumber", ct.getTaskNumber());
            resp.put("currentTaskTopic", ct.getTopic());
            resp.put("currentTaskSubject", ct.getSubject());
            resp.put("currentTaskType", ct.getTaskType());
            resp.put("currentTaskEstimatedMinutes", ct.getEstimatedMinutes());
            resp.put("currentTaskIcon", ct.getIcon());
        });

        return ResponseEntity.ok(resp);
    }
}