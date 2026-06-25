package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ClassCockpitDTO;
import ch.zhaw.praesto.service.TeacherInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lehrer-Cockpit: Reife-Übersicht + Gesprächsleitfaden einer Klasse.
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class TeacherInsightsController {

    private final TeacherInsightsService teacherInsightsService;

    @GetMapping("/{id}/cockpit")
    public ClassCockpitDTO cockpit(@PathVariable String id) {
        return teacherInsightsService.getClassCockpit(id);
    }
}
