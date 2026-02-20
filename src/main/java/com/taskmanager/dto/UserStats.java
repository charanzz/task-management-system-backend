package com.taskmanager.dto;

public class UserStats {

    private int totalTasks;
    private int completedTasks;
    private int focusScore;
    private int streak;

    public UserStats(int totalTasks, int completedTasks, int focusScore, int streak) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.focusScore = focusScore;
        this.streak = streak;
    }

    public int getTotalTasks() { return totalTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public int getFocusScore() { return focusScore; }
    public int getStreak() { return streak; }
}
