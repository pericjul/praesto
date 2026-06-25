package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

        private final UserService userService;
        private final SchoolClassService schoolClassService;
        private final AssignmentRepository assignmentRepository;
        private final SessionRepository sessionRepository;
        private final ApplicationRepository applicationRepository;
        private final BadgeService badgeService;
        private final SubmissionRepository submissionRepository;

        public StudentDashboardResponse getDashboardForCurrentStudent() {
                String studentId = userService.getUserId();
                String studentName = userService.getUserName();
                String studentEmail = userService.getEmail().toLowerCase();

                // ClassId über SchoolClassService holen
                String classId = schoolClassService.getMyClassId();

                // Assignments der Klasse holen
                List<Assignment> allAssignments;
                if (classId != null && !classId.isBlank()) {
                        allAssignments = assignmentRepository.findByClassId(classId);
                } else {
                        allAssignments = assignmentRepository.findAll();
                }

                // Bereits abgegebene Assignment-IDs holen
                List<Submission> mySubmissions = submissionRepository.findByStudentEmail(studentEmail);
                List<String> submittedAssignmentIds = mySubmissions.stream()
                                .map(Submission::getAssignmentId)
                                .collect(Collectors.toList());

                // Nur offene Assignments (nicht abgegeben, nicht CLOSED)
                List<Assignment> openAssignments = allAssignments.stream()
                                .filter(a -> a.getStatus() != AssignmentStatus.CLOSED)
                                .filter(a -> !submittedAssignmentIds.contains(a.getId()))
                                .sorted(Comparator.comparing(
                                                Assignment::getDueDate,
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .collect(Collectors.toList());

                // Für Frontend: Assignment-Infos aufbereiten
                List<AssignmentInfo> openAssignmentInfos = openAssignments.stream()
                                .map(a -> AssignmentInfo.builder()
                                                .id(a.getId())
                                                .title(a.getTitle())
                                                .type(a.getType() != null ? a.getType().name() : null)
                                                .dueDate(a.getDueDate())
                                                .durationMin(a.getDurationMin())
                                                .build())
                                .collect(Collectors.toList());

                // Sessions
                List<Session> sessions = sessionRepository.findByStudentId(studentId);
                int totalSessionsCount = (int) sessions.stream()
                                .filter(s -> s.getStatus() == SessionStatus.CLOSED)
                                .count();

                Session lastSession = sessions.stream()
                                .max(Comparator.comparing(Session::getStartedAt))
                                .orElse(null);

                Session openSession = sessions.stream()
                                .filter(s -> s.getStatus() == SessionStatus.OPEN)
                                .max(Comparator.comparing(Session::getStartedAt))
                                .orElse(null);

                // Übungs-Streak: aufeinanderfolgende Tage mit mind. einer Session
                int streakDays = calculateStreak(sessions);

                // Bewerbungen
                List<Application> myApplications = applicationRepository.findByStudentId(studentId);
                int applicationsCount = myApplications.size();

                // Badges
                long badgesCount = badgeService.getEarnedBadgeCount(studentId);

                // Verdiente Badge-Icons holen
                List<String> earnedBadgeIcons = badgeService.getBadgesWithEarnedInfo(studentId).stream()
                                .filter(BadgeService.BadgeWithEarnedInfo::isEarned)
                                .map(b -> b.getBadge().getIcon())
                                .collect(Collectors.toList());

                // Mitteilungen: Neues Feedback (letzte 7 Tage)
                List<NotificationInfo> notifications = new ArrayList<>();

                Instant sevenDaysAgo = Instant.now().minusSeconds(7L * 24 * 60 * 60);

                // Feedback-Mitteilungen
                mySubmissions.stream()
                                .filter(s -> s.getTeacherFeedback() != null)
                                .filter(s -> s.getSubmittedAt() != null && s.getSubmittedAt().isAfter(sevenDaysAgo))
                                .forEach(s -> {
                                        // Assignment-Titel holen
                                        String assignmentTitle = allAssignments.stream()
                                                        .filter(a -> a.getId().equals(s.getAssignmentId()))
                                                        .map(Assignment::getTitle)
                                                        .findFirst()
                                                        .orElse("Aufgabe");

                                        notifications.add(NotificationInfo.builder()
                                                        .type("FEEDBACK")
                                                        .icon("💬")
                                                        .title("Neues Feedback")
                                                        .message("Feedback für \"" + assignmentTitle + "\" erhalten")
                                                        .assignmentId(s.getAssignmentId())
                                                        .grade(s.getGrade())
                                                        .createdAt(s.getSubmittedAt())
                                                        .build());
                                });

                // Nach Datum sortieren (neueste zuerst)
                notifications.sort(Comparator.comparing(NotificationInfo::getCreatedAt).reversed());

                // Anstehende Termine: Aufgaben-Deadlines + bevorstehende Bewerbungsgespräche
                List<UpcomingEvent> upcomingEvents = buildUpcomingEvents(openAssignments, myApplications);

                return StudentDashboardResponse.builder()
                                .studentName(studentName)
                                .openAssignmentsCount((long) openAssignments.size())
                                .openAssignments(openAssignmentInfos)
                                .lastSessionId(lastSession != null ? lastSession.getId() : null)
                                .lastSessionStartedAt(lastSession != null ? lastSession.getStartedAt() : null)
                                .totalSessionsCount(totalSessionsCount)
                                .openSessionId(openSession != null ? openSession.getId() : null)
                                .streakDays(streakDays)
                                .applicationsCount(applicationsCount)
                                .badgesCount(badgesCount)
                                .earnedBadgeIcons(earnedBadgeIcons)
                                .notifications(notifications)
                                .upcomingEvents(upcomingEvents)
                                .classFacts(computeClassFacts(studentId, classId))
                                .build();
        }

        /**
         * Anonyme Aggregat-Zahlen über die Klasse (ohne sich selbst) für den „Fakt des Tages".
         */
        private StudentClassFacts computeClassFacts(String selfId, String classId) {
                if (classId == null || classId.isBlank()) {
                        return null;
                }
                SchoolClass schoolClass;
                try {
                        schoolClass = schoolClassService.getMyClass();
                } catch (Exception e) {
                        return null;   // Schüler:in ohne Klasse o.ä. – dann keine Facts
                }
                if (schoolClass == null) {
                        return null;
                }
                ZoneId zone = ZoneId.of("Europe/Zurich");
                LocalDate today = LocalDate.now(zone);

                int classmates = 0;
                int practiced = 0;
                int practicedToday = 0;
                int threePlus = 0;
                int withApplication = 0;

                for (String sid : schoolClass.getStudentIds()) {
                        if (sid.equals(selfId)) {
                                continue;
                        }
                        classmates++;
                        List<Session> sessions = sessionRepository.findByStudentId(sid);
                        if (!sessions.isEmpty()) {
                                practiced++;
                        }
                        if (sessions.size() >= 3) {
                                threePlus++;
                        }
                        boolean practicedTodayByThis = sessions.stream()
                                        .map(Session::getStartedAt)
                                        .filter(Objects::nonNull)
                                        .anyMatch(i -> i.atZone(zone).toLocalDate().isEqual(today));
                        if (practicedTodayByThis) {
                                practicedToday++;
                        }
                        if (!applicationRepository.findByStudentId(sid).isEmpty()) {
                                withApplication++;
                        }
                }
                return new StudentClassFacts(classmates, practiced, practicedToday, threePlus, withApplication);
        }

        /**
         * Baut die Liste anstehender Termine: offene Aufgaben mit zukünftiger Deadline
         * und Bewerbungen mit zukünftigem Gesprächstermin. Sortiert nach Datum, max. 5.
         */
        private List<UpcomingEvent> buildUpcomingEvents(List<Assignment> openAssignments, List<Application> applications) {
                ZoneId zone = ZoneId.of("Europe/Zurich");
                Instant now = Instant.now();
                List<UpcomingEvent> events = new ArrayList<>();

                for (Assignment a : openAssignments) {
                        if (a.getDueDate() != null && a.getDueDate().isAfter(now)) {
                                events.add(new UpcomingEvent("ASSIGNMENT", a.getTitle(), a.getDueDate()));
                        }
                }

                for (Application app : applications) {
                        if (app.getInterviewDate() != null) {
                                Instant when = app.getInterviewDate().atStartOfDay(zone).toInstant();
                                if (when.isAfter(now)) {
                                        events.add(new UpcomingEvent("INTERVIEW", app.getCompanyName(), when));
                                }
                        }
                }

                events.sort(Comparator.comparing(UpcomingEvent::date));
                return events.size() > 5 ? events.subList(0, 5) : events;
        }

        /**
         * Anzahl aufeinanderfolgender Tage (bis heute oder gestern) mit mind. einer Session.
         */
        private int calculateStreak(List<Session> sessions) {
                ZoneId zone = ZoneId.of("Europe/Zurich");
                Set<LocalDate> days = sessions.stream()
                                .map(Session::getStartedAt)
                                .filter(Objects::nonNull)
                                .map(i -> i.atZone(zone).toLocalDate())
                                .collect(Collectors.toSet());

                if (days.isEmpty()) {
                        return 0;
                }

                LocalDate today = LocalDate.now(zone);
                LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
                int streak = 0;
                while (days.contains(cursor)) {
                        streak++;
                        cursor = cursor.minusDays(1);
                }
                return streak;
        }
}