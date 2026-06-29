package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.config.InMemoryLogAppender;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Stellt die letzten Server-Logs für den SUPER_ADMIN bereit (App-interner
 * Log-Viewer statt Azure Log stream).
 */
@RestController
@RequestMapping("/api/super/logs")
@RequiredArgsConstructor
public class SuperLogController {

    private final UserService userService;

    @GetMapping
    public List<String> logs() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Nur Super-Admin");
        }
        return InMemoryLogAppender.recent();
    }
}
