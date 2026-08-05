package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Application;
import ch.zhaw.praesto.model.ClassApplicationsDTO;
import ch.zhaw.praesto.model.SchoolClass;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.ApplicationRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Bewerbungs-Übersicht einer Klasse für die Lehrperson – aber NUR die Bewerbungen der
 * Schüler:innen, die ihre Bewerbungen selbst freigegeben haben ({@link ApplicationShareService}).
 * Ohne Freigabe wird die Person zwar gelistet, aber ohne Bewerbungen (shared = false).
 */
@Service
@RequiredArgsConstructor
public class TeacherApplicationsService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationShareService applicationShareService;
    private final UserService userService;

    public ClassApplicationsDTO getClassApplications(String classId) {
        if (!userService.userHasRole(UserRole.TEACHER)) {
            throw new ForbiddenException("Nur Lehrpersonen");
        }
        SchoolClass schoolClass = schoolClassRepository
                .findByIdAndSchoolId(classId, userService.getCurrentSchoolId())
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));
        if (!schoolClass.canManage(userService.getCurrentUserId())) {
            throw new ForbiddenException("Keine Berechtigung für diese Klasse");
        }

        List<ClassApplicationsDTO.StudentApplications> students = new ArrayList<>();
        for (String studentId : schoolClass.getStudentIds()) {
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }
            boolean shared = applicationShareService.isShared(studentId);
            List<Application> applications = shared
                    ? applicationRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                    : List.of();
            students.add(new ClassApplicationsDTO.StudentApplications(
                    studentId, student.getFullName(), shared, applications));
        }
        // Wer geteilt hat, zuerst; dann alphabetisch
        students.sort((a, b) -> {
            if (a.shared() != b.shared()) {
                return a.shared() ? -1 : 1;
            }
            return safe(a.name()).compareToIgnoreCase(safe(b.name()));
        });

        return new ClassApplicationsDTO(classId, schoolClass.getName(), students);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
