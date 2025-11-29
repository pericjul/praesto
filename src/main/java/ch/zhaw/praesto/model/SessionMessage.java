package ch.zhaw.praesto.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessage {

    private String role;        // "user" oder "assistant"
    private String content;     // Nachrichtentext
    private Instant createdAt;
    
}