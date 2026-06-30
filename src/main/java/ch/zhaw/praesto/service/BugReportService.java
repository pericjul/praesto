package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.BugReport;
import ch.zhaw.praesto.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Bug-Meldungen. Bewusst über {@link JdbcTemplate} statt JPA, damit die Tabelle per
 * {@code CREATE TABLE IF NOT EXISTS} selbst angelegt wird – unabhängig von der
 * ddl-auto-Einstellung der Prod-DB und ohne Risiko für den App-Start.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BugReportService {

    private static final Set<String> STATUSES = Set.of("NEW", "IN_PROGRESS", "RESOLVED", "WONTFIX");

    private final JdbcTemplate jdbc;

    private static final RowMapper<BugReport> MAPPER = (rs, n) -> new BugReport(
            rs.getString("id"),
            rs.getString("school_id"),
            rs.getString("reporter_id"),
            rs.getString("reporter_name"),
            rs.getString("reporter_email"),
            rs.getString("reporter_role"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("area"),
            rs.getString("severity"),
            rs.getString("steps"),
            rs.getString("device"),
            rs.getString("status"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null);

    @PostConstruct
    void ensureTable() {
        try {
            jdbc.execute("""
                    CREATE TABLE IF NOT EXISTS bug_reports (
                        id varchar(64) PRIMARY KEY,
                        school_id varchar(64),
                        reporter_id varchar(64),
                        reporter_name varchar(255),
                        reporter_email varchar(255),
                        reporter_role varchar(32),
                        title varchar(500),
                        description text,
                        area varchar(64),
                        severity varchar(32),
                        steps text,
                        device varchar(255),
                        status varchar(32),
                        created_at timestamp
                    )
                    """);
            log.info("bug_reports-Tabelle bereit.");
        } catch (Exception e) {
            log.error("bug_reports-Tabelle konnte nicht angelegt werden: {}", e.getMessage());
        }
    }

    /** Bug von der aktuell eingeloggten Person anlegen. */
    public void create(User reporter, String title, String description, String area,
                       String severity, String steps, String device) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Bitte gib dem Bug einen kurzen Titel.");
        }
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Bitte beschreibe den Bug.");
        }
        String name = (safe(reporter.getFirstName()) + " " + safe(reporter.getLastName())).trim();
        jdbc.update("""
                        INSERT INTO bug_reports
                          (id, school_id, reporter_id, reporter_name, reporter_email, reporter_role,
                           title, description, area, severity, steps, device, status, created_at)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                        """,
                UUID.randomUUID().toString(),
                reporter.getSchoolId(),
                reporter.getId(),
                name.isBlank() ? reporter.getEmail() : name,
                reporter.getEmail(),
                reporter.getRole() != null ? reporter.getRole().name() : null,
                trim(title, 500),
                description,
                trim(area, 64),
                trim(severity, 32),
                steps,
                trim(device, 255),
                "NEW",
                Timestamp.from(Instant.now()));
    }

    /** Alle Bugs (für den Super-Admin), neueste zuerst. */
    public List<BugReport> listAll() {
        return jdbc.query("SELECT * FROM bug_reports ORDER BY created_at DESC", MAPPER);
    }

    public void updateStatus(String id, String status) {
        if (status == null || !STATUSES.contains(status)) {
            throw new BadRequestException("Ungültiger Status.");
        }
        jdbc.update("UPDATE bug_reports SET status = ? WHERE id = ?", status, id);
    }

    public void delete(String id) {
        jdbc.update("DELETE FROM bug_reports WHERE id = ?", id);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
