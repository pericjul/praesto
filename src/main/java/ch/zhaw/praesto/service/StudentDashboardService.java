package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionStatus;
import ch.zhaw.praesto.model.StudentDashboardResponse;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final UserService userService;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;

    public StudentDashboardResponse getDashboardForCurrentStudent() {
        String studentId = userService.getUserId();
        String studentName = userService.getUserName();
        String classId = userService.getClassId();

        // Assignments der Klasse holen (oder alle wenn keine Klasse zugeordnet)
        List<Assignment> allAssignments;
        if (classId != null && !classId.isBlank()) {
            allAssignments = assignmentRepository.findByClassId(classId);
        } else {
            // Fallback: alle Assignments (sollte spaeter angepasst werden)
            allAssignments = assignmentRepository.findAll();
        }

        // Nur offene Assignments (ASSIGNED oder IN_PROGRESS)
        List<Assignment> openAssignments = allAssignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED
                        || a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .toList();

        long openAssignmentsCount = openAssignments.size();

        // Naechste faellige Aufgabe finden
        Assignment nextAssignment = openAssignments.stream()
                .filter(a -> a.getDueDate() != null)
                .min(Comparator.comparing(Assignment::getDueDate))
                .orElse(null);

        // Sessions des Students
        List<Session> sessions = sessionRepository.findByStudentId(studentId);
        int totalSessionsCount = sessions.size();

        // Letzte Session (nach Startdatum)
        Session lastSession = sessions.stream()
                .max(Comparator.comparing(Session::getStartedAt))
                .orElse(null);

        // Gibt es eine offene Session die fortgesetzt werden kann?
        Session openSession = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.OPEN)
                .max(Comparator.comparing(Session::getStartedAt))
                .orElse(null);

        return StudentDashboardResponse.builder()
                .studentName(studentName)
                .openAssignmentsCount(openAssignmentsCount)
                .nextAssignmentTitle(nextAssignment != null ? nextAssignment.getTitle() : null)
                .nextAssignmentDueDate(nextAssignment != null ? nextAssignment.getDueDate() : null)
                .lastSessionId(lastSession != null ? lastSession.getId() : null)
                .lastSessionStartedAt(lastSession != null ? lastSession.getStartedAt() : null)
                .totalSessionsCount(totalSessionsCount)
                .openSessionId(openSession != null ? openSession.getId() : null)
                // Platzhalter fuer spaeter (werden in Issue #4 implementiert)
                .badgesCount(0)
                .notesCount(0)
                .applicationsCount(0)
                .build();
    }
}