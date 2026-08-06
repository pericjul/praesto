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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        String body = messages.get("mail.retention.body", Map.of(
                "NAME", user.getFirstName() == null ? "" : user.getFirstName(),
                "DAYS", String.valueOf(daysLeft),
                "URL", baseUrl.replaceAll("/+$", "") + "/login"));
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setFrom(effectiveFrom);
            msg.setSubject(messages.get("mail.retention.subject"));
            msg.setText(body);
            sender.send(msg);
            log.info("Löschungs-Vorwarnung an {} versendet (in {} Tagen).", user.getEmail(), daysLeft);
        } catch (Exception e) {
            log.error("Löschungs-Vorwarnung konnte nicht versendet werden: {}", e.getMessage());
        }
    }
}
