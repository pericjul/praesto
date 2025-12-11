package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.service.BadgeService;
import ch.zhaw.praesto.service.BadgeService.BadgeWithEarnedInfo;
import ch.zhaw.praesto.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final UserService userService;  // UserService statt AuthUtil

    /**
     * Alle verfügbaren Badges abrufen
     */
    @GetMapping
    public ResponseEntity<List<Badge>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    /**
     * Meine Badges mit Earned-Info abrufen
     */
    @GetMapping("/my")
    public ResponseEntity<List<BadgeWithEarnedInfo>> getMyBadges() {
        String userId = userService.getUserId();
        return ResponseEntity.ok(badgeService.getBadgesWithEarnedInfo(userId));
    }

    /**
     * Anzahl meiner verdienten Badges
     */
    @GetMapping("/my/count")
    public ResponseEntity<Long> getMyBadgeCount() {
        String userId = userService.getUserId();
        return ResponseEntity.ok(badgeService.getEarnedBadgeCount(userId));
    }

    /**
     * Manueller Badge-Check (für Testing)
     */
    @PostMapping("/check")
    public ResponseEntity<List<Badge>> checkMyBadges() {
        String userId = userService.getUserId();
        List<Badge> newBadges = badgeService.checkAndAwardBadges(userId);
        return ResponseEntity.ok(newBadges);
    }
}