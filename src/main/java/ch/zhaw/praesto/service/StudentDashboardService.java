package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.Assignment;
import ch.zhaw.praesto.model.AssignmentStatus;
import ch.zhaw.praesto.model.Session;
import ch.zhaw.praesto.model.SessionStatus;
import ch.zhaw.praesto.model.StudentDashboardResponse;
import ch.zhaw.praesto.repository.ApplicationRepository;
import ch.zhaw.praesto.repository.AssignmentRepository;
import ch.zhaw.praesto.repository.NoteRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final UserService userService;
    private final SchoolClassService schoolClassService;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final BadgeService badgeService;

    public StudentDashboardResponse getDashboardForCurrentStudent() {
        String studentId = userService.getUserId();
        String studentName = userService.getUserName();
        
        // ClassId über SchoolClassService holen (sucht per Email)
        String classId = schoolClassService.getMyClassId();

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

        // Notizen des Students zaehlen
        int notesCount = noteRepository.findByStudentId(studentId).size();

        // Bewerbungen des Students zaehlen
        int applicationsCount = applicationRepository.findByStudentId(studentId).size();

        long badgesCount = badgeService.getEarnedBadgeCount(studentId);

        return StudentDashboardResponse.builder()
                .studentName(studentName)
                .openAssignmentsCount(openAssignmentsCount)
                .nextAssignmentTitle(nextAssignment != null ? nextAssignment.getTitle() : null)
                .nextAssignmentDueDate(nextAssignment != null ? nextAssignment.getDueDate() : null)
                .lastSessionId(lastSession != null ? lastSession.getId() : null)
                .lastSessionStartedAt(lastSession != null ? lastSession.getStartedAt() : null)
                .totalSessionsCount(totalSessionsCount)
                .openSessionId(openSession != null ? openSession.getId() : null)
                .notesCount(notesCount)
                .applicationsCount(applicationsCount)
                .badgesCount((int) badgeService.getEarnedBadgeCount(studentId))                
                .build();
    }
}