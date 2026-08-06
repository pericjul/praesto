package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Passwort-vergessen-Flow: erzeugt einen zeitlich begrenzten Reset-Token, verschickt ihn per
 * E-Mail und setzt damit ein neues Passwort. Schema-sicher über {@link JdbcTemplate}
 * (CREATE TABLE IF NOT EXISTS). Mailversand nur, wenn SMTP konfiguriert ist – die Anfrage
 * verrät nie, ob eine E-Mail existiert (kein Konto-Enumeration).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final LocaleMessages messages;

    @Value("${praesto.base-url:https://praesto.ch}")
    private String baseUrl;
    @Value("${praesto.contact.from:}")
    private String fromAddress;
    // Fallback-Absender, falls MAIL_FROM nicht gesetzt ist: das authentifizierte SMTP-Postfach.
    @Value("${spring.mail.username:}")
    private String mailUsername;

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS password_reset_token (
                        token varchar(80) PRIMARY KEY,
                        user_id varchar(64) NOT NULL,
                        expires_at timestamp NOT NULL,
                        used boolean DEFAULT false,
                        created_at timestamp
                    )
                    """);
            log.info("password_reset_token-Tabelle bereit.");
        } catch (Exception e) {
            log.error("password_reset_token-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Reset anfragen: Token erzeugen + Mail schicken. Gibt nie preis, ob die E-Mail existiert. */
    public void requestReset(String email) {
        if (email == null || !email.contains("@")) {
            return;
        }
        String normalized = email.toLowerCase().trim();
        userRepository.findByEmail(normalized).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            Instant now = Instant.now();
            jdbc.update("INSERT INTO password_reset_token (token, user_id, expires_at, used, created_at) "
                            + "VALUES (?,?,?,?,?)",
                    token, user.getId(), Timestamp.from(now.plus(TOKEN_VALIDITY)), false, Timestamp.from(now));
            sendMail(user, token);
        });
    }

    /** Neues Passwort per Token setzen. */
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException(messages.get("err.passwordMin8"));
        }
        if (token == null || token.isBlank()) {
            throw new BadRequestException(messages.get("err.resetLinkInvalid"));
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT user_id, expires_at, used FROM password_reset_token WHERE token = ?", token);
        if (rows.isEmpty()) {
            throw new BadRequestException(messages.get("err.resetLinkInvalid"));
        }
        Map<String, Object> row = rows.get(0);
        if (Boolean.TRUE.equals(row.get("used"))) {
            throw new BadRequestException(messages.get("err.resetLinkUsed"));
        }
        Timestamp expires = (Timestamp) row.get("expires_at");
        if (expires == null || expires.toInstant().isBefore(Instant.now())) {
            throw new BadRequestException(messages.get("err.resetLinkExpired"));
        }
        String userId = (String) row.get("user_id");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(messages.get("err.accountNotFound")));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        jdbc.update("UPDATE password_reset_token SET used = true WHERE token = ?", token);
        log.info("Passwort per Reset-Link neu gesetzt für {}", user.getEmail());
    }

    private void sendMail(User user, String token) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        String link = baseUrl.replaceAll("/+$", "") + "/passwort-neu?token=" + token;
        // Absender: MAIL_FROM, sonst das SMTP-Login-Postfach (wie beim Kontaktformular).
        String effectiveFrom = fromAddress != null && !fromAddress.isBlank() ? fromAddress : mailUsername;
        if (sender == null || effectiveFrom == null || effectiveFrom.isBlank()) {
            log.warn("Passwort-Reset für {} angefragt, aber kein Mailversand konfiguriert. Link (nur Log): {}",
                    user.getEmail(), link);
            return;
        }
        var nameVar = Map.of("NAME", user.getFullName());
        String greeting = messages.get("mail.reset.greeting", nameVar);
        String plain = greeting + "\n\n"
                + messages.get("mail.reset.introPlain") + "\n\n" + link + "\n\n"
                + messages.get("mail.reset.validity") + "\n\n" + messages.get("mail.reset.signature");
        try {
            var mime = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(mime, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(effectiveFrom);
            helper.setSubject(messages.get("mail.reset.subject"));
            helper.setText(plain, buildHtml(greeting, link));
            sender.send(mime);
            log.info("Passwort-Reset-Mail an {} versendet.", user.getEmail());
        } catch (Exception e) {
            log.error("Passwort-Reset-Mail konnte nicht versendet werden: {}", e.getMessage());
        }
    }

    private String buildHtml(String greeting, String link) {
        return """
                <div style="font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;max-width:480px;margin:0 auto;color:#2d2141;">
                  <div style="background:#2F124D;color:#fff;padding:20px 24px;border-radius:12px 12px 0 0;">
                    <span style="font-size:18px;font-weight:700;">Praesto</span>
                  </div>
                  <div style="background:#ffffff;border:1px solid #ece7f0;border-top:none;padding:24px;border-radius:0 0 12px 12px;">
                    <p style="margin:0 0 12px;">%s</p>
                    <p style="margin:0 0 20px;line-height:1.5;color:#4b4560;">%s</p>
                    <p style="text-align:center;margin:0 0 20px;">
                      <a href="%s" style="background:#2F124D;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:600;display:inline-block;">%s</a>
                    </p>
                    <p style="margin:0 0 8px;font-size:13px;color:#8b849a;line-height:1.5;">%s</p>
                    <p style="margin:16px 0 0;font-size:12px;color:#a49db2;word-break:break-all;">%s %s</p>
                  </div>
                </div>
                """.formatted(escape(greeting), escape(messages.get("mail.reset.introHtml")), link,
                escape(messages.get("mail.reset.button")), escape(messages.get("mail.reset.validity")),
                escape(messages.get("mail.reset.fallback")), link);
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
