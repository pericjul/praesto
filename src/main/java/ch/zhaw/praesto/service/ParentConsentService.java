package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Eltern-Einverständnis für Privat-/B2C-Konten (Minderjährige). Bei der Registrierung wird die
 * E-Mail eines Elternteils erfasst, eine Bestätigungs-Mail verschickt; erst nach dem Klick des
 * Elternteils wird der Zugang (Gratis-Testphase) freigeschaltet. Schema-sicher via JdbcTemplate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParentConsentService {

    private static final int TRIAL_DAYS = 7;

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final LocaleMessages messages;

    @Value("${praesto.base-url:https://praesto.ch}")
    private String baseUrl;
    @Value("${praesto.contact.from:}")
    private String fromAddress;
    // Gleiche verifizierte Absender-Kette wie das Kontaktformular (MAIL_FROM ->
    // CONTACT_RECIPIENT_EMAIL -> SMTP-Login), damit die Eltern-Mail auch ohne MAIL_FROM rausgeht.
    @Value("${praesto.contact.recipient:}")
    private String contactRecipient;
    @Value("${spring.mail.username:}")
    private String mailUsername;

    private String effectiveFrom() {
        if (fromAddress != null && !fromAddress.isBlank()) return fromAddress;
        if (contactRecipient != null && !contactRecipient.isBlank()) return contactRecipient;
        return mailUsername;
    }

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS parent_consent (
                        token varchar(80) PRIMARY KEY,
                        user_id varchar(64) NOT NULL,
                        parent_email varchar(255),
                        confirmed boolean DEFAULT false,
                        created_at timestamp
                    )
                    """);
            log.info("parent_consent-Tabelle bereit.");
        } catch (Exception e) {
            log.error("parent_consent-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Token anlegen und Bestätigungs-Mail an den Elternteil schicken. */
    public void createAndSend(User user, String parentEmail) {
        String token = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO parent_consent (token, user_id, parent_email, confirmed, created_at) "
                        + "VALUES (?,?,?,?,?)",
                token, user.getId(), parentEmail, false, Timestamp.from(Instant.now()));
        sendMail(parentEmail, user.getFirstName(), token);
    }

    /** Erneut senden (aktuellster offener Token oder ein neuer). */
    public void resendForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(messages.get("err.accountNotFound")));
        if (!user.needsParentConsent() || user.getParentEmail() == null) {
            return;
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT token FROM parent_consent WHERE user_id = ? AND confirmed = FALSE ORDER BY created_at DESC",
                userId);
        String token;
        if (!rows.isEmpty()) {
            token = (String) rows.get(0).get("token");
        } else {
            token = UUID.randomUUID().toString().replace("-", "");
            jdbc.update("INSERT INTO parent_consent (token, user_id, parent_email, confirmed, created_at) "
                            + "VALUES (?,?,?,?,?)",
                    token, userId, user.getParentEmail(), false, Timestamp.from(Instant.now()));
        }
        sendMail(user.getParentEmail(), user.getFirstName(), token);
    }

    /** Einverständnis bestätigen: schaltet Zugang + Gratis-Testphase frei. Gibt den Vornamen zurück. */
    public String confirm(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException(messages.get("err.consentLinkInvalid"));
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT user_id, confirmed FROM parent_consent WHERE token = ?", token);
        if (rows.isEmpty()) {
            throw new BadRequestException(messages.get("err.consentLinkInvalid"));
        }
        String userId = (String) rows.get(0).get("user_id");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(messages.get("err.consentLinkInvalid")));

        if (!Boolean.TRUE.equals(user.getParentConsentConfirmed())) {
            Instant now = Instant.now();
            user.setParentConsentConfirmed(true);
            user.setSubscriptionStatus("TRIAL");
            user.setTrialEndsAt(now.plus(Duration.ofDays(TRIAL_DAYS)));
            userRepository.save(user);
            log.info("Eltern-Einverständnis bestätigt für {} – Testphase gestartet.", user.getEmail());
        }
        jdbc.update("UPDATE parent_consent SET confirmed = TRUE WHERE token = ?", token);
        return user.getFirstName();
    }

    private void sendMail(String parentEmail, String childName, String token) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        String link = baseUrl.replaceAll("/+$", "") + "/eltern-bestaetigung?token=" + token;
        String effectiveFrom = effectiveFrom();
        if (sender == null || effectiveFrom == null || effectiveFrom.isBlank() || parentEmail == null) {
            log.warn("Eltern-Einverständnis für {} angefragt, aber kein Mailversand konfiguriert. Link (nur Log): {}",
                    childName, link);
            return;
        }
        var nameVar = Map.of("NAME", childName == null ? "Ihr Kind" : childName);
        String plain = messages.get("mail.consent.greeting") + "\n\n"
                + messages.get("mail.consent.intro", nameVar) + "\n\n"
                + messages.get("mail.consent.action") + "\n" + link + "\n\n"
                + messages.get("mail.consent.note") + "\n\n" + messages.get("mail.reset.signature");
        try {
            var mime = sender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(parentEmail);
            helper.setFrom(effectiveFrom);
            helper.setSubject(messages.get("mail.consent.subject"));
            helper.setText(plain, buildHtml(nameVar, link));
            sender.send(mime);
            log.info("Eltern-Einverständnis-Mail an {} versendet.", parentEmail);
        } catch (Exception e) {
            log.error("Eltern-Einverständnis-Mail konnte nicht versendet werden: {}", e.getMessage());
        }
    }

    private String buildHtml(Map<String, String> nameVar, String link) {
        return """
                <div style="font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;max-width:480px;margin:0 auto;color:#2d2141;">
                  <div style="background:#2F124D;color:#fff;padding:20px 24px;border-radius:12px 12px 0 0;">
                    <span style="font-size:18px;font-weight:700;">Praesto</span>
                  </div>
                  <div style="background:#ffffff;border:1px solid #ece7f0;border-top:none;padding:24px;border-radius:0 0 12px 12px;">
                    <p style="margin:0 0 12px;">%s</p>
                    <p style="margin:0 0 20px;line-height:1.5;color:#4b4560;">%s</p>
                    <p style="margin:0 0 8px;color:#4b4560;">%s</p>
                    <p style="text-align:center;margin:8px 0 20px;">
                      <a href="%s" style="background:#2F124D;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:600;display:inline-block;">%s</a>
                    </p>
                    <p style="margin:0;font-size:13px;color:#8b849a;line-height:1.5;">%s</p>
                  </div>
                </div>
                """.formatted(esc(messages.get("mail.consent.greeting")), esc(messages.get("mail.consent.intro", nameVar)),
                esc(messages.get("mail.consent.action")), link, esc(messages.get("mail.consent.button")),
                esc(messages.get("mail.consent.note")));
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
