package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.DocumentRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Objekt-Level-Autorisierung für Datei-Downloads (gegen IDOR/BOLA): Eine Datei darf
 * nur laden, wem sie gehört (eigenes Dokument/eigene Abgabe), eine Lehrperson/Schulleitung
 * derselben Schule, oder ein SUPER_ADMIN. Dateien ohne zugehörigen Datensatz sind gesperrt.
 */
@Service
@RequiredArgsConstructor
public class FileAccessService {

    private static final String KEIN_ZUGRIFF = "Kein Zugriff auf diese Datei";

    private final DocumentRepository documentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    public void assertCanAccess(String storedFileName) {
        User current = userService.getCurrentUser();   // wirft, wenn nicht authentifiziert
        if (current.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        // Besitzer + Schule aus Dossier-Dokument ODER Abgabe ermitteln
        String ownerId = null;
        String schoolId = null;

        var document = documentRepository.findFirstByFileUrl(storedFileName);
        if (document.isPresent()) {
            ownerId = document.get().getStudentId();
            schoolId = document.get().getSchoolId();
        } else {
            var submission = submissionRepository.findFirstByFileUrl(storedFileName);
            if (submission.isPresent()) {
                ownerId = submission.get().getStudentId();
                schoolId = submission.get().getSchoolId();
            }
        }

        // Keine Zuordnung => kein Download (keine verwaisten Dateien ausliefern)
        if (ownerId == null) {
            throw new ForbiddenException(KEIN_ZUGRIFF);
        }

        boolean isOwner = current.getId() != null && current.getId().equals(ownerId);
        boolean isStaffSameSchool =
                (current.getRole() == UserRole.TEACHER || current.getRole() == UserRole.SCHOOL_ADMIN)
                        && schoolId != null && schoolId.equals(current.getSchoolId());

        if (!isOwner && !isStaffSameSchool) {
            throw new ForbiddenException(KEIN_ZUGRIFF);
        }
    }
}
