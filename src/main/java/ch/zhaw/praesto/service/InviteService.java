package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.InviteTokenRepository;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Erstellung, Validierung und Einlösung von Invite-Links.
 * Lehrer-Links: 30 Tage gültig, Klassen-Links: 14 Tage, beide unbegrenzt nutzbar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private static final int DEFAULT_TEACHER_DAYS = 30;
    private static final int DEFAULT_CLASS_DAYS = 14;
    private static final int DEFAULT_ADMIN_DAYS = 30;
    private static final String KEINE_BERECHTIGUNG = "Keine Berechtigung";

    private final InviteTokenRepository inviteTokenRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final UserService userService;

    @Value("${praesto.invite.base-url:https://praesto.ch/join/}")
    private String joinBaseUrl;

    // ============================================================
    // Öffentlich: Registrierung
    // ============================================================

    public InviteToken requireUsableInvite(String token) {
        InviteToken invite = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Einladungslink ungültig"));
        if (!invite.isUsable(Instant.now())) {
            throw new BadRequestException("Einladungslink ist abgelaufen oder nicht mehr gültig");
        }
        return invite;
    }

    public InviteDetailsDTO getInviteDetails(String token) {
        InviteToken invite = requireUsableInvite(token);
        String schoolName = schoolRepository.findById(invite.getSchoolId())
                .map(School::getName)
                .orElse(null);
        String className = null;
        if (invite.getType() == InviteType.CLASS && invite.getClassId() != null) {
            className = schoolClassRepository.findById(invite.getClassId())
                    .map(SchoolClass::getName)
                    .orElse(null);
        }
        return new InviteDetailsDTO(invite.getType().name(), schoolName, className, invite.getExpiresAt());
    }

    public void markUsed(InviteToken invite) {
        invite.setUsedCount(invite.getUsedCount() + 1);
        inviteTokenRepository.save(invite);
    }

    // ============================================================
    // SUPER_ADMIN: Schulleiter-Invite
    // ============================================================

    public InviteCreatedDTO createSchoolAdminInvite(String schoolId, Integer expiresInDays) {
        requireRole(UserRole.SUPER_ADMIN);
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("Schule nicht gefunden"));
        InviteToken token = buildInvite(schoolId, InviteType.SCHOOL_ADMIN, null,
                resolveDays(expiresInDays, DEFAULT_ADMIN_DAYS));
        inviteTokenRepository.save(token);
        return toCreatedDTO(token, null);
    }

    /**
     * Schulleiter-Invite mit exaktem Ablaufzeitpunkt (für zeitlich begrenzte Demo-Schulen).
     */
    public InviteCreatedDTO createSchoolAdminInvite(String schoolId, Instant expiresAt) {
        requireRole(UserRole.SUPER_ADMIN);
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("Schule nicht gefunden"));
        Instant now = Instant.now();
        InviteToken token = InviteToken.builder()
                .token(UUID.randomUUID().toString())
                .schoolId(schoolId)
                .type(InviteType.SCHOOL_ADMIN)
                .createdByUserId(userService.getCurrentUserId())
                .expiresAt(expiresAt)
                .maxUses(null)
                .usedCount(0)
                .isActive(true)
                .createdAt(now)
                .build();
        inviteTokenRepository.save(token);
        return toCreatedDTO(token, null);
    }

    // ============================================================
    // SCHOOL_ADMIN: Lehrer-Invites
    // ============================================================

    public InviteCreatedDTO createTeacherInvite(Integer expiresInDays) {
        requireRole(UserRole.SCHOOL_ADMIN);
        String schoolId = userService.getCurrentSchoolId();
        InviteToken token = buildInvite(schoolId, InviteType.TEACHER, null,
                resolveDays(expiresInDays, DEFAULT_TEACHER_DAYS));
        inviteTokenRepository.save(token);
        return toCreatedDTO(token, null);
    }

    public List<InviteToken> listSchoolInvites() {
        requireRole(UserRole.SCHOOL_ADMIN);
        return inviteTokenRepository.findBySchoolIdAndIsActiveTrue(userService.getCurrentSchoolId());
    }

    public void deactivateInvite(String id) {
        requireRole(UserRole.SCHOOL_ADMIN);
        InviteToken token = inviteTokenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Einladung nicht gefunden"));
        if (!token.getSchoolId().equals(userService.getCurrentSchoolId())) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
        token.setActive(false);
        inviteTokenRepository.save(token);
    }

    // ============================================================
    // TEACHER: Klassen-Invites
    // ============================================================

    public InviteCreatedDTO createClassInvite(String classId, Integer expiresInDays) {
        requireRole(UserRole.TEACHER);
        SchoolClass schoolClass = requireOwnClass(classId);
        InviteToken token = buildInvite(schoolClass.getSchoolId(), InviteType.CLASS, classId,
                resolveDays(expiresInDays, DEFAULT_CLASS_DAYS));
        inviteTokenRepository.save(token);
        return toCreatedDTO(token, schoolClass.getName());
    }

    public List<InviteToken> listClassInvites(String classId) {
        requireRole(UserRole.TEACHER);
        requireOwnClass(classId);
        return inviteTokenRepository.findByClassIdAndIsActiveTrue(classId);
    }

    // ============================================================
    // Intern
    // ============================================================

    private SchoolClass requireOwnClass(String classId) {
        String schoolId = userService.getCurrentSchoolId();
        SchoolClass schoolClass = schoolClassRepository.findByIdAndSchoolId(classId, schoolId)
                .orElseThrow(() -> new NotFoundException("Klasse nicht gefunden"));
        if (!schoolClass.getTeacherId().equals(userService.getCurrentUserId())) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
        return schoolClass;
    }

    private InviteToken buildInvite(String schoolId, InviteType type, String classId, int days) {
        Instant now = Instant.now();
        return InviteToken.builder()
                .token(UUID.randomUUID().toString())
                .schoolId(schoolId)
                .type(type)
                .classId(classId)
                .createdByUserId(userService.getCurrentUserId())
                .expiresAt(now.plus(days, ChronoUnit.DAYS))
                .maxUses(null)          // unbegrenzt nutzbar
                .usedCount(0)
                .isActive(true)
                .createdAt(now)
                .build();
    }

    private InviteCreatedDTO toCreatedDTO(InviteToken token, String className) {
        return new InviteCreatedDTO(
                token.getId(),
                token.getToken(),
                joinBaseUrl + token.getToken(),
                token.getExpiresAt(),
                className);
    }

    private int resolveDays(Integer requested, int fallback) {
        return requested != null && requested > 0 ? requested : fallback;
    }

    private void requireRole(UserRole role) {
        if (!userService.userHasRole(role)) {
            throw new ForbiddenException(KEINE_BERECHTIGUNG);
        }
    }
}
