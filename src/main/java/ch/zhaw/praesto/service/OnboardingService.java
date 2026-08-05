package ch.zhaw.praesto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

/**
 * Kurze Willkommens-Umfrage direkt nach der Registrierung (rollenspezifisch). Die Antworten
 * werden als JSON gespeichert. Bewusst über {@link JdbcTemplate} (CREATE TABLE IF NOT EXISTS),
 * damit das Feature unabhängig von der ddl-auto-Einstellung der Prod-DB funktioniert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final JdbcTemplate jdbc;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS onboarding_survey (
                        user_id varchar(64) PRIMARY KEY,
                        role varchar(32),
                        answers text,
                        created_at timestamp
                    )
                    """);
            log.info("onboarding_survey-Tabelle bereit.");
        } catch (Exception e) {
            log.error("onboarding_survey-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Hat der aktuelle User die Willkommens-Umfrage schon ausgefüllt? */
    public boolean isCompleted() {
        String userId = userService.getUserId();
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM onboarding_survey WHERE user_id = ?", Integer.class, userId);
        return count != null && count > 0;
    }

    /** Antworten des aktuellen Users speichern (Upsert). */
    public void save(Map<String, Object> answers) {
        String userId = userService.getUserId();
        String role = userService.getCurrentUserRole() != null ? userService.getCurrentUserRole().name() : null;
        String json;
        try {
            json = objectMapper.writeValueAsString(answers != null ? answers : Map.of());
        } catch (Exception e) {
            json = "{}";
        }
        jdbc.update("DELETE FROM onboarding_survey WHERE user_id = ?", userId);
        jdbc.update("INSERT INTO onboarding_survey (user_id, role, answers, created_at) VALUES (?,?,?,?)",
                userId, role, json, Timestamp.from(Instant.now()));
    }
}
