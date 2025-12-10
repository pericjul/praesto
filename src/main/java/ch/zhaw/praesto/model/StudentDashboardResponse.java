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
    
    // Offene Session zum Fortsetzen (falls vorhanden)
    private String openSessionId;

    // Badges (vorerst Platzhalter)
    private int badgesCount;

    // Notizen (vorerst Platzhalter)
    private int notesCount;
    
    // Bewerbungen (vorerst Platzhalter)
    private int applicationsCount;
}