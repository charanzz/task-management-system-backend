package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_messages")
public class TeamMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"members","tasks"})
    private Team team;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"tasks","badges"})
    private User sender;

    @PrePersist protected void onCreate() { sentAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String t) { this.text = t; }
    public LocalDateTime getSentAt() { return sentAt; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
}