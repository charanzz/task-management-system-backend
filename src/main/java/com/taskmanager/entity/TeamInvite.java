package com.taskmanager.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "team_invites")
public class TeamInvite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "team_id") private Team team;
    private String email;
    @Column(unique = true) private String token;
    @ManyToOne @JoinColumn(name = "invited_by") private User invitedBy;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    public Long getId(){return id;}
    public Team getTeam(){return team;} public void setTeam(Team t){this.team=t;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public String getToken(){return token;} public void setToken(String t){this.token=t;}
    public User getInvitedBy(){return invitedBy;} public void setInvitedBy(User u){this.invitedBy=u;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime e){this.expiresAt=e;}
}