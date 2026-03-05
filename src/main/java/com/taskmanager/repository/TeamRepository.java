package com.taskmanager.repository;
import com.taskmanager.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TeamRepository extends JpaRepository<Team, Long> {}