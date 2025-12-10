package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    private String id;

    private String title;
    private String description;
    private Integer durationMin;
    private Instant dueDate;

    private AssignmentType type;      // NEU: Art der Aufgabe
    private AssignmentStatus status;

    private String classId;
    private String createdByTeacherId;

    private Instant createdAt;
}