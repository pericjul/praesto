package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.CalendarEvent;
import ch.zhaw.praesto.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kalender-Termine fürs Dashboard (Schüler & Lehrer).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/student/calendar")
    public List<CalendarEvent> studentCalendar() {
        return calendarService.studentCalendar();
    }

    @GetMapping("/teacher/calendar")
    public List<CalendarEvent> teacherCalendar() {
        return calendarService.teacherCalendar();
    }
}
