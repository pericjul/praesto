package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.ApplicationRepository;
import ch.zhaw.praesto.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Liefert Termine fürs Kalender-Dashboard – jeweils mit Link zum Detail.
 * Schüler: Aufgaben-Deadlines der eigenen Klasse + geplante Bewerbungsgespräche.
 * Lehrer: Aufgaben-Deadlines aller eigenen Klassen.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Zurich");

    private final UserService userService;
    private final SchoolClassService schoolClassService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationRepository applicationRepository;

    public List<CalendarEvent> studentCalendar() {
        List<CalendarEvent> events = new ArrayList<>();

        String classId = schoolClassService.getMyClassId();
        if (classId != null && !classId.isBlank()) {
            for (Assignment a : assignmentRepository.findByClassId(classId)) {
                if (a.getStatus() != AssignmentStatus.CLOSED && a.getDueDate() != null) {
                    events.add(new CalendarEvent("ASSIGNMENT", a.getTitle(), a.getDueDate(),
                            "/student/assignments/" + a.getId()));
                }
            }
        }

        for (Application app : applicationRepository.findByStudentId(userService.getUserId())) {
            if (app.getInterviewDate() != null) {
                events.add(new CalendarEvent("INTERVIEW", app.getCompanyName(),
                        app.getInterviewDate().atStartOfDay(ZONE).toInstant(),
                        "/student/applications"));
            }
        }
        return events;
    }

    public List<CalendarEvent> teacherCalendar() {
        List<CalendarEvent> events = new ArrayList<>();
        for (SchoolClass schoolClass : schoolClassService.getMyClasses()) {
            for (Assignment a : assignmentRepository.findByClassId(schoolClass.getId())) {
                if (a.getStatus() != AssignmentStatus.CLOSED && a.getDueDate() != null) {
                    events.add(new CalendarEvent("ASSIGNMENT",
                            a.getTitle() + " · " + schoolClass.getName(),
                            a.getDueDate(),
                            "/teacher/assignments/" + a.getId()));
                }
            }
        }
        return events;
    }
}
