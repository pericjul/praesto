package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.InviteCreatedDTO;
import ch.zhaw.praesto.model.InviteToken;
import ch.zhaw.praesto.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Erstellung und Verwaltung von Invite-Links.
 * SCHOOL_ADMIN verwaltet Lehrer-Invites, TEACHER die Klassen-Invites seiner Klassen.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    // ===== SCHOOL_ADMIN: Lehrer-Invites =====

    @PostMapping("/admin/invites/teacher")
    public InviteCreatedDTO createTeacherInvite(@RequestBody(required = false) Map<String, Integer> body) {
        return inviteService.createTeacherInvite(expiresInDays(body));
    }

    @GetMapping("/admin/invites")
    public List<InviteToken> listSchoolInvites() {
        return inviteService.listSchoolInvites();
    }

    @DeleteMapping("/admin/invites/{id}")
    public ResponseEntity<Void> deactivateInvite(@PathVariable String id) {
        inviteService.deactivateInvite(id);
        return ResponseEntity.noContent().build();
    }

    // ===== TEACHER: Klassen-Invites =====

    @PostMapping("/teacher/invites/class/{classId}")
    public InviteCreatedDTO createClassInvite(
            @PathVariable String classId,
            @RequestBody(required = false) Map<String, Integer> body) {
        return inviteService.createClassInvite(classId, expiresInDays(body));
    }

    @GetMapping("/teacher/invites/class/{classId}")
    public List<InviteToken> listClassInvites(@PathVariable String classId) {
        return inviteService.listClassInvites(classId);
    }

    private Integer expiresInDays(Map<String, Integer> body) {
        return body != null ? body.get("expiresInDays") : null;
    }
}
