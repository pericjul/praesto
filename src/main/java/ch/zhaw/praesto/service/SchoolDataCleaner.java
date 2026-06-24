package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Löscht eine komplette Schule samt aller mandantenbezogenen Daten (User, Klassen,
 * Aufgaben, Sessions, Abgaben, Invites sowie Notizen/Bewerbungen/Badges der Nutzer).
 * Wird vom nächtlichen Demo-Reset und vom Cleanup abgelaufener Demo-Schulen genutzt.
 */
@Service
@RequiredArgsConstructor
public class SchoolDataCleaner {

    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public void deleteSchoolAndData(String schoolId) {
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
        schoolRepository.deleteById(schoolId);
    }
}
