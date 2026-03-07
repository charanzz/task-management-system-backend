package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
public class TaskComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String text;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"comments","subTasks","user","team","assignee"})
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"tasks","badges"})
    private User author;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getText() { return text; }
    public void setText(String t) { this.text = t; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}