package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import ch.zhaw.praesto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login / Registrierung / Demo-Login. Erstellt keine JWTs selbst – das übernimmt
 * der {@code AuthController} via {@code JwtService}; dieser Service liefert nur den
 * authentifizierten {@link User}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String LOGIN_FEHLER = "Email oder Passwort falsch";

    // ----- einfaches In-Memory Rate-Limiting gegen Brute-Force -----
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_WINDOW = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Attempt> loginAttempts = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final InviteService inviteService;
    private final LocaleMessages messages;
    private final ParentConsentService parentConsentService;

    private record Attempt(int count, Instant windowStart) {
    }

    /**
     * Login per Email + Passwort. Aktualisiert lastLoginAt.
     */
    public User login(String email, String rawPassword) {
        String normalized = normalizeEmail(email);
        if (normalized == null || rawPassword == null) {
            throw new BadRequestException(LOGIN_FEHLER);
        }

        checkNotLocked(normalized);

        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null
                || user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            registerFailedAttempt(normalized);
            throw new BadRequestException(LOGIN_FEHLER);
        }

        if (!user.isActive()) {
            throw new ForbiddenException("Dieser Account ist deaktiviert");
        }

        if (!user.isWithinDemoWindow(Instant.now())) {
            throw new ForbiddenException("Dieser Demo-Zugang ist nur am gebuchten Tag aktiv.");
        }

        // Schul-Sperre (z.B. bei ausbleibender Zahlung): Login für alle Nutzer der Schule
        // blockieren, Daten bleiben erhalten. Super-Admin ist davon ausgenommen.
        if (user.getRole() != UserRole.SUPER_ADMIN && user.getSchoolId() != null) {
            boolean schoolActive = schoolRepository.findById(user.getSchoolId())
                    .map(School::isActive)
                    .orElse(true);
            if (!schoolActive) {
                throw new ForbiddenException(
                        "Diese Schule ist aktuell gesperrt. Bitte wende dich an deine Schulleitung.");
            }
        }

        loginAttempts.remove(normalized);
        user.setLastLoginAt(Instant.now());
        return userRepository.save(user);
    }

    private void checkNotLocked(String email) {
        Attempt attempt = loginAttempts.get(email);
        if (attempt == null) {
            return;
        }
        boolean windowActive = Duration.between(attempt.windowStart(), Instant.now()).compareTo(LOCK_WINDOW) < 0;
        if (windowActive && attempt.count() >= MAX_ATTEMPTS) {
            throw new BadRequestException(
                    "Zu viele Fehlversuche. Bitte warte ein paar Minuten und versuche es erneut.");
        }
    }

    private void registerFailedAttempt(String email) {
        Instant now = Instant.now();
        loginAttempts.compute(email, (key, current) -> {
            if (current == null || Duration.between(current.windowStart(), now).compareTo(LOCK_WINDOW) >= 0) {
                return new Attempt(1, now);
            }
            return new Attempt(current.count() + 1, current.windowStart());
        });
    }

    /**
     * Registrierung über einen Invite-Token. Rolle und schoolId stammen aus dem Invite;
     * bei einem Klassen-Invite wird der neue Schüler direkt in die Klasse eingeschrieben.
     */
    public User register(String token, RegisterRequest req) {
        InviteToken invite = inviteService.requireUsableInvite(token);
        validateRegister(req);

        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(messages.get("err.emailExists"));
        }

        UserRole role = roleForInvite(invite.getType());
        Instant now = Instant.now();

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .role(role)
                .schoolId(invite.getSchoolId())
                .isActive(true)
                .createdAt(now)
                .build();

        // Bei einer zeitlich begrenzten Demo-Schule das Zugangsfenster vererben,
        // damit auch nachträglich angelegte Lehrer/Schüler nur am Demo-Tag rein können.
        schoolRepository.findById(invite.getSchoolId())
                .filter(School::isDemo)
                .ifPresent(school -> {
                    user.setDemoAccessFrom(school.getDemoAccessFrom());
                    user.setDemoAccessUntil(school.getDemoAccessUntil());
                });

        User saved = userRepository.save(user);

        if (invite.getType() == InviteType.CLASS && invite.getClassId() != null) {
            schoolClassRepository.findById(invite.getClassId()).ifPresent(schoolClass -> {
                schoolClass.addStudent(saved.getId());
                schoolClass.setUpdatedAt(now);
                schoolClassRepository.save(schoolClass);
            });
        }

        inviteService.markUsed(invite);
        log.info("Neuer User registriert: {} ({})", saved.getEmail(), role);
        return saved;
    }

    /** Dauer der Gratis-Testphase für neue Privat-Konten. */
    private static final int TRIAL_DAYS = 7;

    /**
     * Selbst-Registrierung eines Privat-/B2C-Kontos (ohne Schule/Lehrperson). Rolle bleibt
     * STUDENT (damit alle Schüler-Funktionen greifen), aber accountType=INDIVIDUAL und eine
     * eigene, zufällige schoolId (eigener Mandant, ohne echten Schul-Eintrag). Startet mit
     * einer {@value #TRIAL_DAYS}-tägigen Gratis-Testphase.
     */
    public User registerIndividual(RegisterRequest req) {
        validateRegister(req);

        String email = normalizeEmail(req.email());
        if (isDisposableEmail(email)) {
            throw new BadRequestException(messages.get("err.disposableEmail"));
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(messages.get("err.emailExists"));
        }
        String parentEmail = normalizeEmail(req.parentEmail());
        if (parentEmail == null || !parentEmail.contains("@")) {
            throw new BadRequestException(messages.get("err.parentEmailRequired"));
        }
        if (parentEmail.equals(email)) {
            throw new BadRequestException(messages.get("err.parentEmailSame"));
        }

        Instant now = Instant.now();
        // Zugang erst nach Eltern-Bestätigung: noch keine Testphase, Status PENDING_CONSENT.
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .role(UserRole.STUDENT)
                .accountType(AccountType.INDIVIDUAL)
                .schoolId(java.util.UUID.randomUUID().toString())  // eigener Mandant, kein Schul-Eintrag
                .isActive(true)
                .subscriptionStatus("PENDING_CONSENT")
                .parentEmail(parentEmail)
                .parentConsentConfirmed(false)
                .createdAt(now)
                .build();

        User saved = userRepository.save(user);
        parentConsentService.createAndSend(saved, parentEmail);
        log.info("Neues Privat-Konto registriert: {} (Eltern-Bestätigung ausstehend)", saved.getEmail());
        return saved;
    }

    /**
     * Demo-Login ohne Passwort. Je nach gewünschter Rolle wird ein echtes (beschreibbares)
     * Konto der Demo-Schule geliefert, damit Besucher:innen wirklich alles ausprobieren können:
     *   student → übt KI-Gespräche, löst Aufgaben
     *   teacher → erstellt Aufgaben, bewertet
     *   admin   → verwaltet die Schule
     * Ohne/unbekannte Rolle: read-only DEMO_USER als Fallback.
     */
    public User demoLogin(String as) {
        String email = switch (as == null ? "" : as.toLowerCase()) {
            case "student" -> "schueler1@demo.praesto.ch";
            case "teacher" -> "lehrerin.mueller@demo.praesto.ch";
            case "admin" -> "demo-admin@praesto.ch";
            default -> null;
        };

        if (email != null) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("Demo-Account nicht gefunden – wurde die Demo-Schule angelegt?"));
        }

        return userRepository.findByRole(UserRole.DEMO_USER)
                .orElseThrow(() -> new NotFoundException("Kein Demo-Account vorhanden"));
    }

    private UserRole roleForInvite(InviteType type) {
        return switch (type) {
            case SCHOOL_ADMIN -> UserRole.SCHOOL_ADMIN;
            case TEACHER -> UserRole.TEACHER;
            case CLASS -> UserRole.STUDENT;
        };
    }

    private void validateRegister(RegisterRequest req) {
        if (req.firstName() == null || req.firstName().isBlank()) {
            throw new BadRequestException(messages.get("err.firstNameRequired"));
        }
        if (req.lastName() == null || req.lastName().isBlank()) {
            throw new BadRequestException(messages.get("err.lastNameRequired"));
        }
        if (req.email() == null || !req.email().contains("@")) {
            throw new BadRequestException(messages.get("err.invalidEmail"));
        }
        if (req.password() == null || req.password().length() < 8) {
            throw new BadRequestException(messages.get("err.passwordMin8"));
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase().trim();
    }

    // Bekannte Wegwerf-/Temporär-Mail-Domains blockieren (erste Hürde gegen Mehrfach-Trials).
    // Keine vollständige Liste – die eigentliche Absicherung ist die E-Mail-Verifizierung.
    private static final java.util.Set<String> DISPOSABLE_DOMAINS = java.util.Set.of(
            "mailinator.com", "guerrillamail.com", "guerrillamail.info", "sharklasers.com",
            "10minutemail.com", "tempmail.com", "temp-mail.org", "trashmail.com", "yopmail.com",
            "getnada.com", "dispostable.com", "maildrop.cc", "fakeinbox.com", "throwawaymail.com",
            "mohmal.com", "mailnesia.com", "spamgourmet.com", "tempmailo.com", "moakt.com",
            "emailondeck.com", "mintemail.com", "1secmail.com", "wegwerfmail.de", "trashmail.de");

    private boolean isDisposableEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).trim();
        return DISPOSABLE_DOMAINS.contains(domain);
    }
}
