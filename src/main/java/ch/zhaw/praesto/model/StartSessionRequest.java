package ch.zhaw.praesto.model;

import lombok.Data;

@Data
public class StartSessionRequest {
    private String assignmentId;  // optional
}