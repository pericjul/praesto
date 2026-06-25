package ch.zhaw.praesto.model;

import lombok.Data;

@Data
public class StartSessionRequest {
    private String assignmentId;  // optional
    private boolean roast;        // Roast-Modus (nur beim freien Üben wirksam)
}