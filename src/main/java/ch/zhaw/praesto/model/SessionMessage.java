package ch.zhaw.praesto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessage {

    private String role;        // "USER" oder "ASSISTANT"

    @Column(columnDefinition = "text")
    private String content;     // Nachrichtentext (kann lang sein)

    private Instant createdAt;
}
