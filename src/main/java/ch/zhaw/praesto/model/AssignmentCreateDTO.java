package ch.zhaw.praesto.model;

import lombok.Data;

import java.util.List;

@Data
public class AssignmentCreateDTO {
    private String title;
    private String description;
    private Integer durationMin;
    private String dueDate;
    private String classId;            // Einzelklasse (Rückwärtskompatibilität)
    private List<String> classIds;     // Mehrere Klassen gleichzeitig (optional)
    private AssignmentType type;
}