package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Application;
import ch.zhaw.praesto.model.ApplicationDTO;
import ch.zhaw.praesto.model.ApplicationStats;
import ch.zhaw.praesto.model.ApplicationStatus;
import ch.zhaw.praesto.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final String BEWERBUNG_NICHT_GEFUNDEN = "Bewerbung nicht gefunden";
    private static final String STUDENT = "STUDENT";
    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final BadgeService badgeService;

    /**
     * Neue Bewerbung erstellen.
     */
    public Application createApplication(ApplicationDTO dto) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Bewerbungen erstellen");
        }

        validateDTO(dto);

        String studentId = userService.getUserId();
        Instant now = Instant.now();

        Application application = Application.builder()
                .studentId(studentId)
                .companyName(dto.getCompanyName())
                .position(dto.getPosition())
                .status(parseStatus(dto.getStatus()))
                .appliedAt(parseDate(dto.getAppliedAt()))
                .interviewDate(parseDate(dto.getInterviewDate()))
                .notes(dto.getNotes())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Application saved = applicationRepository.save(application);

        // Badge-Check NACH dem Speichern
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    /**
     * Alle Bewerbungen eines Schuelers (neueste zuerst).
     */
    public List<Application> getMyApplications() {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Bewerbungen sehen");
        }

        String studentId = userService.getUserId();
        return applicationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Eine einzelne Bewerbung holen.
     */
    public Application getApplicationById(String applicationId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Bewerbungen sehen");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(BEWERBUNG_NICHT_GEFUNDEN));

        checkOwnership(application);
        return application;
    }

    /**
     * Bewerbung bearbeiten.
     */
    public Application updateApplication(String applicationId, ApplicationDTO dto) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Bewerbungen bearbeiten");
        }

        validateDTO(dto);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(BEWERBUNG_NICHT_GEFUNDEN));

        checkOwnership(application);

        String studentId = userService.getUserId();

        application.setCompanyName(dto.getCompanyName());
        application.setPosition(dto.getPosition());
        application.setStatus(parseStatus(dto.getStatus()));
        application.setAppliedAt(parseDate(dto.getAppliedAt()));
        application.setInterviewDate(parseDate(dto.getInterviewDate()));
        application.setNotes(dto.getNotes());
        application.setUpdatedAt(Instant.now());

        Application saved = applicationRepository.save(application);

        // Badge-Check NACH dem Speichern (für Status-Badges wie INVITED, ACCEPTED)
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    /**
     * Nur Status aendern.
     */
    public Application updateStatus(String applicationId, String newStatus) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen den Status aendern");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(BEWERBUNG_NICHT_GEFUNDEN));

        checkOwnership(application);

        String studentId = userService.getUserId();

        application.setStatus(parseStatus(newStatus));
        application.setUpdatedAt(Instant.now());

        Application saved = applicationRepository.save(application);

        // Badge-Check NACH dem Speichern (für Status-Badges wie INVITED, ACCEPTED)
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    /**
     * Bewerbung loeschen.
     */
    public void deleteApplication(String applicationId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Bewerbungen loeschen");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException(BEWERBUNG_NICHT_GEFUNDEN));

        checkOwnership(application);

        applicationRepository.delete(application);
    }

    /**
     * Statistiken fuer den aktuellen Schueler.
     */
    public ApplicationStats getMyStats() {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Statistiken sehen");
        }

        String studentId = userService.getUserId();

        long planned = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.PLANNED);
        long applied = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.APPLIED);
        long invited = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.INVITED);
        long interviewDone = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.INTERVIEW_DONE);
        long accepted = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.ACCEPTED);
        long rejected = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.REJECTED);
        long withdrawn = applicationRepository.countByStudentIdAndStatus(studentId, ApplicationStatus.WITHDRAWN);

        long total = planned + applied + invited + interviewDone + accepted + rejected + withdrawn;
        long active = planned + applied + invited + interviewDone;

        return ApplicationStats.builder()
                .total(total)
                .planned(planned)
                .applied(applied)
                .invited(invited)
                .interviewDone(interviewDone)
                .accepted(accepted)
                .rejected(rejected)
                .withdrawn(withdrawn)
                .active(active)
                .build();
    }

    /**
     * Prueft ob der aktuelle User der Besitzer ist.
     */
    private void checkOwnership(Application application) {
        String studentId = userService.getUserId();
        if (!studentId.equals(application.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Bewerbung");
        }
    }

    /**
     * DTO validieren.
     */
    private void validateDTO(ApplicationDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new BadRequestException("Firmenname ist erforderlich");
        }
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new BadRequestException("Status ist erforderlich");
        }
    }

    /**
     * Status-String zu Enum konvertieren.
     */
    private ApplicationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ApplicationStatus.PLANNED;
        }
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ungueltiger Status: " + status);
        }
    }

    /**
     * Datum-String zu LocalDate konvertieren.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new BadRequestException("Ungueltiges Datum: " + dateStr);
        }
    }
}