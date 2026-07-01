package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Legt eine vollständige, BESCHREIBBARE Test-Schule mit echten Accounts (mit Passwort)
 * an, damit alle Rollen inkl. positiver und negativer Fälle durchgetestet werden können.
 *
 * Läuft NUR, wenn {@code praesto.seed-test-data=true} (Azure: SEED_TEST_DATA=true) gesetzt
 * ist – und nur, wenn die Test-Schule noch nicht existiert (idempotent). Bei Fehlern bricht
 * der App-Start NICHT ab.
 */
@Component
@Order(4)
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder implements CommandLineRunner {

    /** Passwort aller Test-Accounts (Test-only, danach Accounts löschen). */
    private static final String PW = "PilotTest2026!";
    private static final String SCHOOL_NAME = "Testschule Pilot";

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    // Akzeptiert die Azure-Umgebungsvariable SEED_TEST_DATA (true/false) direkt.
    @Value("${SEED_TEST_DATA:false}")
    private boolean seedTestData;

    @Override
    public void run(String... args) {
        if (!seedTestData) {
            return;
        }
        try {
            if (schoolRepository.findByName(SCHOOL_NAME).isPresent()) {
                log.info("Test-Schule '{}' existiert bereits – überspringe Test-Seeding.", SCHOOL_NAME);
                return;
            }
            seed();
        } catch (Exception e) {
            log.error("Test-Seeding fehlgeschlagen (App läuft normal weiter): {}", e.getMessage(), e);
        }
    }

    private void seed() {
        Instant now = Instant.now();
        log.info("Erstelle Test-Daten für Rollen-Tests...");

        School school = schoolRepository.save(School.builder()
                .name(SCHOOL_NAME).canton("ZH").city("Zürich")
                .isActive(true).createdAt(now).build());
        String schoolId = school.getId();

        // ----- Accounts -----
        createUser("admin.test@praesto.ch", "Test", "Schulleitung", UserRole.SCHOOL_ADMIN, schoolId, now);
        User teacher = createUser("lehrer.test@praesto.ch", "Test", "Lehrperson", UserRole.TEACHER, schoolId, now);
        User s1 = createUser("schueler1.test@praesto.ch", "Test", "Schüler 1", UserRole.STUDENT, schoolId, now);
        User s2 = createUser("schueler2.test@praesto.ch", "Test", "Schüler 2", UserRole.STUDENT, schoolId, now);
        User s3 = createUser("schueler3.test@praesto.ch", "Test", "Schüler 3", UserRole.STUDENT, schoolId, now);

        // ----- Klasse -----
        SchoolClass klasse = schoolClassRepository.save(SchoolClass.builder()
                .schoolId(schoolId).name("Testklasse 3T").teacherId(teacher.getId())
                .studentIds(new ArrayList<>(List.of(s1.getId(), s2.getId(), s3.getId())))
                .createdAt(now).updatedAt(now).build());

        // ----- Aufgaben (offen, überfällig, verschiedene Typen) -----
        Assignment interview = assignmentRepository.save(assignment(schoolId, klasse.getId(), teacher.getId(),
                "Bewerbungsgespräch üben (KI)", "Führe ein KI-Bewerbungsgespräch und reiche es ein.",
                AssignmentType.AI_INTERVIEW, 15, now.plus(2, ChronoUnit.DAYS), now));

        assignmentRepository.save(assignment(schoolId, klasse.getId(), teacher.getId(),
                "Überfällige Übung (KI)", "Diese Aufgabe ist bereits abgelaufen – Test des Überfällig-Falls.",
                AssignmentType.AI_INTERVIEW, 15, now.minus(1, ChronoUnit.DAYS), now));

        Assignment reflexion = assignmentRepository.save(assignment(schoolId, klasse.getId(), teacher.getId(),
                "Kurze Selbstreflexion", "Schreibe ein paar Sätze zu deiner Motivation.",
                AssignmentType.SELF_REFLECTION, null, now.plus(3, ChronoUnit.DAYS), now));

        assignmentRepository.save(assignment(schoolId, klasse.getId(), teacher.getId(),
                "Lebenslauf hochladen", "Lade deinen Lebenslauf als PDF hoch.",
                AssignmentType.DOCUMENT_UPLOAD, null, now.plus(4, ChronoUnit.DAYS), now));

        // ----- Abgaben: eine ZU BEWERTEN (für die Lehrperson), eine BEREITS BEWERTET (für Schüler:in) -----
        submissionRepository.save(Submission.builder()
                .schoolId(schoolId).assignmentId(reflexion.getId())
                .studentId(s1.getId()).studentEmail(s1.getEmail())
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Ich möchte KV lernen, weil ich gerne organisiert arbeite und Kundenkontakt mag.")
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(now.minus(2, ChronoUnit.HOURS))
                .build());

        submissionRepository.save(Submission.builder()
                .schoolId(schoolId).assignmentId(interview.getId())
                .studentId(s2.getId()).studentEmail(s2.getEmail())
                .type(AssignmentType.AI_INTERVIEW)
                .status(SubmissionStatus.REVIEWED)
                .submittedAt(now.minus(1, ChronoUnit.DAYS))
                .teacherFeedback("Gut gemacht! Nächstes Mal noch ein konkretes Beispiel nennen.")
                .grade(5)
                .reviewedAt(now.minus(2, ChronoUnit.HOURS))
                .reviewedByTeacherId(teacher.getId())
                .build());

        // ----- Schüler 1: abgeschlossenes Übungsgespräch, Notiz, Bewerbung -----
        sessionRepository.save(Session.builder()
                .schoolId(schoolId).studentId(s1.getId()).studentEmail(s1.getEmail())
                .status(SessionStatus.CLOSED)
                .startedAt(now.minus(1, ChronoUnit.DAYS))
                .closedAt(now.minus(1, ChronoUnit.DAYS).plus(15, ChronoUnit.MINUTES))
                .messages(sampleMessages(now.minus(1, ChronoUnit.DAYS)))
                .score(62).scoreReason("Solide Antworten – gib mehr konkrete Beispiele.")
                .submittedAsAssignment(false)
                .build());

        noteRepository.save(Note.builder()
                .studentId(s1.getId()).companyName("Muster AG").position("KV EFZ")
                .text("Schnuppertag am Mittwoch. Ansprechperson: Frau Muster.")
                .createdAt(now).lastUpdated(now).build());

        applicationRepository.save(Application.builder()
                .studentId(s1.getId()).companyName("Muster AG").position("KV EFZ")
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDate.now())
                .notes("Online-Bewerbung über yousty.ch abgeschickt.")
                .createdAt(now).updatedAt(now).build());

        log.info("Test-Daten erstellt: Schule '{}', 1 Admin, 1 Lehrer, 3 Schüler, 1 Klasse, 4 Aufgaben.", SCHOOL_NAME);
    }

    private User createUser(String email, String firstName, String lastName, UserRole role, String schoolId, Instant now) {
        return userRepository.save(User.builder()
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(PW))
                .firstName(firstName).lastName(lastName)
                .role(role).schoolId(schoolId).isActive(true).createdAt(now).build());
    }

    private Assignment assignment(String schoolId, String classId, String teacherId, String title,
                                  String desc, AssignmentType type, Integer durationMin, Instant due, Instant now) {
        return Assignment.builder()
                .schoolId(schoolId).title(title).description(desc)
                .durationMin(durationMin).dueDate(due).classId(classId).type(type)
                .status(AssignmentStatus.ASSIGNED).createdByTeacherId(teacherId).createdAt(now)
                .build();
    }

    private List<SessionMessage> sampleMessages(Instant base) {
        List<SessionMessage> m = new ArrayList<>();
        String[][] turns = {
                {"ASSISTANT", "Willkommen! Für welchen Beruf möchtest du üben?"},
                {"USER", "Für eine KV-Lehrstelle."},
                {"ASSISTANT", "Super! Warum interessiert dich der Beruf?"},
                {"USER", "Ich arbeite gerne organisiert und mit Menschen."}
        };
        for (int i = 0; i < turns.length; i++) {
            m.add(SessionMessage.builder().role(turns[i][0]).content(turns[i][1])
                    .createdAt(base.plus(i, ChronoUnit.MINUTES)).build());
        }
        return m;
    }
}
