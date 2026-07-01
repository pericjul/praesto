package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.BugReport;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.service.BugReportService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Bug-Meldungen: jede eingeloggte Person kann melden; nur der Super-Admin sieht/verwaltet sie.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BugController {

    private final BugReportService bugReportService;
    private final UserService userService;

    /** POST /api/bugs – Bug melden (jede eingeloggte Person; Demo-Token ist read-only und wird geblockt). */
    @PostMapping("/bugs")
    public ResponseEntity<Void> report(@RequestBody Map<String, String> body) {
        User reporter = userService.getCurrentUser();
        bugReportService.create(
                reporter,
                body.get("title"),
                body.get("description"),
                body.get("area"),
                body.get("severity"),
                body.get("steps"),
                body.get("device"),
                body.get("screenshot"));
        return ResponseEntity.ok().build();
    }

    /** GET /api/super/bugs – alle Bugs (nur Super-Admin). */
    @GetMapping("/super/bugs")
    public List<BugReport> all() {
        requireSuperAdmin();
        return bugReportService.listAll();
    }

    /** PATCH /api/super/bugs/{id}/status – Status setzen (nur Super-Admin). */
    @PatchMapping("/super/bugs/{id}/status")
    public ResponseEntity<Void> setStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        requireSuperAdmin();
        bugReportService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/super/bugs/{id} – Bug löschen (nur Super-Admin). */
    @DeleteMapping("/super/bugs/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        requireSuperAdmin();
        bugReportService.delete(id);
        return ResponseEntity.ok().build();
    }

    private void requireSuperAdmin() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Nur der Super-Admin kann Bug-Meldungen einsehen.");
        }
    }
}
