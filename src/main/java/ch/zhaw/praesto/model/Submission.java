package ch.zhaw.praesto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    private String id;

    @Indexed
    private String schoolId;         // Mandanten-Isolation (Pflichtfeld)

    private String assignmentId;
    private String studentId;        // User.id
    private String studentEmail;     // Denormalisierung (für Badge-/Anzeige-Queries)
    private AssignmentType type;

    // Inhalt je nach Typ
    private String textContent;      // Für SELF_REFLECTION, RESEARCH
    private List<String> links;      // Für RESEARCH
    private String fileUrl;          // Für DOCUMENT_UPLOAD, VIDEO_PITCH
    private String fileName;         // Original-Dateiname
    private String comment;          // Optionaler Kommentar

    // AI_INTERVIEW: Referenz zur Chat-Session
    private String chatSessionId;

    private SubmissionStatus status;
    private Instant submittedAt;

    // Feedback vom Lehrer
    private String teacherFeedback;
    private Integer grade;           // Optional: Note/Punkte
    private Instant reviewedAt;
    private String reviewedByTeacherId;
}