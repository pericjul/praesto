package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ApproveDemoRequest;
import ch.zhaw.praesto.model.DemoRequestDTO;
import ch.zhaw.praesto.model.DemoRequestStatus;
import ch.zhaw.praesto.model.DemoRequestSubmitRequest;
import ch.zhaw.praesto.service.DemoRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Öffentliches Demo-Termin-Anfrageformular (POST /api/demo-requests) und
 * SuperAdmin-Verwaltung (Liste, Freigabe für einen Tag, Status).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoRequestController {

    private final DemoRequestService demoRequestService;

    @PostMapping("/demo-requests")
    public Map<String, Boolean> submit(@RequestBody DemoRequestSubmitRequest request) {
        demoRequestService.submit(request);
        return Map.of("success", true);
    }

    @GetMapping("/super/demo-requests")
    public List<DemoRequestDTO> list() {
        return demoRequestService.list();
    }

    @PostMapping("/super/demo-requests/{id}/approve")
    public DemoRequestDTO approve(@PathVariable String id,
                                  @RequestBody(required = false) ApproveDemoRequest body) {
        return demoRequestService.approve(id, body != null ? body.date() : null);
    }

    @PutMapping("/super/demo-requests/{id}/status")
    public DemoRequestDTO updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        return demoRequestService.updateStatus(id, DemoRequestStatus.valueOf(body.get("status")));
    }
}
