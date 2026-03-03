package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Value("${RESEND_FROM_EMAIL:TaskFlow <onboarding@resend.dev>}")
    private String fromEmail;

    @Value("${FRONTEND_URL:https://www.todoperks.online}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String RESEND_API = "https://api.resend.com/emails";

    // ─── Core send method ────────────────────────────────────────────────────
    private void sendEmail(String to, String subject, String htmlBody) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            System.err.println("❌ RESEND_API_KEY is not set!");
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", fromEmail);
            body.put("to", new String[]{to});
            body.put("subject", subject);
            body.put("html", htmlBody);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Email sent to: " + to);
            } else {
                System.err.println("❌ Resend error: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("❌ Email send failed: " + e.getMessage());
        }
    }

    // ─── Task Reminder ───────────────────────────────────────────────────────
    public void sendTaskReminder(User user, Task task, String timeLabel) {
        String priorityColor = switch (task.getPriority().name()) {
            case "HIGH"   -> "#ff6b6b";
            case "MEDIUM" -> "#ffd93d";
            default       -> "#6bcb77";
        };
        String desc = task.getDescription() != null && !task.getDescription().isBlank()
            ? "<p style='color:#a0a0b8;font-size:13px;margin:0 0 12px;'>" + task.getDescription() + "</p>"
            : "";
        String html = """
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
            """.formatted(user.getName(), priorityColor, timeLabel.toUpperCase(),
                task.getTitle(), desc, priorityColor, priorityColor,
                task.getPriority().name(), frontendUrl);

        sendEmail(user.getEmail(), "⏰ TaskFlow: \"" + task.getTitle() + "\" is due " + timeLabel, html);
    }

    // ─── Badge Earned ─────────────────────────────────────────────────────────
    public void sendBadgeEmail(User user, String badgeName, String badgeEmoji) {
        String html = """
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
                    <p style="color:#6b6b8a;font-size:13px;margin:0;">You've earned this achievement on TaskFlow!</p>
                  </div>
                  <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 32px;border-radius:12px;text-decoration:none;font-weight:700;font-size:14px;">Keep Going →</a>
                </div>
              </div>
            </body></html>
            """.formatted(badgeEmoji, user.getName(), badgeName, frontendUrl);

        sendEmail(user.getEmail(), "🏆 TaskFlow: You earned the " + badgeName + " badge!", html);
    }

    // ─── Password Reset ───────────────────────────────────────────────────────
    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String html = """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:32px;text-align:center;">
                  <div style="font-size:40px;margin-bottom:12px;">🔐</div>
                  <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">Reset Your Password</h1>
                </div>
                <div style="padding:32px;">
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 16px;">Hi <strong style="color:#f0f0f8;">%s</strong>,</p>
                  <p style="color:#a0a0b8;font-size:14px;margin:0 0 24px;">We received a request to reset your TaskFlow password. Click below — this link expires in <strong style="color:#f0f0f8;">1 hour</strong>.</p>
                  <div style="text-align:center;margin-bottom:24px;">
                    <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:16px 40px;border-radius:12px;text-decoration:none;font-weight:700;font-size:15px;box-shadow:0 4px 20px rgba(124,58,237,.4);">Reset Password →</a>
                  </div>
                  <div style="background:#1a1a24;border-radius:10px;padding:14px;border:1px solid rgba(255,255,255,.06);">
                    <p style="color:#6b6b8a;font-size:12px;margin:0;">If you didn't request this, you can safely ignore this email.</p>
                  </div>
                </div>
                <div style="padding:16px 32px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;">
                  <p style="color:#6b6b8a;font-size:11px;margin:0;">© 2025 TaskFlow · Link expires in 1 hour</p>
                </div>
              </div>
            </body></html>
            """.formatted(user.getName(), resetUrl);

        sendEmail(user.getEmail(), "🔐 TaskFlow: Reset your password", html);
    }

    // ─── AI Digest ────────────────────────────────────────────────────────────
    public void sendAIDigestEmail(User user, String aiContent, String digestType) {
        String emoji = digestType.equals("Daily") ? "☀️" : "🧠";
        String title = digestType.equals("Daily") ? "Your Daily Digest" : "Weekly Coaching Report";
        String htmlContent = aiContent
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            .replace("\n", "<br>");

        String html = """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#0a0a0f;font-family:'Segoe UI',sans-serif;">
              <div style="max-width:600px;margin:40px auto;background:#111118;border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.08);">
                <div style="background:linear-gradient(135deg,#7c3aed,#a855f7);padding:36px;text-align:center;">
                  <div style="font-size:48px;margin-bottom:12px;">%s</div>
                  <h1 style="color:#fff;margin:0;font-size:24px;font-weight:800;">%s</h1>
                  <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;">Powered by Claude AI · TaskFlow</p>
                </div>
                <div style="padding:36px;">
                  <p style="color:#a0a0b8;font-size:15px;margin:0 0 24px;">Hi <strong style="color:#f0f0f8;">%s</strong> 👋</p>
                  <div style="background:#1a1a24;border-radius:14px;padding:24px;border:1px solid rgba(124,58,237,.15);line-height:1.8;color:#c0c0d8;font-size:14px;">%s</div>
                  <div style="text-align:center;margin-top:28px;">
                    <a href="%s/dashboard" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#a855f7);color:#fff;padding:14px 36px;border-radius:12px;text-decoration:none;font-weight:700;font-size:14px;">Open TaskFlow →</a>
                  </div>
                </div>
                <div style="padding:16px 36px;border-top:1px solid rgba(255,255,255,.06);text-align:center;">
                  <p style="color:#6b6b8a;font-size:11px;margin:0;">© 2025 TaskFlow · AI-powered productivity</p>
                </div>
              </div>
            </body></html>
            """.formatted(emoji, title, user.getName(), htmlContent, frontendUrl);

        sendEmail(user.getEmail(), emoji + " TaskFlow " + digestType + " — " + java.time.LocalDate.now(), html);
    }
}