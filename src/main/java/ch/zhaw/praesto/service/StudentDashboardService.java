package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.StudentDashboardResponse;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

        // TODO: Hier spaeter wirklich nur Assignments der Klassen dieses Students holen.
        // Fuer den Moment nehmen wir alle Assignments mit Status ASSIGNED oder IN_PROGRESS.
        List<Assignment> openAssignments = assignmentRepository
                .findAll()
                .stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED
                        || a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .toList();

        long openAssignmentsCount = openAssignments.size();

        Assignment nextAssignment = openAssignments
                .stream()
                .filter(a -> a.getDueDate() != null)
                .min(Comparator.comparing(Assignment::getDueDate))
                .orElse(null);

        // Sessions des Students
        List<Session> sessions = sessionRepository.findByStudentId(studentId);
        int totalSessionsCount = sessions.size();

        Session lastSession = sessions.stream()
                .max(Comparator.comparing(Session::getStartedAt))
                .orElse(null);

        return StudentDashboardResponse.builder()
                // Name optional, falls du den nicht hast, kannst du das spaeter anpassen.
                .openAssignmentsCount(openAssignmentsCount)
                .nextAssignmentTitle(nextAssignment != null ? nextAssignment.getTitle() : null)
                .nextAssignmentDueDate(nextAssignment != null ? nextAssignment.getDueDate() : null)
                .lastSessionId(lastSession != null ? lastSession.getId() : null)
                .lastSessionStartedAt(lastSession != null ? lastSession.getStartedAt() : null)
                .totalSessionsCount(totalSessionsCount)
                // Platzhalter fuer spaeter
                .badgesCount(0)
                .notesCount(0)
                .build();
    }
}