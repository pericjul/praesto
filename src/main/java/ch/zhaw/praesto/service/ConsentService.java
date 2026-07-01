package ch.zhaw.praesto.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Verfolgt, für welche Schüler:innen die unterschriebene Einverständniserklärung
 * der Eltern vorliegt. Die Lehrperson hakt das pro Schüler:in ab.
 *
 * Bewusst über {@link JdbcTemplate} statt JPA (CREATE TABLE IF NOT EXISTS), damit
 * das Feature unabhängig von der ddl-auto-Einstellung der Prod-DB funktioniert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {

    private final JdbcTemplate jdbc;

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS student_consent (
                        student_id varchar(64) PRIMARY KEY,
                        school_id varchar(64),
                        signed boolean,
                        updated_at timestamp,
                        updated_by varchar(64)
                    )
                    """);
            log.info("student_consent-Tabelle bereit.");
        } catch (Exception e) {
            log.error("student_consent-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Setzt/aktualisiert den Einverständnis-Status einer Schüler:in (Upsert, DB-übergreifend). */
    public void setSigned(String studentId, String schoolId, boolean signed, String updatedBy) {
        jdbc.update("DELETE FROM student_consent WHERE student_id = ?", studentId);
        jdbc.update("INSERT INTO student_consent (student_id, school_id, signed, updated_at, updated_by) VALUES (?,?,?,?,?)",
                studentId, schoolId, signed, Timestamp.from(Instant.now()), updatedBy);
    }

    /** Alle Schüler-IDs einer Schule, für die das Einverständnis als unterschrieben markiert ist. */
    public Set<String> signedStudentIds(String schoolId) {
        if (schoolId == null) {
            return Set.of();
        }
        return new HashSet<>(jdbc.queryForList(
                "SELECT student_id FROM student_consent WHERE school_id = ? AND signed = TRUE",
                String.class, schoolId));
    }
}
