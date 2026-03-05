package com.taskmanager.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    private String description;
    @ManyToOne @JoinColumn(name = "owner_id") private User owner;
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { if(createdAt==null) createdAt=LocalDateTime.now(); }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String n){this.name=n;}
    public String getDescription(){return description;} public void setDescription(String d){this.description=d;}
    public User getOwner(){return owner;} public void setOwner(User o){this.owner=o;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}