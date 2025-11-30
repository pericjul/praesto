package ch.zhaw.praesto.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StudentDashboardResponse {

    // Begruessung
    private String studentName;

    // Aufgaben
    private long openAssignmentsCount;
    private String nextAssignmentTitle;
    private Instant nextAssignmentDueDate;

    // KI Training
    private String lastSessionId;
    private Instant lastSessionStartedAt;
    private int totalSessionsCount;

    // Badges (vorerst Platzhalter)
    private int badgesCount;

    // Notizen (vorerst Platzhalter)
    private int notesCount;
}