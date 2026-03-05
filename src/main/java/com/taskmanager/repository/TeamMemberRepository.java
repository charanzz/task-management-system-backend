package com.taskmanager.repository;
import com.taskmanager.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByUserId(Long userId);
    List<TeamMember> findByTeamId(Long teamId);
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    void deleteByTeamIdAndUserId(Long teamId, Long userId);
}