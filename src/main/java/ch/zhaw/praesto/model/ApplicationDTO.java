package ch.zhaw.praesto.model;

import lombok.Data;

@Data
public class ApplicationDTO {
    private String companyName;
    private String position;
    private String status;          // String, wird zu Enum konvertiert
    private String appliedAt;       // ISO Date String (yyyy-MM-dd)
    private String interviewDate;   // ISO Date String (optional)
    private String notes;
}