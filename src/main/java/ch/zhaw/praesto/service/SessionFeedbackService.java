package ch.zhaw.praesto.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lehrer-Feedback (Text + optionale Note) zu einem KI-Chat – unabhängig davon, ob der Chat
 * als Aufgabe abgegeben wurde. So kann eine Lehrperson auch zu freien Übungs-/Schnupper-Chats
 * Feedback geben.
 *
 * Bewusst über {@link JdbcTemplate} statt JPA (CREATE TABLE IF NOT EXISTS), damit das Feature
 * unabhängig von der ddl-auto-Einstellung der Prod-DB funktioniert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionFeedbackService {

    private final JdbcTemplate jdbc;

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS session_feedback (
                        session_id varchar(64) PRIMARY KEY,
                        teacher_feedback text,
                        grade double precision,
                        updated_at timestamp,
                        updated_by varchar(64)
                    )
                    """);
            log.info("session_feedback-Tabelle bereit.");
        } catch (Exception e) {
            log.error("session_feedback-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Feedback/Note speichern (Upsert, DB-übergreifend). */
    public void save(String sessionId, String feedback, Double grade, String updatedBy) {
        jdbc.update("DELETE FROM session_feedback WHERE session_id = ?", sessionId);
        jdbc.update("INSERT INTO session_feedback (session_id, teacher_feedback, grade, updated_at, updated_by) "
                        + "VALUES (?,?,?,?,?)",
                sessionId, feedback, grade, Timestamp.from(Instant.now()), updatedBy);
    }

    /** {@code {teacherFeedback, grade}} oder leere Map, wenn keins vorhanden. */
    public Map<String, Object> get(String sessionId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT teacher_feedback, grade FROM session_feedback WHERE session_id = ?", sessionId);
        if (rows.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> r = rows.get(0);
        Map<String, Object> out = new HashMap<>();
        out.put("teacherFeedback", r.get("teacher_feedback"));
        out.put("grade", r.get("grade"));
        return out;
    }
}
