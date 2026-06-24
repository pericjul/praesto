package ch.zhaw.praesto.model;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {

    private String studentName;
    
    // Aufgaben
    private long openAssignmentsCount;
    private List<AssignmentInfo> openAssignments;  // Alle offenen Aufgaben
    
    // Sessions
    private String lastSessionId;
    private Instant lastSessionStartedAt;
    private int totalSessionsCount;
    private String openSessionId;
    private int streakDays;          // Tage in Folge mit mind. einer Session
    
    // Bewerbungen
    private int applicationsCount;
    
    // Badges
    private long badgesCount;
    private List<String> earnedBadgeIcons;  // Icons der verdienten Badges
    
    // Mitteilungen
    private List<NotificationInfo> notifications;  // Feedback etc.

    // Anstehende Termine (Deadlines + Bewerbungsgespräche)
    private List<UpcomingEvent> upcomingEvents;
}