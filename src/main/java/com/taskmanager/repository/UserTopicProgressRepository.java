package com.taskmanager.repository;

import com.taskmanager.entity.UserTopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicProgressRepository extends JpaRepository<UserTopicProgress, Long> {
    Optional<UserTopicProgress> findByUserIdAndTopicId(Long userId, Long topicId);

    @Query("SELECT p FROM UserTopicProgress p WHERE p.user.id = :userId AND p.topic.phase.examPath.id = :pathId")
    List<UserTopicProgress> findByUserIdAndPathId(@Param("userId") Long userId, @Param("pathId") Long pathId);

    @Query("SELECT COUNT(p) FROM UserTopicProgress p WHERE p.user.id = :userId AND p.topic.phase.examPath.id = :pathId AND p.completed = true")
    int countCompletedByUserAndPath(@Param("userId") Long userId, @Param("pathId") Long pathId);

    @Query("SELECT COUNT(p) FROM UserTopicProgress p WHERE p.user.id = :userId AND p.topic.phase.id = :phaseId AND p.completed = true")
    int countCompletedByUserAndPhase(@Param("userId") Long userId, @Param("phaseId") Long phaseId);
}