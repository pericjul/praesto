package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Einfacher In-Memory-Rate-Limiter (pro Schlüssel, gleitendes Zeitfenster). Für einzelne
 * Endpunkte wie Registrierung und "Passwort vergessen", um Missbrauch (Massen-Anfragen,
 * Mail-Spam) zu bremsen. Bewusst schlank – kein externer Speicher nötig.
 */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private record Window(int count, Instant start) {}

    private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();
    private final LocaleMessages messages;

    /**
     * Registriert einen Treffer für {@code key}. Überschreitet die Anzahl innerhalb des
     * Zeitfensters {@code max}, wird eine Fehlermeldung geworfen.
     */
    public void hit(String key, int max, Duration window) {
        Instant now = Instant.now();
        counters.compute(key, (k, existing) -> {
            if (existing == null || Duration.between(existing.start(), now).compareTo(window) >= 0) {
                return new Window(1, now);
            }
            return new Window(existing.count() + 1, existing.start());
        });
        Window w = counters.get(key);
        if (w != null && w.count() > max) {
            throw new BadRequestException(messages.get("err.tooManyAttempts"));
        }
    }
}
