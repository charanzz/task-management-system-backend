package com.taskmanager.repository;
import com.taskmanager.entity.TeamMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeamMessageRepository extends JpaRepository<TeamMessage, Long> {
    List<TeamMessage> findByTeamIdOrderBySentAtAsc(Long teamId);
}