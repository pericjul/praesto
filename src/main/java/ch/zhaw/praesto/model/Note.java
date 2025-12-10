package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;

    private String studentId;      // aus JWT
    private String companyName;    // optional
    private String position;       // optional
    private String text;           // Notiztext

    private Instant createdAt;
    private Instant lastUpdated;
}