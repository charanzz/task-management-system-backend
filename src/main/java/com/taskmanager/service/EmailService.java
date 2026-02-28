package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTaskReminder(User user, Task task, String timeLabel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("⏰ TaskFlow Reminder: \"" + task.getTitle() + "\" is due " + timeLabel);
            helper.setText(buildEmailHtml(user, task, timeLabel), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendBadgeEmail(User user, String badgeName, String badgeEmoji) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("🏆 TaskFlow: You earned the " + badgeName + " badge!");
            helper.setText(buildBadgeEmailHtml(user, badgeName, badgeEmoji), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send badge email: " + e.getMessage());
        }
    }

    private String buildEmailHtml(User user, Task task, String timeLabel) {
        String priorityColor = switch (task.getPriority().name()) {
            case "HIGH"   -> "#ff6b6b";
            case "MEDIUM" -> "#ffd93d";
            default       -> "#6bcb77";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:32px;text-align:center;">
                  <div style="font-size:32px;margin-bottom:8px;">⚡</div>
                  <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">TaskFlow Reminder</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;">Don't let this slip through the cracks!</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 20px;">Hi <strong style="color:#f0f0f8;">%s</strong>,</p>
                  <div style="background:#1a1a24;border-radius:14px;padding:20px;border-left:4px solid %s;margin-bottom:24px;">
                    <p style="color:#6b6b8a;font-size:10px;letter-spacing:2px;text-transform:uppercase;margin:0 0 8px;">TASK DUE %s</p>
                    <h2 style="color:#f0f0f8;margin:0 0 8px;font-size:18px;">%s</h2>
                    %s
                    <div style="display:flex;gap:10px;margin-top:12px;">
                      <span style="background:%s22;color:%s;padding:4px 12px;border-radius:20px;font-size:11px;font-weight:600;">%s Priority</span>
                    </div>
                  </div>
                  <div style="text-align:center;">
                    <a href="https://todoperks.online/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;font-size:14px;">View Task →</a>
                  </div>
                </div>
                <div style="padding:20px 32px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
                  <p style="color:#6b6b8a;font-size:11px;margin:0;">© 2025 TaskFlow · <a href="#" style="color:#7c3aed;">Unsubscribe</a></p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                priorityColor,
                timeLabel.toUpperCase(),
                task.getTitle(),
                task.getDescription() != null ? "<p style='color:#a0a0b8;font-size:13px;margin:0;'>" + task.getDescription() + "</p>" : "",
                priorityColor, priorityColor,
                task.getPriority().name()
        );
    }

    private String buildBadgeEmailHtml(User user, String badgeName, String badgeEmoji) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:40px;text-align:center;">
                  <div style="font-size:64px;margin-bottom:12px;">%s</div>
                  <h1 style="color:#fff;margin:0;font-size:24px;font-weight:800;">Badge Unlocked!</h1>
                </div>
                <div style="padding:32px;text-align:center;">
                  <p style="color:#a0a0b8;">Congratulations <strong style="color:#f0f0f8;">%s</strong>!</p>
                  <div style="background:#1a1a24;border-radius:14px;padding:24px;margin:20px 0;">
                    <h2 style="color:#c084fc;margin:0;font-size:20px;">%s</h2>
                    <p style="color:#6b6b8a;font-size:13px;margin:8px 0 0;">You've unlocked this achievement on TaskFlow</p>
                  </div>
                  <a href="https://todoperks.online/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;">Keep Going →</a>
                </div>
              </div>
            </body>
            </html>
            """.formatted(badgeEmoji, user.getName(), badgeName);
    }
}