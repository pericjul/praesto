package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.model.BadgeRuleType;
import ch.zhaw.praesto.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

        List<Badge> badges = List.of(
            // Session Badges
            Badge.builder()
                .icon("🎯")
                .title("Erste Session")
                .description("Dein erstes KI-Training absolviert!")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(1)
                .sortOrder(1)
                .build(),
            
            Badge.builder()
                .icon("🔥")
                .title("Fleissig")
                .description("5 KI-Trainings gemacht")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(5)
                .sortOrder(2)
                .build(),
            
            Badge.builder()
                .icon("🏆")
                .title("Training-Profi")
                .description("10 KI-Trainings absolviert")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(10)
                .sortOrder(3)
                .build(),
            
            // Notizen Badges
            Badge.builder()
                .icon("📝")
                .title("Notizen-Starter")
                .description("Erste Notiz erstellt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(1)
                .sortOrder(4)
                .build(),
            
            Badge.builder()
                .icon("📚")
                .title("Notizen-Sammler")
                .description("10 Notizen gesammelt")
                .ruleType(BadgeRuleType.NOTES_CREATED)
                .threshold(10)
                .sortOrder(5)
                .build(),
            
            // Bewerbungen Badges
            Badge.builder()
                .icon("💼")
                .title("Bewerber")
                .description("Erste Bewerbung eingetragen")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(1)
                .sortOrder(6)
                .build(),
            
            Badge.builder()
                .icon("🚀")
                .title("Aktiv")
                .description("5 Bewerbungen eingetragen")
                .ruleType(BadgeRuleType.APPLICATIONS_CREATED)
                .threshold(5)
                .sortOrder(7)
                .build(),
            
            // Status Badges
            Badge.builder()
                .icon("📨")
                .title("Einladung")
                .description("Zum Gespräch eingeladen")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(0) // 0 = INVITED
                .sortOrder(8)
                .build(),
            
            Badge.builder()
                .icon("🎉")
                .title("Erfolg")
                .description("Erste Zusage erhalten")
                .ruleType(BadgeRuleType.APPLICATION_STATUS)
                .threshold(1) // 1 = ACCEPTED
                .sortOrder(9)
                .build()
        );

        badgeRepository.saveAll(badges);
        log.info("{} Badges erstellt", badges.size());
    }
}