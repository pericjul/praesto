package ch.zhaw.praesto.model;

import lombok.Data;

@Data
public class AssignmentCreateDTO {
    private String title;
    private String description;
    private Integer durationMin;
    private String dueDate;   
    private String classId;
    private AssignmentType type;  // NEU
}