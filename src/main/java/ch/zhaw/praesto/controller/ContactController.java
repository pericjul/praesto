package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ContactInquiry;
import ch.zhaw.praesto.model.ContactRequest;
import ch.zhaw.praesto.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Öffentliches Kontaktformular (POST /api/contact) und SuperAdmin-Übersicht der Anfragen.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/contact")
    public Map<String, Boolean> submit(@RequestBody ContactRequest request) {
        contactService.submit(request);
        return Map.of("success", true);
    }

    @GetMapping("/super/inquiries")
    public List<ContactInquiry> list() {
        return contactService.list();
    }

    @PutMapping("/super/inquiries/{id}/handled")
    public ContactInquiry markHandled(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        return contactService.markHandled(id, Boolean.TRUE.equals(body.get("handled")));
    }
}
