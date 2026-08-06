package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.AccountType;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stripe-Abo für Privat-Konten (INDIVIDUAL): Checkout starten, Kundenportal öffnen und
 * per Webhook den Abo-Status pflegen. Ist kein Secret-Key konfiguriert, ist Billing inaktiv
 * (die Endpunkte melden das freundlich).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${praesto.stripe.secret-key:}")
    private String secretKey;
    @Value("${praesto.stripe.webhook-secret:}")
    private String webhookSecret;
    @Value("${praesto.stripe.price-monthly:}")
    private String priceMonthly;
    @Value("${praesto.stripe.price-yearly:}")
    private String priceYearly;
    @Value("${praesto.stripe.success-url:https://praesto.ch/abo/erfolg}")
    private String successUrl;
    @Value("${praesto.stripe.cancel-url:https://praesto.ch/abo}")
    private String cancelUrl;
    @Value("${praesto.stripe.portal-return-url:https://praesto.ch/student/dashboard}")
    private String portalReturnUrl;

    @PostConstruct
    void init() {
        if (isEnabled()) {
            Stripe.apiKey = secretKey;
            log.info("Stripe-Billing aktiv.");
        } else {
            log.info("Stripe-Billing inaktiv (kein STRIPE_SECRET_KEY gesetzt).");
        }
    }

    public boolean isEnabled() {
        return secretKey != null && !secretKey.isBlank();
    }

    // ============================================================
    // Checkout & Kundenportal
    // ============================================================

    /** Startet einen Stripe-Checkout fürs gewählte Abo ("monthly"/"yearly") und liefert die URL. */
    public String createCheckoutUrl(String plan) {
        User user = requireIndividual();
        String priceId = "yearly".equalsIgnoreCase(plan) ? priceYearly : priceMonthly;
        if (priceId == null || priceId.isBlank()) {
            throw new BadRequestException("Für dieses Abo ist noch kein Preis hinterlegt.");
        }
        String customerId = ensureCustomer(user);
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setClientReferenceId(user.getId())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build())
                    .build();
            return Session.create(params).getUrl();
        } catch (Exception e) {
            log.error("Stripe-Checkout fehlgeschlagen: {}", e.getMessage());
            throw new BadRequestException("Zahlung konnte nicht gestartet werden. Bitte später erneut versuchen.");
        }
    }

    /** Öffnet das Stripe-Kundenportal (Abo verwalten/kündigen) und liefert die URL. */
    public String createPortalUrl() {
        User user = requireIndividual();
        if (user.getStripeCustomerId() == null) {
            throw new BadRequestException("Noch kein Abo vorhanden.");
        }
        try {
            var params = com.stripe.param.billingportal.SessionCreateParams.builder()
                    .setCustomer(user.getStripeCustomerId())
                    .setReturnUrl(portalReturnUrl)
                    .build();
            return com.stripe.model.billingportal.Session.create(params).getUrl();
        } catch (Exception e) {
            log.error("Stripe-Portal fehlgeschlagen: {}", e.getMessage());
            throw new BadRequestException("Das Kundenportal konnte nicht geöffnet werden.");
        }
    }

    /** Abo-Status des aktuellen Kontos fürs Frontend. */
    public Map<String, Object> status() {
        User user = userService.getCurrentUser();
        Map<String, Object> out = new HashMap<>();
        boolean individual = user.getAccountType() == AccountType.INDIVIDUAL;
        out.put("individual", individual);
        out.put("billingEnabled", isEnabled());
        out.put("status", user.getSubscriptionStatus());
        out.put("trialEndsAt", user.getTrialEndsAt() != null ? user.getTrialEndsAt().toString() : null);
        out.put("subscriptionEndsAt", user.getSubscriptionEndsAt() != null ? user.getSubscriptionEndsAt().toString() : null);
        out.put("hasAccess", user.hasSubscriptionAccess(Instant.now()));
        boolean trialActive = individual && user.getTrialEndsAt() != null && Instant.now().isBefore(user.getTrialEndsAt());
        out.put("trialActive", trialActive);
        out.put("parentConsentPending", user.needsParentConsent());
        out.put("parentEmail", user.getParentEmail());
        return out;
    }

    // ============================================================
    // Webhook
    // ============================================================

    /** Verarbeitet ein Stripe-Webhook-Ereignis (Signatur wird geprüft). */
    public void handleWebhook(String payload, String signature) {
        if (!isEnabled() || webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Stripe-Webhook empfangen, aber Billing/Webhook-Secret nicht konfiguriert.");
            return;
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            throw new BadRequestException("Ungültige Webhook-Signatur");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> onCheckoutCompleted(event);
            case "customer.subscription.created",
                 "customer.subscription.updated",
                 "customer.subscription.deleted" -> onSubscriptionEvent(event);
            default -> { /* andere Ereignisse ignorieren */ }
        }
    }

    private void onCheckoutCompleted(Event event) {
        Optional<com.stripe.model.StripeObject> obj = event.getDataObjectDeserializer().getObject();
        if (obj.isEmpty() || !(obj.get() instanceof Session session)) {
            return;
        }
        User user = null;
        if (session.getClientReferenceId() != null) {
            user = userRepository.findById(session.getClientReferenceId()).orElse(null);
        }
        if (user == null && session.getCustomer() != null) {
            user = userRepository.findByStripeCustomerId(session.getCustomer()).orElse(null);
        }
        if (user == null) {
            log.warn("checkout.session.completed: kein User gefunden ({})", session.getId());
            return;
        }
        if (user.getStripeCustomerId() == null && session.getCustomer() != null) {
            user.setStripeCustomerId(session.getCustomer());
        }
        // Enddatum kommt zuverlässig über das anschliessende subscription-Event; hier
        // schon mal als aktiv markieren.
        user.setSubscriptionStatus("ACTIVE");
        userRepository.save(user);
        log.info("Abo aktiviert (Checkout) für {}", user.getEmail());
    }

    private void onSubscriptionEvent(Event event) {
        Optional<com.stripe.model.StripeObject> obj = event.getDataObjectDeserializer().getObject();
        if (obj.isEmpty() || !(obj.get() instanceof Subscription sub)) {
            return;
        }
        User user = userRepository.findByStripeCustomerId(sub.getCustomer()).orElse(null);
        if (user == null) {
            log.warn("Subscription-Event ohne passenden User (Kunde {})", sub.getCustomer());
            return;
        }
        String status = sub.getStatus();  // active, trialing, past_due, canceled, unpaid, ...
        boolean paidThrough = "active".equals(status) || "trialing".equals(status) || "past_due".equals(status);
        Long periodEnd = currentPeriodEnd(sub);
        if (periodEnd != null) {
            user.setSubscriptionEndsAt(Instant.ofEpochSecond(periodEnd));
        }
        if ("canceled".equals(status) || "unpaid".equals(status) || "incomplete_expired".equals(status)) {
            user.setSubscriptionStatus("CANCELED");
        } else if (paidThrough) {
            user.setSubscriptionStatus("ACTIVE");
        }
        userRepository.save(user);
        log.info("Abo-Status aktualisiert für {}: {} (bis {})", user.getEmail(), status, user.getSubscriptionEndsAt());
    }

    // ============================================================
    // Intern
    // ============================================================

    // Seit Stripe-API 2025 liegt current_period_end auf den Subscription-Items, nicht mehr
    // direkt auf der Subscription. Wir nehmen das Ende des ersten Items.
    private Long currentPeriodEnd(Subscription sub) {
        try {
            var items = sub.getItems();
            if (items != null && items.getData() != null && !items.getData().isEmpty()) {
                return items.getData().get(0).getCurrentPeriodEnd();
            }
        } catch (Exception ignored) {
            // Fällt auf null zurück
        }
        return null;
    }

    private User requireIndividual() {
        if (!isEnabled()) {
            throw new BadRequestException("Bezahlung ist derzeit nicht verfügbar.");
        }
        User user = userService.getCurrentUser();
        if (user.getAccountType() != AccountType.INDIVIDUAL) {
            throw new ForbiddenException("Abos gibt es nur für Privat-Konten.");
        }
        return user;
    }

    private String ensureCustomer(User user) {
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
            return user.getStripeCustomerId();
        }
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getFullName())
                    .putMetadata("praestoUserId", user.getId())
                    .build();
            String customerId = com.stripe.model.Customer.create(params).getId();
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
            return customerId;
        } catch (Exception e) {
            log.error("Stripe-Kunde konnte nicht erstellt werden: {}", e.getMessage());
            throw new BadRequestException("Zahlung konnte nicht gestartet werden.");
        }
    }
}
