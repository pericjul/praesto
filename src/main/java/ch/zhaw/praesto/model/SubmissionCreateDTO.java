package ch.zhaw.praesto.model;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionCreateDTO {
    private String assignmentId;
    
    // Für Text-basierte Abgaben (SELF_REFLECTION, RESEARCH)
    private String textContent;
    
    // Für RESEARCH
    private List<String> links;
    
    // Für File-Uploads (DOCUMENT_UPLOAD, VIDEO_PITCH)
    private String fileUrl;
    private String fileName;
    
    // Optionaler Kommentar
    private String comment;
    
    // Für AI_INTERVIEW
    private String chatSessionId;
}