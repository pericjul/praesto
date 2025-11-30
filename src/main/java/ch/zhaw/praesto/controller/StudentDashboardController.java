package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.StudentDashboardResponse;
import ch.zhaw.praesto.service.StudentDashboardService;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService dashboardService;
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<StudentDashboardResponse> getDashboard() {

        if (!userService.userHasRole("STUDENT")) {
            throw new ForbiddenException("Nur Schueler duerfen dieses Dashboard sehen");
        }

        StudentDashboardResponse response = dashboardService.getDashboardForCurrentStudent();
        return ResponseEntity.ok(response);
    }
}