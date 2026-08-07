package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.UserDTO;
import ch.zhaw.praesto.service.BillingService;
import ch.zhaw.praesto.service.SuperUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Datenschutz-Endpoints für den SUPER_ADMIN: Benutzersuche, Datenauskunft (Export)
 * und vollständige Löschung aller Daten einer Person.
 */
@RestController
@RequestMapping("/api/super/users")
@RequiredArgsConstructor
public class SuperUserController {

    private final SuperUserService superUserService;
    private final BillingService billingService;

    // Privat-Konto ohne Eltern-Bestätigung freischalten (startet ggf. die Testphase).
    @PostMapping("/{id}/grant-access")
    public Map<String, Object> grantAccess(@PathVariable String id) {
        return superUserService.grantIndividualAccess(id);
    }

    // Abo eines Privat-Kontos direkt mit Stripe abgleichen (falls ein Webhook verpasst wurde).
    @PostMapping("/{id}/resync-subscription")
    public Map<String, Object> resyncSubscription(@PathVariable String id) {
        return billingService.resyncForUser(id);
    }

    // Alle Privat-Abos in einem Rutsch mit Stripe abgleichen (ein Klick).
    @PostMapping("/resync-subscriptions")
    public Map<String, Object> resyncAllSubscriptions() {
        return billingService.resyncAllIndividuals();
    }

    @GetMapping("/search")
    public List<UserDTO> search(@RequestParam(required = false) String q) {
        return superUserService.search(q);
    }

    // Kennzahlen zu Privat-/B2C-Konten (Registrierung -> Bezahlung).
    @GetMapping("/b2c-stats")
    public Map<String, Object> b2cStats() {
        return superUserService.individualStats();
    }

    // Weiteren Super-Admin anlegen (nur Super-Admin).
    @PostMapping("/admins")
    public UserDTO createAdmin(@RequestBody Map<String, String> body) {
        return UserDTO.from(superUserService.createAdmin(
                body.get("email"), body.get("firstName"), body.get("lastName"), body.get("password")));
    }

    @GetMapping("/{id}/export")
    public Map<String, Object> export(@PathVariable String id) {
        return superUserService.exportUserData(id);
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        superUserService.setActive(id, Boolean.TRUE.equals(body.get("active")));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        superUserService.deleteUserData(id);
        return ResponseEntity.noContent().build();
    }
}
