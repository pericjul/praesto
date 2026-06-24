package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.DemoRequestRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Demo-Terminanfragen: öffentliche Erfassung sowie Freigabe durch den SUPER_ADMIN.
 * Bei Freigabe entsteht eine eigene, zeitlich begrenzte Demo-Schule + Einladungslink,
 * der nur am gebuchten Tag funktioniert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemoRequestService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");
    private static final String KEINE_BERECHTIGUNG = "Keine Berechtigung";

    private final DemoRequestRepository demoRequestRepository;
    private final SchoolRepository schoolRepository;
    private final InviteService inviteService;
    private final UserService userService;

    @Value("${praesto.invite.base-url:https://praesto.ch/join/}")
    private String joinBaseUrl;

    // ============================================================
    // Öffentlich
    // ============================================================

    public DemoRequest submit(DemoRequestSubmitRequest req) {
        if (req == null || req.email() == null || !req.email().contains("@")) {
            throw new BadRequestException("Bitte gib eine gültige E-Mail-Adresse an.");
        }
        if (req.schoolName() == null || req.schoolName().isBlank()) {
            throw new BadRequestException("Bitte gib den Namen der Schule an.");
        }
        DemoRequest request = DemoRequest.builder()
                .schoolName(req.schoolName().trim())
                .contactName(req.contactName() != null ? req.contactName().trim() : null)
                .email(req.email().trim())
                .preferredDate(req.preferredDate())
                .message(req.message())
                .status(DemoRequestStatus.NEW)
                .createdAt(Instant.now())
                .build();
        DemoRequest saved = demoRequestRepository.save(request);
        log.info("Neue Demo-Anfrage von {} ({}), Wunschdatum {}",
                saved.getSchoolName(), saved.getEmail(), saved.getPreferredDate());
        return saved;
    }

    // ============================================================
    // SUPER_ADMIN
    // ============================================================

    public List<DemoRequestDTO> list() {
        requireSuper();
        return demoRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Gibt eine Anfrage für einen Tag frei: erzeugt eine zeitlich begrenzte Demo-Schule
     * und einen nur an diesem Tag gültigen Schulleiter-Einladungslink.
     */
    @Transactional
    public DemoRequestDTO approve(String id, LocalDate date) {
        requireSuper();
        DemoRequest request = demoRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Anfrage nicht gefunden"));

        LocalDate day = date != null ? date : request.getPreferredDate();
        if (day == null) {
            throw new BadRequestException("Kein Datum angegeben");
        }

        Instant from = day.atStartOfDay(ZONE).toInstant();
        Instant until = day.atTime(LocalTime.MAX).atZone(ZONE).toInstant();

        School school = schoolRepository.save(School.builder()
                .name(uniqueName(request.getSchoolName() + " (Demo " + day + ")"))
                .isActive(true)
                .isDemo(true)
                .demoAccessFrom(from)
                .demoAccessUntil(until)
                .createdAt(Instant.now())
                .build());

        InviteCreatedDTO invite = inviteService.createSchoolAdminInvite(school.getId(), until);

        request.setStatus(DemoRequestStatus.APPROVED);
        request.setApprovedSchoolId(school.getId());
        request.setInviteToken(invite.token());
        request.setApprovedDate(day);
        demoRequestRepository.save(request);

        log.info("Demo-Anfrage {} freigegeben für {} – Schule '{}'", id, day, school.getName());
        return DemoRequestDTO.from(request, invite.url());
    }

    public DemoRequestDTO updateStatus(String id, DemoRequestStatus status) {
        requireSuper();
        DemoRequest request = demoRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Anfrage nicht gefunden"));
        request.setStatus(status);
        return toDTO(demoRequestRepository.save(request));
    }

    // ============================================================
    // Intern
    // ============================================================

    private DemoRequestDTO toDTO(DemoRequest r) {
        String url = r.getInviteToken() != null ? joinBaseUrl + r.getInviteToken() : null;
        return DemoRequestDTO.from(r, url);
    }

    private String uniqueName(String base) {
        String name = base;
        int suffix = 2;
        while (schoolRepository.existsByName(name)) {
            name = base + " #" + suffix++;
        }
        return name;
    }

    private void requireSuper() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
    }
}
