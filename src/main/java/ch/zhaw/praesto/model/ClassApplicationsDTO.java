package ch.zhaw.praesto.model;

import java.util.List;

/**
 * Bewerbungs-Übersicht einer Klasse für die Lehrperson. Pro Schüler:in wird angezeigt,
 * ob sie ihre Bewerbungen freigegeben hat; nur dann sind {@code applications} gefüllt.
 */
public record ClassApplicationsDTO(
        String classId,
        String className,
        List<StudentApplications> students) {

    public record StudentApplications(
            String studentId,
            String name,
            boolean shared,
            List<Application> applications) {
    }
}
