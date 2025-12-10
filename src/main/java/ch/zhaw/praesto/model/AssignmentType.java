package ch.zhaw.praesto.model;

public enum AssignmentType {
    AI_INTERVIEW("KI-Bewerbungsgespräch", "Übe ein Bewerbungsgespräch mit der KI"),
    DOCUMENT_UPLOAD("Dokument einreichen", "Lade ein Dokument hoch (PDF, Word)"),
    SELF_REFLECTION("Selbstreflexion", "Schreibe eine Reflexion zu einem Thema"),
    VIDEO_PITCH("Video-Bewerbung", "Nimm ein kurzes Bewerbungsvideo auf"),
    RESEARCH("Recherche", "Recherchiere zu einem Thema und fasse zusammen");

    private final String displayName;
    private final String description;

    AssignmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}