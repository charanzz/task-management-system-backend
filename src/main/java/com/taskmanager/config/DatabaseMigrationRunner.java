package com.taskmanager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Auto-runs DB migrations on every startup.
 * Uses IF NOT EXISTS so it's safe to run multiple times.
 * NO manual DB access needed — Spring Boot handles it!
 */
@Configuration
public class DatabaseMigrationRunner {

    @Bean
    public CommandLineRunner runMigrations(JdbcTemplate jdbc) {
        return args -> {
            System.out.println("🔧 Running TaskFlow DB migrations...");

            // Sub-tasks table
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS sub_tasks (
                    id         BIGSERIAL PRIMARY KEY,
                    title      VARCHAR(500) NOT NULL,
                    completed  BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    task_id    BIGINT REFERENCES tasks(id) ON DELETE CASCADE
                )
            """);

            // Task comments table
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS task_comments (
                    id         BIGSERIAL PRIMARY KEY,
                    text       TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    task_id    BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
                    user_id    BIGINT REFERENCES users(id)
                )
            """);

            // Team messages (persistent chat)
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS team_messages (
                    id        BIGSERIAL PRIMARY KEY,
                    text      TEXT NOT NULL,
                    sent_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    team_id   BIGINT REFERENCES teams(id) ON DELETE CASCADE,
                    sender_id BIGINT REFERENCES users(id)
                )
            """);

            // Recurring columns on tasks (ALTER is safe with IF NOT EXISTS on Postgres)
            try {
                jdbc.execute("ALTER TABLE tasks ADD COLUMN IF NOT EXISTS recurring BOOLEAN DEFAULT FALSE");
            } catch (Exception e) {
                System.out.println("recurring column already exists, skipping");
            }
            try {
                jdbc.execute("ALTER TABLE tasks ADD COLUMN IF NOT EXISTS recurring_interval VARCHAR(50)");
            } catch (Exception e) {
                System.out.println("recurring_interval column already exists, skipping");
            }

            // Notifications table
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id         BIGSERIAL PRIMARY KEY,
                    type       VARCHAR(50) NOT NULL,
                    title      VARCHAR(255),
                    body       TEXT,
                    link       VARCHAR(255),
                    read       BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    user_id    BIGINT REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // Profile fields on users
            try { jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_color VARCHAR(20)"); } catch (Exception e) {}
            try { jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(300)"); } catch (Exception e) {}
            try { jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(60)"); } catch (Exception e) {}

            // Pomodoro sessions table
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                    id          BIGSERIAL PRIMARY KEY,
                    duration    INT DEFAULT 25,
                    completed   BOOLEAN DEFAULT FALSE,
                    started_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    ended_at    TIMESTAMP,
                    task_id     BIGINT REFERENCES tasks(id) ON DELETE SET NULL,
                    user_id     BIGINT REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // Onboarding flag on users
            try { jdbc.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_done BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}

            // Habits
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS habits (
                    id              BIGSERIAL PRIMARY KEY,
                    title           VARCHAR(255) NOT NULL,
                    emoji           VARCHAR(20),
                    color           VARCHAR(20),
                    frequency       VARCHAR(20) DEFAULT 'DAILY',
                    target_days     INT DEFAULT 7,
                    current_streak  INT DEFAULT 0,
                    longest_streak  INT DEFAULT 0,
                    archived        BOOLEAN DEFAULT FALSE,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE
                )
            """);
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS habit_logs (
                    id           BIGSERIAL PRIMARY KEY,
                    log_date     DATE NOT NULL,
                    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    habit_id     BIGINT REFERENCES habits(id) ON DELETE CASCADE,
                    UNIQUE(habit_id, log_date)
                )
            """);
            // Tags on tasks
            try { jdbc.execute("ALTER TABLE tasks ADD COLUMN IF NOT EXISTS tags VARCHAR(500)"); } catch(Exception e){}

            System.out.println("✅ DB migrations complete!");
        };
    }
}