package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    private String studentId;           // aus JWT
    private String companyName;         // Firmenname
    private String position;            // Position/Stelle
    private ApplicationStatus status;   // Status der Bewerbung

    private LocalDate appliedAt;        // Datum der Bewerbung
    private LocalDate interviewDate;    // Gesprächstermin (optional)
    private String notes;               // Notizen zur Bewerbung

    private Instant createdAt;
    private Instant updatedAt;
}