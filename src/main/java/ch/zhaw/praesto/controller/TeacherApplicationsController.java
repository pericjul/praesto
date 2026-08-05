package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.ClassApplicationsDTO;
import ch.zhaw.praesto.service.TeacherApplicationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lehrer-Ansicht: Bewerbungs-Übersicht einer Klasse (nur freigegebene Bewerbungen).
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherApplicationsController {

    private final TeacherApplicationsService teacherApplicationsService;

    @GetMapping("/class/{classId}/applications")
    public ResponseEntity<ClassApplicationsDTO> classApplications(@PathVariable String classId) {
        return ResponseEntity.ok(teacherApplicationsService.getClassApplications(classId));
    }
}
