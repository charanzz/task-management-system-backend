package com.taskmanager.service;

import com.taskmanager.dto.ExamPathDetailDTO;
import com.taskmanager.dto.ExamPathSummaryDTO;
import com.taskmanager.dto.QuizDTO;
import com.taskmanager.dto.QuizResultDTO;
import com.taskmanager.dto.QuizSubmitRequest;
import com.taskmanager.entity.ExamPath;
import com.taskmanager.entity.ExamPhase;
import com.taskmanager.entity.ExamQuestion;
import com.taskmanager.entity.ExamTopic;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserPathEnrollment;
import com.taskmanager.entity.UserTopicProgress;
import com.taskmanager.repository.ExamPathRepository;
import com.taskmanager.repository.ExamPhaseRepository;
import com.taskmanager.repository.ExamQuestionRepository;
import com.taskmanager.repository.ExamTopicRepository;
import com.taskmanager.repository.UserPathEnrollmentRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.UserTopicProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamPathService {

    private final ExamPathRepository pathRepo;
    private final ExamPhaseRepository phaseRepo;
    private final ExamTopicRepository topicRepo;
    private final ExamQuestionRepository questionRepo;
    private final UserPathEnrollmentRepository enrollmentRepo;
    private final UserTopicProgressRepository progressRepo;
    private final UserRepository userRepo;

    // Explicit constructor — no Lombok needed
    public ExamPathService(ExamPathRepository pathRepo,
                           ExamPhaseRepository phaseRepo,
                           ExamTopicRepository topicRepo,
                           ExamQuestionRepository questionRepo,
                           UserPathEnrollmentRepository enrollmentRepo,
                           UserTopicProgressRepository progressRepo,
                           UserRepository userRepo) {
        this.pathRepo = pathRepo;
        this.phaseRepo = phaseRepo;
        this.topicRepo = topicRepo;
        this.questionRepo = questionRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.progressRepo = progressRepo;
        this.userRepo = userRepo;
    }

    // ── List all paths ────────────────────────────────────────────────────
    public List<ExamPathSummaryDTO> getAllPaths(Long userId) {
        return pathRepo.findAllByOrderByComingSoonAscIdAsc().stream()
                .map(p -> toSummaryDTO(p, userId))
                .collect(Collectors.toList());
    }

    // ── Full path detail ──────────────────────────────────────────────────
    public ExamPathDetailDTO getPathDetail(Long pathId, Long userId) {
        ExamPath path = pathRepo.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));

        UserPathEnrollment enrollment = userId != null
                ? enrollmentRepo.findByUserIdAndExamPathId(userId, pathId).orElse(null)
                : null;

        List<UserTopicProgress> allProgress = userId != null
                ? progressRepo.findByUserIdAndPathId(userId, pathId)
                : List.of();

        Map<Long, UserTopicProgress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(p -> p.getTopic().getId(), p -> p));

        List<ExamPathDetailDTO.PhaseDTO> phaseDTOs = path.getPhases().stream()
                .map(phase -> toPhaseDTO(phase, progressMap))
                .collect(Collectors.toList());

        int completedTopics = enrollment != null ? enrollment.getCompletedTopics() : 0;
        double progress = path.getTotalTasks() > 0
                ? (completedTopics * 100.0 / path.getTotalTasks()) : 0;

        return ExamPathDetailDTO.builder()
                .id(path.getId())
                .title(path.getTitle())
                .slug(path.getSlug())
                .category(path.getCategory())
                .description(path.getDescription())
                .icon(path.getIcon())
                .audience(path.getAudience())
                .language(path.getLanguage())
                .totalWeeks(path.getTotalWeeks())
                .totalTasks(path.getTotalTasks())
                .enrolled(enrollment != null)
                .completedTopics(completedTopics)
                .progressPercent(Math.round(progress * 10.0) / 10.0)
                .totalXpEarned(enrollment != null ? enrollment.getTotalXpEarned() : 0)
                .overallAccuracy(enrollment != null ? enrollment.getOverallAccuracy() : 0)
                .currentStreak(enrollment != null ? enrollment.getCurrentStreak() : 0)
                .phases(phaseDTOs)
                .build();
    }

    // ── Enroll ────────────────────────────────────────────────────────────
    @Transactional
    public void enroll(Long pathId, Long userId) {
        if (enrollmentRepo.existsByUserIdAndExamPathId(userId, pathId)) return;
        ExamPath path = pathRepo.findById(pathId)
                .orElseThrow(() -> new RuntimeException("Path not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPathEnrollment e = new UserPathEnrollment();
        e.setUser(user);
        e.setExamPath(path);
        e.setEnrolledAt(LocalDateTime.now());
        e.setLastActivityAt(LocalDateTime.now());
        e.setCompletedTopics(0);
        e.setTotalXpEarned(0);
        e.setOverallAccuracy(0);
        e.setCurrentStreak(0);
        e.setCompleted(false);
        enrollmentRepo.save(e);
    }

    // ── Complete topic ────────────────────────────────────────────────────
    @Transactional
    public void completeTopic(Long topicId, Long userId) {
        ExamTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserTopicProgress progress = progressRepo
                .findByUserIdAndTopicId(userId, topicId)
                .orElseGet(() -> {
                    UserTopicProgress p = new UserTopicProgress();
                    p.setUser(user);
                    p.setTopic(topic);
                    return p;
                });

        if (progress.isCompleted()) return;

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepo.save(progress);

        updateEnrollmentStats(userId, topic.getPhase().getExamPath().getId(), topic.getXpReward());
    }

    // ── Get quiz ──────────────────────────────────────────────────────────
    public QuizDTO getQuiz(Long topicId) {
        ExamTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        List<ExamQuestion> questions = questionRepo.findByTopicIdOrderByOrderIndexAsc(topicId);

        List<QuizDTO.QuestionDTO> questionDTOs = questions.stream()
                .map(q -> QuizDTO.QuestionDTO.builder()
                        .id(q.getId())
                        .question(q.getQuestion())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .build())
                .collect(Collectors.toList());

        return QuizDTO.builder()
                .topicId(topicId)
                .topicTitle(topic.getTitle())
                .questions(questionDTOs)
                .build();
    }

    // ── Submit quiz ───────────────────────────────────────────────────────
    @Transactional
    public QuizResultDTO submitQuiz(Long topicId, Long userId, QuizSubmitRequest request) {
        ExamTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExamQuestion> questions = questionRepo.findByTopicIdOrderByOrderIndexAsc(topicId);
        Map<Long, ExamQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));

        List<QuizResultDTO.AnswerResultDTO> results = request.getAnswers().stream()
                .map(ans -> {
                    ExamQuestion q = questionMap.get(ans.getQuestionId());
                    boolean correct = q != null && q.getCorrectOption().equals(ans.getSelectedOption());
                    return QuizResultDTO.AnswerResultDTO.builder()
                            .questionId(ans.getQuestionId())
                            .question(q != null ? q.getQuestion() : "")
                            .selectedOption(ans.getSelectedOption())
                            .correctOption(q != null ? q.getCorrectOption() : "")
                            .explanation(q != null ? q.getExplanation() : "")
                            .correct(correct)
                            .build();
                })
                .collect(Collectors.toList());

        long correctCount = results.stream()
                .filter(QuizResultDTO.AnswerResultDTO::isCorrect).count();
        int total = questions.size();
        double accuracy = total > 0 ? (correctCount * 100.0 / total) : 0;
        int xpEarned = (int) (accuracy >= 60 ? topic.getXpReward() * 0.5 : topic.getXpReward() * 0.2);

        UserTopicProgress progress = progressRepo.findByUserIdAndTopicId(userId, topicId)
                .orElseGet(() -> {
                    UserTopicProgress p = new UserTopicProgress();
                    p.setUser(user);
                    p.setTopic(topic);
                    return p;
                });
        progress.setQuizAttempted(true);
        progress.setQuizScore((int) correctCount);
        progress.setQuizTotal(total);
        progress.setQuizAccuracy(Math.round(accuracy * 10.0) / 10.0);
        progress.setQuizAttemptedAt(LocalDateTime.now());
        progressRepo.save(progress);

        updateEnrollmentAccuracy(userId, topic.getPhase().getExamPath().getId());

        return QuizResultDTO.builder()
                .score((int) correctCount)
                .total(total)
                .accuracy(Math.round(accuracy * 10.0) / 10.0)
                .xpEarned(xpEarned)
                .results(results)
                .build();
    }

    // ── Skip quiz ─────────────────────────────────────────────────────────
    @Transactional
    public void skipQuiz(Long topicId, Long userId) {
        ExamTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserTopicProgress progress = progressRepo.findByUserIdAndTopicId(userId, topicId)
                .orElseGet(() -> {
                    UserTopicProgress p = new UserTopicProgress();
                    p.setUser(user);
                    p.setTopic(topic);
                    return p;
                });
        progress.setQuizSkipped(true);
        progressRepo.save(progress);
    }

    // ── Private helpers ───────────────────────────────────────────────────
    private void updateEnrollmentStats(Long userId, Long pathId, int xpEarned) {
        enrollmentRepo.findByUserIdAndExamPathId(userId, pathId).ifPresent(e -> {
            int completed = progressRepo.countCompletedByUserAndPath(userId, pathId);
            e.setCompletedTopics(completed);
            e.setTotalXpEarned(e.getTotalXpEarned() + xpEarned);
            e.setLastActivityAt(LocalDateTime.now());
            e.setCurrentStreak(e.getCurrentStreak() + 1);
            if (completed >= e.getExamPath().getTotalTasks()) e.setCompleted(true);
            enrollmentRepo.save(e);
        });
    }

    private void updateEnrollmentAccuracy(Long userId, Long pathId) {
        enrollmentRepo.findByUserIdAndExamPathId(userId, pathId).ifPresent(e -> {
            double avg = progressRepo.findByUserIdAndPathId(userId, pathId).stream()
                    .filter(UserTopicProgress::isQuizAttempted)
                    .mapToDouble(UserTopicProgress::getQuizAccuracy)
                    .average().orElse(0);
            e.setOverallAccuracy(Math.round(avg * 10.0) / 10.0);
            enrollmentRepo.save(e);
        });
    }

    private ExamPathSummaryDTO toSummaryDTO(ExamPath path, Long userId) {
        UserPathEnrollment e = userId != null
                ? enrollmentRepo.findByUserIdAndExamPathId(userId, path.getId()).orElse(null)
                : null;
        int completed = e != null ? e.getCompletedTopics() : 0;
        double pct = path.getTotalTasks() > 0 ? (completed * 100.0 / path.getTotalTasks()) : 0;

        return ExamPathSummaryDTO.builder()
                .id(path.getId()).title(path.getTitle()).slug(path.getSlug())
                .category(path.getCategory()).description(path.getDescription())
                .icon(path.getIcon()).audience(path.getAudience()).language(path.getLanguage())
                .totalWeeks(path.getTotalWeeks()).totalTasks(path.getTotalTasks())
                .comingSoon(path.isComingSoon()).enrolled(e != null)
                .completedTopics(completed)
                .progressPercent(Math.round(pct * 10.0) / 10.0)
                .totalXpEarned(e != null ? e.getTotalXpEarned() : 0)
                .currentStreak(e != null ? e.getCurrentStreak() : 0)
                .build();
    }

    private ExamPathDetailDTO.PhaseDTO toPhaseDTO(ExamPhase phase,
                                                   Map<Long, UserTopicProgress> progressMap) {
        List<ExamPathDetailDTO.TopicDTO> topics = phase.getTopics().stream()
                .map(t -> toTopicDTO(t, progressMap.get(t.getId())))
                .collect(Collectors.toList());

        long done = topics.stream()
                .filter(ExamPathDetailDTO.TopicDTO::isCompleted).count();
        double pct = phase.getTotalTopics() > 0 ? (done * 100.0 / phase.getTotalTopics()) : 0;

        return ExamPathDetailDTO.PhaseDTO.builder()
                .id(phase.getId()).title(phase.getTitle()).description(phase.getDescription())
                .icon(phase.getIcon()).orderIndex(phase.getOrderIndex())
                .totalTopics(phase.getTotalTopics()).completedTopics((int) done)
                .progressPercent(Math.round(pct * 10.0) / 10.0).topics(topics)
                .build();
    }

    private ExamPathDetailDTO.TopicDTO toTopicDTO(ExamTopic topic, UserTopicProgress p) {
        return ExamPathDetailDTO.TopicDTO.builder()
                .id(topic.getId()).title(topic.getTitle()).description(topic.getDescription())
                .content(topic.getContent()).resourceUrl(topic.getResourceUrl())
                .resourceType(topic.getResourceType()).orderIndex(topic.getOrderIndex())
                .xpReward(topic.getXpReward()).estimatedMinutes(topic.getEstimatedMinutes())
                .hasQuiz(topic.isHasQuiz())
                .completed(p != null && p.isCompleted())
                .quizAttempted(p != null && p.isQuizAttempted())
                .quizSkipped(p != null && p.isQuizSkipped())
                .quizAccuracy(p != null ? p.getQuizAccuracy() : 0)
                .build();
    }
}