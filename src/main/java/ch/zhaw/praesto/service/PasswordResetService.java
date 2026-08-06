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

    @Value("${praesto.base-url:https://praesto.ch}")
    private String baseUrl;
    @Value("${praesto.contact.from:}")
    private String fromAddress;

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
            throw new BadRequestException("Passwort muss mindestens 8 Zeichen haben.");
        }
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Ungültiger Link.");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT user_id, expires_at, used FROM password_reset_token WHERE token = ?", token);
        if (rows.isEmpty()) {
            throw new BadRequestException("Dieser Link ist ungültig.");
        }
        Map<String, Object> row = rows.get(0);
        if (Boolean.TRUE.equals(row.get("used"))) {
            throw new BadRequestException("Dieser Link wurde bereits verwendet.");
        }
        Timestamp expires = (Timestamp) row.get("expires_at");
        if (expires == null || expires.toInstant().isBefore(Instant.now())) {
            throw new BadRequestException("Dieser Link ist abgelaufen. Bitte fordere einen neuen an.");
        }
        String userId = (String) row.get("user_id");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Konto nicht gefunden."));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        jdbc.update("UPDATE password_reset_token SET used = true WHERE token = ?", token);
        log.info("Passwort per Reset-Link neu gesetzt für {}", user.getEmail());
    }

    private void sendMail(User user, String token) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        String link = baseUrl.replaceAll("/+$", "") + "/passwort-neu?token=" + token;
        if (sender == null || fromAddress == null || fromAddress.isBlank()) {
            log.warn("Passwort-Reset für {} angefragt, aber kein Mailversand konfiguriert. Link (nur Log): {}",
                    user.getEmail(), link);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setFrom(fromAddress);
            msg.setSubject("Praesto – Passwort zurücksetzen");
            msg.setText("Hallo " + user.getFullName() + ",\n\n"
                    + "du hast angefragt, dein Praesto-Passwort zurückzusetzen. "
                    + "Klicke auf den folgenden Link, um ein neues Passwort zu setzen:\n\n"
                    + link + "\n\n"
                    + "Der Link ist 1 Stunde gültig. Falls du das nicht warst, ignoriere diese E-Mail einfach – "
                    + "dein Passwort bleibt unverändert.\n\n"
                    + "Freundliche Grüsse\nDein Praesto-Team");
            sender.send(msg);
            log.info("Passwort-Reset-Mail an {} versendet.", user.getEmail());
        } catch (Exception e) {
            log.error("Passwort-Reset-Mail konnte nicht versendet werden: {}", e.getMessage());
        }
    }
}
