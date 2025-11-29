package ch.zhaw.praesto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    private String studentId;     // aus JWT
    private String assignmentId;  // optional

    @Builder.Default
    private List<SessionMessage> messages = new ArrayList<>();

    private Integer score;        // spaeter fuer Auswertung

    private SessionStatus status; // OPEN oder CLOSED

    private Instant startedAt;
    private Instant closedAt;
}