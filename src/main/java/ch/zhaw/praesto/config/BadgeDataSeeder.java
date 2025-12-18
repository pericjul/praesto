package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.model.BadgeRuleType;
import ch.zhaw.praesto.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class BadgeDataSeeder implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(String... args) {
        // Nur seeden wenn keine Badges existieren
        if (badgeRepository.count() > 0) {
            log.info("Badges bereits vorhanden, überspringe Seeding");
            return;
        }

        log.info("Erstelle initiale Badges...");

        List<Badge> badges = new ArrayList<>();
        int sortOrder = 1;

        // ========== KI-TRAINING BADGES (8 Stück) ==========
        badges.add(Badge.builder()
                .icon("🎯")
                .title("Erste Session")
                .description("Dein erstes KI-Training absolviert!")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🔥")
                .title("Dreifach trainiert")
                .description("3 KI-Trainings gemacht")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(3)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("💪")
                .title("Fleissig")
                .description("5 KI-Trainings absolviert")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🏆")
                .title("Training-Profi")
                .description("10 KI-Trainings gemeistert")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("⭐")
                .title("Ausdauernd")
                .description("15 KI-Trainings absolviert")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(15)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🌟")
                .title("Training-Champion")
                .description("25 KI-Trainings geschafft")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(25)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("👑")
                .title("Halb-Hunderter")
                .description("50 KI-Trainings absolviert!")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(50)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🏅")
                .title("Training-Legende")
                .description("100 KI-Trainings - unglaublich!")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(100)
                .sortOrder(sortOrder++)
                .build());

        // ========== NOTIZEN BADGES (5 Stück) ==========
        badges.add(Badge.builder()
                .icon("📝")
                .title("Notizen-Starter")
                .description("Erste Notiz erstellt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📒")
                .title("Fleissiger Schreiber")
                .description("5 Notizen gesammelt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📚")
                .title("Notizen-Sammler")
                .description("10 Notizen erstellt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📖")
                .title("Dokumentations-Profi")
                .description("25 Notizen gesammelt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(25)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🗂️")
                .title("Archiv-Meister")
                .description("50 Notizen erstellt!")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(50)
                .sortOrder(sortOrder++)
                .build());

        // ========== BEWERBUNGEN ANZAHL BADGES (6 Stück) ==========
        badges.add(Badge.builder()
                .icon("💼")
                .title("Bewerber")
                .description("Erste Bewerbung eingetragen")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📋")
                .title("Aktiver Bewerber")
                .description("3 Bewerbungen eingetragen")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(3)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🚀")
                .title("Bewerbungs-Profi")
                .description("5 Bewerbungen gesammelt")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🎯")
                .title("Zielstrebig")
                .description("10 Bewerbungen eingetragen")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("💎")
                .title("Bewerbungs-Champion")
                .description("15 Bewerbungen gesammelt")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(15)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🏆")
                .title("Bewerbungs-Legende")
                .description("25 Bewerbungen - beeindruckend!")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(25)
                .sortOrder(sortOrder++)
                .build());

        // ========== BEWERBUNGS-STATUS BADGES (4 Stück) ==========
        badges.add(Badge.builder()
                .icon("✉️")
                .title("Abgeschickt")
                .description("Erste Bewerbung versendet")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(0)  // APPLIED
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📨")
                .title("Einladung erhalten")
                .description("Zum Gespräch eingeladen!")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(1)  // INVITED
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🤝")
                .title("Gespräch geführt")
                .description("Bewerbungsgespräch absolviert")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(2)  // INTERVIEW_DONE
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🎉")
                .title("Erfolg!")
                .description("Zusage erhalten - Herzlichen Glückwunsch!")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(3)  // ACCEPTED
                .sortOrder(sortOrder++)
                .build());

        // ========== AUFGABEN-ABGABE BADGES (6 Stück) ==========
        badges.add(Badge.builder()
                .icon("✅")
                .title("Erste Abgabe")
                .description("Erste Aufgabe abgegeben")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📤")
                .title("Fleissig abgegeben")
                .description("3 Aufgaben abgegeben")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(3)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🎒")
                .title("Aufgaben-Sammler")
                .description("5 Aufgaben erledigt")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📊")
                .title("Aufgaben-Profi")
                .description("10 Aufgaben abgegeben")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🌟")
                .title("Fleissiger Schüler")
                .description("20 Aufgaben erledigt!")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(20)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🏅")
                .title("Aufgaben-Meister")
                .description("50 Aufgaben abgegeben - wow!")
                .ruleType(BadgeRuleType.SUBMISSIONS_COMPLETED)
                .threshold(50)
                .sortOrder(sortOrder++)
                .build());

        // ========== FEEDBACK BADGES (4 Stück) ==========
        badges.add(Badge.builder()
                .icon("💬")
                .title("Erstes Feedback")
                .description("Feedback von der Lehrperson erhalten")
                .ruleType(BadgeRuleType.FEEDBACK_RECEIVED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📣")
                .title("Feedback-Sammler")
                .description("5x Feedback erhalten")
                .ruleType(BadgeRuleType.FEEDBACK_RECEIVED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🗣️")
                .title("Gut betreut")
                .description("10x Feedback erhalten")
                .ruleType(BadgeRuleType.FEEDBACK_RECEIVED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📢")
                .title("Feedback-Champion")
                .description("25x Feedback erhalten")
                .ruleType(BadgeRuleType.FEEDBACK_RECEIVED)
                .threshold(25)
                .sortOrder(sortOrder++)
                .build());

        // ========== NOTEN BADGES (4 Stück) ==========
        badges.add(Badge.builder()
                .icon("📊")
                .title("Erste Note")
                .description("Erste Bewertung erhalten")
                .ruleType(BadgeRuleType.GRADES_RECEIVED)
                .threshold(1)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📈")
                .title("Noten-Sammler")
                .description("5 Bewertungen erhalten")
                .ruleType(BadgeRuleType.GRADES_RECEIVED)
                .threshold(5)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("📉")
                .title("Gut bewertet")
                .description("10 Bewertungen erhalten")
                .ruleType(BadgeRuleType.GRADES_RECEIVED)
                .threshold(10)
                .sortOrder(sortOrder++)
                .build());

        badges.add(Badge.builder()
                .icon("🎓")
                .title("Bewertungs-Profi")
                .description("25 Bewertungen erhalten")
                .ruleType(BadgeRuleType.GRADES_RECEIVED)
                .threshold(25)
                .sortOrder(sortOrder)
                .build());

        badgeRepository.saveAll(badges);
        log.info("{} Badges erstellt", badges.size());
    }
}