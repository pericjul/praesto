package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Abo/Bezahlung für Privat-Konten (Stripe). Der Webhook ist öffentlich (Signatur-geprüft),
 * die übrigen Endpunkte erfordern ein eingeloggtes Privat-Konto.
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(billingService.status());
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(@RequestBody Map<String, String> body) {
        String url = billingService.createCheckoutUrl(body.get("plan"));
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> portal() {
        return ResponseEntity.ok(Map.of("url", billingService.createPortalUrl()));
    }

    // Öffentlich (permitAll); die Echtheit wird über die Stripe-Signatur geprüft.
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader("Stripe-Signature") String signature) {
        billingService.handleWebhook(payload, signature);
        return ResponseEntity.ok("ok");
    }
}
