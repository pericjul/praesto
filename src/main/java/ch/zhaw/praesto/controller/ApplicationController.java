package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.Application;
import ch.zhaw.praesto.model.ApplicationDTO;
import ch.zhaw.praesto.model.ApplicationStats;
import ch.zhaw.praesto.service.ApplicationService;
import ch.zhaw.praesto.service.ApplicationShareService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationShareService applicationShareService;
    private final UserService userService;

    /** GET /api/applications/sharing – teile ich meine Bewerbungen mit der Lehrperson? */
    @GetMapping("/sharing")
    public ResponseEntity<Map<String, Boolean>> getSharing() {
        boolean shared = applicationShareService.isShared(userService.getUserId());
        return ResponseEntity.ok(Map.of("shared", shared));
    }

    /** PUT /api/applications/sharing – Freigabe ein-/ausschalten. */
    @PutMapping("/sharing")
    public ResponseEntity<Map<String, Boolean>> setSharing(@RequestBody Map<String, Boolean> body) {
        boolean shared = Boolean.TRUE.equals(body.get("shared"));
        applicationShareService.setShared(userService.getUserId(), userService.getCurrentSchoolId(), shared);
        return ResponseEntity.ok(Map.of("shared", shared));
    }

    /**
     * POST /api/applications - Neue Bewerbung erstellen
     */
    @PostMapping("")
    public ResponseEntity<Application> createApplication(@RequestBody ApplicationDTO dto) {
        Application application = applicationService.createApplication(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    /**
     * GET /api/applications - Alle eigenen Bewerbungen
     */
    @GetMapping("")
    public ResponseEntity<List<Application>> getMyApplications() {
        List<Application> applications = applicationService.getMyApplications();
        return ResponseEntity.ok(applications);
    }

    /**
     * GET /api/applications/stats - Statistiken
     */
    @GetMapping("/stats")
    public ResponseEntity<ApplicationStats> getMyStats() {
        ApplicationStats stats = applicationService.getMyStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/applications/{id} - Eine Bewerbung
     */
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable String id) {
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }

    /**
     * PUT /api/applications/{id} - Bewerbung bearbeiten
     */
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable String id,
            @RequestBody ApplicationDTO dto) {
        Application application = applicationService.updateApplication(id, dto);
        return ResponseEntity.ok(application);
    }

    /**
     * PUT /api/applications/{id}/status - Nur Status aendern
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Application> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        Application application = applicationService.updateStatus(id, newStatus);
        return ResponseEntity.ok(application);
    }

    /**
     * DELETE /api/applications/{id} - Bewerbung loeschen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}