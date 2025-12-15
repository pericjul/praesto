package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

                // Bewerbungen
                int applicationsCount = applicationRepository.findByStudentId(studentId).size();

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

                return StudentDashboardResponse.builder()
                                .studentName(studentName)
                                .openAssignmentsCount((long) openAssignments.size())
                                .openAssignments(openAssignmentInfos)
                                .lastSessionId(lastSession != null ? lastSession.getId() : null)
                                .lastSessionStartedAt(lastSession != null ? lastSession.getStartedAt() : null)
                                .totalSessionsCount(totalSessionsCount)
                                .openSessionId(openSession != null ? openSession.getId() : null)
                                .applicationsCount(applicationsCount)
                                .badgesCount(badgesCount)
                                .earnedBadgeIcons(earnedBadgeIcons)
                                .notifications(notifications)
                                .build();
        }
}