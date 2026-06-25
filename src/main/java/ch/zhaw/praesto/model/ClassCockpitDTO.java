package ch.zhaw.praesto.model;

import java.time.Instant;
import java.util.List;

/**
 * Lehrer-Klassen-Cockpit: pro Schüler:in Reife/Score + Aktivität, plus ein
 * abgeleiteter Gesprächsleitfaden (wen ansprechen und warum). "reasonCode" wird
 * im Frontend lokalisiert (NEVER_PRACTICED, LOW_SCORE, INACTIVE).
 */
public record ClassCockpitDTO(
        String classId,
        String className,
        int studentCount,
        int practicedCount,
        Integer avgClassScore,
        int needAttentionCount,
        List<Student> students,
        List<Guidance> guidance) {

    public record Student(
            String studentId,
            String name,
            int sessionCount,
            int closedCount,
            Integer bestScore,
            Integer avgScore,
            Instant lastActivity,
            long submissionCount,
            boolean needsAttention,
            List<String> reasons) {
    }

    public record Guidance(
            String studentId,
            String name,
            String reasonCode) {
    }
}
