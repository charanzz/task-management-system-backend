package com.taskmanager.controller;

import com.taskmanager.dto.ExamPathDetailDTO;
import com.taskmanager.dto.ExamPathSummaryDTO;
import com.taskmanager.dto.QuizDTO;
import com.taskmanager.dto.QuizResultDTO;
import com.taskmanager.dto.QuizSubmitRequest;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.ExamPathService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paths")
public class ExamPathController {

    private final ExamPathService service;
    private final UserRepository userRepo;

    public ExamPathController(ExamPathService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<ExamPathSummaryDTO>> getAllPaths(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.getAllPaths(getUserId(userDetails)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamPathDetailDTO> getPath(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.getPathDetail(id, getUserId(userDetails)));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Void> enroll(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        service.enroll(id, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/topics/{topicId}/complete")
    public ResponseEntity<Void> completeTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        service.completeTopic(topicId, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/topics/{topicId}/quiz")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable Long topicId) {
        return ResponseEntity.ok(service.getQuiz(topicId));
    }

    @PostMapping("/topics/{topicId}/quiz/submit")
    public ResponseEntity<QuizResultDTO> submitQuiz(
            @PathVariable Long topicId,
            @RequestBody QuizSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.submitQuiz(topicId, getUserId(userDetails), request));
    }

    @PostMapping("/topics/{topicId}/quiz/skip")
    public ResponseEntity<Void> skipQuiz(
            @PathVariable Long topicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        service.skipQuiz(topicId, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepo.findByEmail(userDetails.getUsername())
                .map(u -> u.getId())
                .orElse(null);
    }
}