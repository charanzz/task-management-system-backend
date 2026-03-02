package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${GMAIL_USERNAME:noreply@taskflow.com}")
    private String fromEmail;

    @Value("${FRONTEND_URL:https://www.todoperks.online}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTaskReminder(User user, Task task, String timeLabel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("⏰ TaskFlow: \"" + task.getTitle() + "\" is due " + timeLabel);
            helper.setText(buildReminderHtml(user, task, timeLabel), true);
            mailSender.send(message);
            System.out.println("✅ Reminder email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendBadgeEmail(User user, String badgeName, String badgeEmoji) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🏆 TaskFlow: You earned the " + badgeName + " badge!");
            helper.setText(buildBadgeHtml(user, badgeName, badgeEmoji), true);
            mailSender.send(message);
            System.out.println("✅ Badge email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send badge email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🔐 TaskFlow: Reset your password");
            helper.setText(buildResetHtml(user, resetUrl), true);
            mailSender.send(message);
            System.out.println("✅ Password reset email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send reset email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildReminderHtml(User user, Task task, String timeLabel) {
        String priorityColor = switch (task.getPriority().name()) {
            case "HIGH"   -> "#ff6b6b";
            case "MEDIUM" -> "#ffd93d";
            default       -> "#6bcb77";
        };
        return """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:32px;text-align:center;">
                  <div style="font-size:32px;margin-bottom:8px;">⏰</div>
                  <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">Task Reminder</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;">Don't let this slip through!</p>
                </div>
                <div style="padding:32px;">
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 20px;">Hi <strong style="color:#f0f0f8;">%s</strong>,</p>
                  <div style="background:#1a1a24;border-radius:14px;padding:20px;border-left:4px solid %s;margin-bottom:24px;">
                    <p style="color:#6b6b8a;font-size:10px;letter-spacing:2px;text-transform:uppercase;margin:0 0 8px;">DUE %s</p>
                    <h2 style="color:#f0f0f8;margin:0 0 8px;font-size:18px;">%s</h2>
                    %s
                    <span style="background:%s22;color:%s;padding:4px 12px;border-radius:20px;font-size:11px;font-weight:600;">%s Priority</span>
                  </div>
                  <div style="text-align:center;">
                    <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;font-size:14px;">Open TaskFlow →</a>
                  </div>
                </div>
                <div style="padding:16px 32px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
                  <p style="color:#6b6b8a;font-size:11px;margin:0;">© 2025 TaskFlow</p>
                </div>
              </div>
            </body></html>
            """.formatted(
                user.getName(), priorityColor, timeLabel.toUpperCase(), task.getTitle(),
                task.getDescription() != null ? "<p style='color:#a0a0b8;font-size:13px;margin:0 0 12px;'>" + task.getDescription() + "</p>" : "",
                priorityColor, priorityColor, task.getPriority().name(), frontendUrl
        );
    }

    private String buildBadgeHtml(User user, String badgeName, String badgeEmoji) {
        return """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:40px;text-align:center;">
                  <div style="font-size:64px;margin-bottom:12px;">%s</div>
                  <h1 style="color:#fff;margin:0;font-size:24px;font-weight:800;">Badge Unlocked!</h1>
                </div>
                <div style="padding:32px;text-align:center;">
                  <p style="color:#a0a0b8;font-size:15px;">Congratulations <strong style="color:#f0f0f8;">%s</strong>!</p>
                  <div style="background:#1a1a24;border-radius:14px;padding:24px;margin:20px 0;border:1px solid rgba(124,58,237,.2);">
                    <h2 style="color:#c084fc;margin:0 0 8px;font-size:22px;">%s</h2>
                    <p style="color:#6b6b8a;font-size:13px;margin:0;">You've unlocked this achievement on TaskFlow!</p>
                  </div>
                  <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;font-size:14px;">Keep Going →</a>
                </div>
              </div>
            </body></html>
            """.formatted(badgeEmoji, user.getName(), badgeName, frontendUrl);
    }

    private String buildResetHtml(User user, String resetUrl) {
        return """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:32px;text-align:center;">
                  <div style="font-size:40px;margin-bottom:12px;">🔐</div>
                  <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">Reset Your Password</h1>
                </div>
                <div style="padding:32px;">
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 16px;">Hi <strong style="color:#f0f0f8;">%s</strong>,</p>
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 24px;">We received a request to reset your TaskFlow password. Click the button below — this link expires in <strong style="color:#f0f0f8;">1 hour</strong>.</p>
                  <div style="text-align:center;margin-bottom:24px;">
                    <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:16px 40px;border-radius:12px;text-decoration:none;font-weight:700;font-size:15px;box-shadow:0 4px 20px rgba(124,58,237,.4);">Reset Password →</a>
                  </div>
                  <div style="background:#1a1a24;border-radius:10px;padding:14px;border:1px solid rgba(255,255,255,.06);">
                    <p style="color:#6b6b8a;font-size:12px;margin:0;">If you didn't request this, you can safely ignore this email. Your password won't change.</p>
                  </div>
                </div>
                <div style="padding:16px 32px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
                  <p style="color:#6b6b8a;font-size:11px;margin:0;">© 2025 TaskFlow · This link expires in 1 hour</p>
                </div>
              </div>
            </body></html>
            """.formatted(user.getName(), resetUrl);
    }
}