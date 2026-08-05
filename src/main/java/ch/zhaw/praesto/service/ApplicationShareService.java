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
 * Freigabe: Ob ein:e Schüler:in die eigenen Bewerbungen mit der/den Lehrperson(en) teilt.
 * Standard = NICHT geteilt (Datenschutz, Opt-in). Die Schüler:in steuert das selbst.
 *
 * Bewusst über {@link JdbcTemplate} (CREATE TABLE IF NOT EXISTS), damit das Feature
 * unabhängig von der ddl-auto-Einstellung der Prod-DB funktioniert – wie bei {@link ConsentService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationShareService {

    private final JdbcTemplate jdbc;

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS application_sharing (
                        student_id varchar(64) PRIMARY KEY,
                        school_id varchar(64),
                        shared boolean,
                        updated_at timestamp
                    )
                    """);
            log.info("application_sharing-Tabelle bereit.");
        } catch (Exception e) {
            log.error("application_sharing-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Freigabe einer Schüler:in setzen (Upsert). */
    public void setShared(String studentId, String schoolId, boolean shared) {
        jdbc.update("DELETE FROM application_sharing WHERE student_id = ?", studentId);
        jdbc.update("INSERT INTO application_sharing (student_id, school_id, shared, updated_at) VALUES (?,?,?,?)",
                studentId, schoolId, shared, Timestamp.from(Instant.now()));
    }

    /** Teilt diese:r Schüler:in die Bewerbungen? (Standard false). */
    public boolean isShared(String studentId) {
        if (studentId == null) {
            return false;
        }
        Boolean v = jdbc.query(
                "SELECT shared FROM application_sharing WHERE student_id = ?",
                rs -> rs.next() ? rs.getBoolean("shared") : Boolean.FALSE,
                studentId);
        return Boolean.TRUE.equals(v);
    }

    /** Alle Schüler-IDs einer Schule, die ihre Bewerbungen freigegeben haben. */
    public Set<String> sharedStudentIds(String schoolId) {
        if (schoolId == null) {
            return Set.of();
        }
        return new HashSet<>(jdbc.queryForList(
                "SELECT student_id FROM application_sharing WHERE school_id = ? AND shared = TRUE",
                String.class, schoolId));
    }
}
