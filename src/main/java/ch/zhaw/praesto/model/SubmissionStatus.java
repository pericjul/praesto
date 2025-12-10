package ch.zhaw.praesto.model;

public enum SubmissionStatus {
    SUBMITTED("Eingereicht"),
    IN_REVIEW("In Prüfung"),
    REVIEWED("Bewertet"),
    RETURNED("Zurückgegeben");

    private final String displayName;

    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}