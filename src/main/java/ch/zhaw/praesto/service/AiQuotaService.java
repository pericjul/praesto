package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.AiFeature;
import ch.zhaw.praesto.model.AiQuotaStatus;
import ch.zhaw.praesto.model.AiUsage;
import ch.zhaw.praesto.repository.AiUsageRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Festes KI-Nutzungs-Kontingent pro Schüler:in und Funktion (kein Nachladen).
 * Lehrer-Aufgaben laufen NICHT über dieses Kontingent. Schützt vor unkontrolliertem
 * Token-Verbrauch: max. Kosten = Schüler × Limits × Tokens pro Aufruf.
 */
@Service
@RequiredArgsConstructor
public class AiQuotaService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");

    private final AiUsageRepository aiUsageRepository;
    private final SessionRepository sessionRepository;

    @Value("${praesto.ai.limit.practice-interview:3}")
    private int limitPracticeInterview;

    // Freies Üben: 1 Gespräch pro Tag (füllt sich jeden Tag automatisch auf).
    @Value("${praesto.ai.limit.practice-per-day:1}")
    private int practicePerDay;

    @Value("${praesto.ai.limit.cv:1}")
    private int limitCv;

    @Value("${praesto.ai.limit.cover-letter:3}")
    private int limitCoverLetter;

    public int limitFor(AiFeature feature) {
        return switch (feature) {
            case PRACTICE_INTERVIEW -> limitPracticeInterview;
            case CV -> limitCv;
            case COVER_LETTER -> limitCoverLetter;
        };
    }

    public AiQuotaStatus status(String userId, AiFeature feature) {
        // Freies Üben ist UNBEGRENZT (nur je 15 Min pro Chat). limit/remaining = -1 = unbegrenzt.
        if (feature == AiFeature.PRACTICE_INTERVIEW) {
            return new AiQuotaStatus(feature.name(), -1, practiceUsedToday(userId), -1);
        }
        int used = aiUsageRepository.findByUserIdAndFeature(userId, feature)
                .map(AiUsage::getUsedTotal)
                .orElse(0);
        int limit = limitFor(feature);
        return new AiQuotaStatus(feature.name(), limit, used, Math.max(0, limit - used));
    }

    /** Anzahl freier Übungsgespräche, die der Schüler HEUTE schon gestartet hat. */
    public int practiceUsedToday(String userId) {
        Instant startOfToday = LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
        return (int) sessionRepository
                .countByStudentIdAndAssignmentIdIsNullAndStartedAtAfter(userId, startOfToday);
    }

    public int practicePerDay() {
        return practicePerDay;
    }

    /**
     * Zählt eine Nutzung kumulativ hoch, OHNE Limit-Prüfung (z.B. für den persistenten
     * "hat geübt"-Zähler im Lehrer-Cockpit). Die eigentliche Tages-Grenze prüft der
     * SessionService.
     */
    public void recordUse(String userId, String schoolId, AiFeature feature) {
        AiUsage usage = aiUsageRepository.findByUserIdAndFeature(userId, feature)
                .orElseGet(() -> AiUsage.builder()
                        .userId(userId)
                        .schoolId(schoolId)
                        .feature(feature)
                        .usedTotal(0)
                        .build());
        usage.setUsedTotal(usage.getUsedTotal() + 1);
        usage.setUpdatedAt(Instant.now());
        aiUsageRepository.save(usage);
    }

    public boolean hasRemaining(String userId, AiFeature feature) {
        return status(userId, feature).remaining() > 0;
    }

    /**
     * Verbraucht 1 Kontingent. Wirft eine BadRequestException, wenn das Limit erreicht ist.
     */
    public void consume(String userId, String schoolId, AiFeature feature) {
        AiUsage usage = aiUsageRepository.findByUserIdAndFeature(userId, feature)
                .orElseGet(() -> AiUsage.builder()
                        .userId(userId)
                        .schoolId(schoolId)
                        .feature(feature)
                        .usedTotal(0)
                        .build());

        if (usage.getUsedTotal() >= limitFor(feature)) {
            throw new BadRequestException(quotaMessage(feature));
        }
        usage.setUsedTotal(usage.getUsedTotal() + 1);
        usage.setUpdatedAt(Instant.now());
        aiUsageRepository.save(usage);
    }

    private String quotaMessage(AiFeature feature) {
        return switch (feature) {
            case PRACTICE_INTERVIEW ->
                    "Du hast deine 3 eigenen Übungsgespräche aufgebraucht. Aufgaben deiner Lehrperson kannst du weiterhin machen.";
            case CV -> "Du hast deinen Lebenslauf bereits erstellt.";
            case COVER_LETTER -> "Du hast dein Kontingent für Bewerbungsschreiben (3) aufgebraucht.";
        };
    }
}
