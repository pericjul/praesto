package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.AiFeature;
import ch.zhaw.praesto.model.AiQuotaStatus;
import ch.zhaw.praesto.service.AiQuotaService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Liefert der Schüler:in ihren aktuellen KI-Kontingent-Stand (für die Anzeige).
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class AiQuotaController {

    private final AiQuotaService aiQuotaService;
    private final UserService userService;

    @GetMapping("/ai-quota")
    public Map<String, AiQuotaStatus> myQuota() {
        String userId = userService.getUserId();
        return Map.of(
                "PRACTICE_INTERVIEW", aiQuotaService.status(userId, AiFeature.PRACTICE_INTERVIEW),
                "CV", aiQuotaService.status(userId, AiFeature.CV),
                "COVER_LETTER", aiQuotaService.status(userId, AiFeature.COVER_LETTER));
    }
}
