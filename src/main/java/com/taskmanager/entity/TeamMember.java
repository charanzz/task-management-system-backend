package com.taskmanager.entity;
import jakarta.persistence.*;
@Entity @Table(name = "team_members")
public class TeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "team_id") private Team team;
    @ManyToOne @JoinColumn(name = "user_id") private User user;
    private String role; // OWNER, ADMIN, MEMBER
    public Long getId(){return id;}
    public Team getTeam(){return team;} public void setTeam(Team t){this.team=t;}
    public User getUser(){return user;} public void setUser(User u){this.user=u;}
    public String getRole(){return role;} public void setRole(String r){this.role=r;}
}