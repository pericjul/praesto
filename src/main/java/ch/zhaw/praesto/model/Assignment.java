package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Indexed
    private String schoolId;        // Mandanten-Isolation (Pflichtfeld)

    private String title;
    private String description;
    private Integer durationMin;
    private Instant dueDate;

    private AssignmentType type;
    private AssignmentStatus status;

    private String classId;
    private String createdByTeacherId;

    private Instant createdAt;
}