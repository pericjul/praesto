package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.AccountType;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import ch.zhaw.praesto.service.LocaleMessages;
import ch.zhaw.praesto.service.SuperUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Aufbewahrungs-Regel für Privat-/B2C-Konten (Datenminimierung): Konten, die nach Ablauf der
 * Testphase seit {@code individual-days} nicht bezahlt und inaktiv sind, werden inkl. aller
 * Daten automatisch gelöscht. {@code warn-days} vorher geht eine einmalige Vorwarn-Mail raus.
 * Schul-/Bestandskonten sind NICHT betroffen.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountRetentionJob {

    private final UserRepository userRepository;
    private final SuperUserService superUserService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final LocaleMessages messages;

    @Value("${praesto.retention.enabled:true}")
    private boolean enabled;
    @Value("${praesto.retention.individual-days:180}")
    private long retentionDays;
    @Value("${praesto.retention.warn-days:14}")
    private long warnDays;
    @Value("${praesto.base-url:https://praesto.ch}")
    private String baseUrl;
    @Value("${praesto.contact.from:}")
    private String fromAddress;
    @Value("${spring.mail.username:}")
    private String mailUsername;

    // Täglich um 03:30 (Europe/Zurich).
    @Scheduled(cron = "0 30 3 * * *", zone = "Europe/Zurich")
    public void cleanupIndividualAccounts() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        int deleted = 0;
        int warned = 0;
        for (User user : userRepository.findByAccountType(AccountType.INDIVIDUAL)) {
            // Zahlt / Testphase aktiv -> nichts tun (evtl. gesetzte Vorwarnung zurücknehmen).
            if (user.hasSubscriptionAccess(now)) {
                if (user.getDeletionWarnedAt() != null) {
                    user.setDeletionWarnedAt(null);
                    userRepository.save(user);
                }
                continue;
            }
            Instant accessEnd = accessEnd(user);
            Instant deletionDate = accessEnd.plus(Duration.ofDays(retentionDays));

            if (now.isAfter(deletionDate)) {
                superUserService.deleteAccountAndData(user);
                deleted++;
                continue;
            }
            Instant warnDate = deletionDate.minus(Duration.ofDays(warnDays));
            if (now.isAfter(warnDate) && user.getDeletionWarnedAt() == null) {
                long daysLeft = Math.max(1, Duration.between(now, deletionDate).toDays());
                sendWarning(user, daysLeft);
                user.setDeletionWarnedAt(now);
                userRepository.save(user);
                warned++;
            }
        }
        if (deleted > 0 || warned > 0) {
            log.info("Aufbewahrungs-Job: {} Privat-Konten gelöscht, {} vorgewarnt.", deleted, warned);
        }
    }

    /** Zeitpunkt, ab dem der Zugang endete (Testphasen-/Abo-Ende, sonst Erstellung). */
    private Instant accessEnd(User user) {
        Instant end = user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now();
        if (user.getTrialEndsAt() != null && user.getTrialEndsAt().isAfter(end)) {
            end = user.getTrialEndsAt();
        }
        if (user.getSubscriptionEndsAt() != null && user.getSubscriptionEndsAt().isAfter(end)) {
            end = user.getSubscriptionEndsAt();
        }
        return end;
    }

    private void sendWarning(User user, long daysLeft) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        String effectiveFrom = fromAddress != null && !fromAddress.isBlank() ? fromAddress : mailUsername;
        if (sender == null || effectiveFrom == null || effectiveFrom.isBlank() || user.getEmail() == null) {
            log.warn("Löschungs-Vorwarnung für {} nicht versendet (kein Mailversand konfiguriert).", user.getEmail());
            return;
        }
        String link = baseUrl.replaceAll("/+$", "") + "/login";
        String greeting = messages.get("mail.reset.greeting",
                Map.of("NAME", user.getFirstName() == null ? "" : user.getFirstName()));
        String intro = messages.get("mail.retention.intro", Map.of("DAYS", String.valueOf(daysLeft)));
        String action = messages.get("mail.retention.action");
        String note = messages.get("mail.retention.note");
        String plain = greeting + "\n\n" + intro + "\n\n" + action + "\n" + link + "\n\n"
                + note + "\n\n" + messages.get("mail.reset.signature");
        try {
            var mime = sender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setFrom(effectiveFrom);
            helper.setSubject(messages.get("mail.retention.subject"));
            helper.setText(plain, buildHtml(greeting, intro, action, messages.get("mail.retention.button"), note, link));
            sender.send(mime);
            log.info("Löschungs-Vorwarnung an {} versendet (in {} Tagen).", user.getEmail(), daysLeft);
        } catch (Exception e) {
            log.error("Löschungs-Vorwarnung konnte nicht versendet werden: {}", e.getMessage());
        }
    }

    private String buildHtml(String greeting, String intro, String action, String button, String note, String link) {
        return """
                <div style="font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;max-width:480px;margin:0 auto;color:#2d2141;">
                  <div style="background:#2F124D;color:#fff;padding:20px 24px;border-radius:12px 12px 0 0;">
                    <span style="font-size:18px;font-weight:700;">Praesto</span>
                  </div>
                  <div style="background:#ffffff;border:1px solid #ece7f0;border-top:none;padding:24px;border-radius:0 0 12px 12px;">
                    <p style="margin:0 0 12px;">%s</p>
                    <p style="margin:0 0 16px;line-height:1.5;color:#4b4560;">%s</p>
                    <p style="margin:0 0 8px;color:#4b4560;">%s</p>
                    <p style="text-align:center;margin:8px 0 20px;">
                      <a href="%s" style="background:#2F124D;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:600;display:inline-block;">%s</a>
                    </p>
                    <p style="margin:0;font-size:13px;color:#8b849a;line-height:1.5;">%s</p>
                  </div>
                </div>
                """.formatted(esc(greeting), esc(intro), esc(action), link, esc(button), esc(note));
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
