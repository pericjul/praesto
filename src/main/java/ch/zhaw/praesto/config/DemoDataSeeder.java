package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Erstellt eine vollständige Demo-Schule (Schule, Schulleitung, 2 Lehrer, 3 Klassen,
 * 15 Schüler, Aufgaben, Sessions, Abgaben mit Feedback) sowie einen read-only
 * DEMO_USER. Läuft nur, wenn die Demo-Schule noch nicht existiert.
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo1234!";

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${praesto.demo.school-name:Demo Schule Praesto}")
    private String demoSchoolName;

    @Override
    public void run(String... args) {
        if (schoolRepository.findByName(demoSchoolName).isPresent()) {
            log.info("Demo-Schule existiert bereits, überspringe Demo-Seeding");
            return;
        }
        seedDemoData();
    }

    /**
     * Nächtliches Reset (täglich 03:00 Europe/Zurich): Demo-Schule löschen und frisch
     * neu aufsetzen, damit Besucher-Änderungen verschwinden.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Zurich")
    @Transactional
    public void nightlyReset() {
        log.info("Nächtliches Demo-Reset startet...");
        wipeDemoData();
        seedDemoData();
        log.info("Demo-Schule wurde zurückgesetzt");
    }

    /**
     * Löscht alle Daten der Demo-Schule: User, Klassen, Aufgaben, Sessions, Abgaben,
     * Invites sowie Notizen/Bewerbungen/Badges der Demo-Nutzer – und die Schule selbst.
     */
    private void wipeDemoData() {
        schoolRepository.findByName(demoSchoolName).ifPresent(school -> {
            String schoolId = school.getId();
            List<String> userIds = userRepository.findBySchoolId(schoolId).stream()
                    .map(User::getId)
                    .toList();
            if (!userIds.isEmpty()) {
                noteRepository.deleteByStudentIdIn(userIds);
                applicationRepository.deleteByStudentIdIn(userIds);
                userBadgeRepository.deleteByStudentIdIn(userIds);
            }
            submissionRepository.deleteBySchoolId(schoolId);
            sessionRepository.deleteBySchoolId(schoolId);
            assignmentRepository.deleteBySchoolId(schoolId);
            schoolClassRepository.deleteBySchoolId(schoolId);
            inviteTokenRepository.deleteBySchoolId(schoolId);
            userRepository.deleteBySchoolId(schoolId);
            schoolRepository.delete(school);
            log.info("Demo-Schule '{}' geleert", demoSchoolName);
        });
    }

    private void seedDemoData() {
        log.info("Erstelle Demo-Daten...");
        Instant now = Instant.now();

        // ----- Schule -----
        School school = schoolRepository.save(School.builder()
                .name(demoSchoolName)
                .canton("ZH")
                .city("Zürich")
                .isActive(true)
                .createdAt(now)
                .build());
        String schoolId = school.getId();

        // ----- Schulleitung + Demo-User -----
        createUser("demo-admin@praesto.ch", "Demo", "Admin", UserRole.SCHOOL_ADMIN, schoolId, DEMO_PASSWORD, now);
        User mueller = createUser("lehrerin.mueller@demo.praesto.ch", "Anna", "Müller", UserRole.TEACHER, schoolId, DEMO_PASSWORD, now);
        User schmidt = createUser("lehrer.schmidt@demo.praesto.ch", "Thomas", "Schmidt", UserRole.TEACHER, schoolId, DEMO_PASSWORD, now);

        // read-only Demo-Account (Login via /demo, kein Passwort nötig)
        createUser("demo@praesto.ch", "Demo", "Besucher", UserRole.DEMO_USER, schoolId, DEMO_PASSWORD, now);

        // ----- 15 Schüler -----
        List<User> students = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            students.add(createUser("schueler" + i + "@demo.praesto.ch",
                    "Schüler", String.valueOf(i), UserRole.STUDENT, schoolId, DEMO_PASSWORD, now));
        }

        // ----- Klassen -----
        SchoolClass class3a = createClass(schoolId, "3a", mueller.getId(), studentIds(students, 1, 8), now);
        createClass(schoolId, "3b", schmidt.getId(), studentIds(students, 9, 15), now);
        createClass(schoolId, "KV2024", mueller.getId(), List.of(
                students.get(0).getId(), students.get(4).getId(), students.get(8).getId()), now);

        // ----- Aufgaben (für Klasse 3a) -----
        Assignment interview = assignmentRepository.save(Assignment.builder()
                .schoolId(schoolId)
                .title("Bewerbungsgespräch üben")
                .description("Übe ein realistisches Bewerbungsgespräch mit dem KI-Coach.")
                .durationMin(20)
                .dueDate(now.plus(1, ChronoUnit.DAYS))
                .classId(class3a.getId())
                .type(AssignmentType.AI_INTERVIEW)
                .status(AssignmentStatus.ASSIGNED)
                .createdByTeacherId(mueller.getId())
                .createdAt(now)
                .build());

        Assignment motivation = assignmentRepository.save(Assignment.builder()
                .schoolId(schoolId)
                .title("Motivationsschreiben")
                .description("Schreibe eine kurze Selbstreflexion zu deiner Motivation.")
                .dueDate(now.plus(2, ChronoUnit.DAYS))
                .classId(class3a.getId())
                .type(AssignmentType.SELF_REFLECTION)
                .status(AssignmentStatus.ASSIGNED)
                .createdByTeacherId(mueller.getId())
                .createdAt(now)
                .build());

        // ----- Sessions (3 abgeschlossene für schueler1) -----
        User schueler1 = students.get(0);
        for (int s = 1; s <= 3; s++) {
            sessionRepository.save(Session.builder()
                    .schoolId(schoolId)
                    .studentId(schueler1.getId())
                    .studentEmail(schueler1.getEmail())
                    .status(SessionStatus.CLOSED)
                    .startedAt(now.minus(s, ChronoUnit.DAYS))
                    .closedAt(now.minus(s, ChronoUnit.DAYS).plus(20, ChronoUnit.MINUTES))
                    .messages(demoMessages(now.minus(s, ChronoUnit.DAYS)))
                    .submittedAsAssignment(false)
                    .build());
        }

        // ----- Abgaben mit Feedback -----
        submissionRepository.save(Submission.builder()
                .schoolId(schoolId)
                .assignmentId(interview.getId())
                .studentId(schueler1.getId())
                .studentEmail(schueler1.getEmail())
                .type(AssignmentType.AI_INTERVIEW)
                .status(SubmissionStatus.REVIEWED)
                .submittedAt(now.minus(2, ChronoUnit.DAYS))
                .teacherFeedback("Gut gemacht! Achte beim nächsten Mal auf konkrete Beispiele.")
                .grade(5)
                .reviewedAt(now.minus(1, ChronoUnit.DAYS))
                .reviewedByTeacherId(mueller.getId())
                .build());

        submissionRepository.save(Submission.builder()
                .schoolId(schoolId)
                .assignmentId(motivation.getId())
                .studentId(students.get(1).getId())
                .studentEmail(students.get(1).getEmail())
                .type(AssignmentType.SELF_REFLECTION)
                .textContent("Ich interessiere mich für diesen Beruf, weil ich gerne mit Menschen arbeite ...")
                .status(SubmissionStatus.REVIEWED)
                .submittedAt(now.minus(2, ChronoUnit.DAYS))
                .teacherFeedback("Schöne Reflexion – etwas mehr Details zu deinen Stärken wären toll.")
                .grade(5)
                .reviewedAt(now.minus(1, ChronoUnit.DAYS))
                .reviewedByTeacherId(mueller.getId())
                .build());

        log.info("Demo-Daten erstellt: Schule '{}', 1 Admin, 2 Lehrer, 15 Schüler, 3 Klassen, 2 Aufgaben",
                demoSchoolName);
    }

    private User createUser(String email, String firstName, String lastName,
                            UserRole role, String schoolId, String rawPassword, Instant now) {
        return userRepository.save(User.builder()
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .schoolId(schoolId)
                .isActive(true)
                .createdAt(now)
                .build());
    }

    private SchoolClass createClass(String schoolId, String name, String teacherId,
                                    List<String> studentIds, Instant now) {
        return schoolClassRepository.save(SchoolClass.builder()
                .schoolId(schoolId)
                .name(name)
                .teacherId(teacherId)
                .studentIds(new ArrayList<>(studentIds))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    /** User-Ids der Schüler von 1-basiert fromInclusive..toInclusive. */
    private List<String> studentIds(List<User> students, int fromInclusive, int toInclusive) {
        List<String> ids = new ArrayList<>();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            ids.add(students.get(i - 1).getId());
        }
        return ids;
    }

    private List<SessionMessage> demoMessages(Instant base) {
        List<SessionMessage> messages = new ArrayList<>();
        String[][] turns = {
                {"ASSISTANT", "Willkommen zum Bewerbungsgespräch! Für welchen Beruf möchtest du üben?"},
                {"USER", "Für eine KV-Lehrstelle."},
                {"ASSISTANT", "Super! Warum interessierst du dich für eine KV-Lehrstelle?"},
                {"USER", "Ich arbeite gerne organisiert und mit Menschen."},
                {"ASSISTANT", "Schöne Antwort! Erzähl mir von einer Stärke und einem Beispiel dazu."}
        };
        for (int i = 0; i < turns.length; i++) {
            messages.add(SessionMessage.builder()
                    .role(turns[i][0])
                    .content(turns[i][1])
                    .createdAt(base.plus(i, ChronoUnit.MINUTES))
                    .build());
        }
        return messages;
    }
}
