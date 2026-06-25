package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.UserDTO;
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

    @GetMapping("/search")
    public List<UserDTO> search(@RequestParam(required = false) String q) {
        return superUserService.search(q);
    }

    @GetMapping("/{id}/export")
    public Map<String, Object> export(@PathVariable String id) {
        return superUserService.exportUserData(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        superUserService.deleteUserData(id);
        return ResponseEntity.noContent().build();
    }
}
