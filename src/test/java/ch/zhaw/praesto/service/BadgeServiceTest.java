package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.BadgeRepository;
import ch.zhaw.praesto.repository.SessionRepository;
import ch.zhaw.praesto.repository.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private BadgeService badgeService;

    private Badge testBadge;

    @BeforeEach
    void setUp() {
        testBadge = Badge.builder()
                .id("badge-123")
                .title("Erste Schritte")
                .description("Erste Session abgeschlossen")
                .icon("🎯")
                .ruleType(BadgeRuleType.SESSIONS_COMPLETED)
                .threshold(1)
                .build();
    }

    @Nested
    @DisplayName("getAllBadges")
    class GetAllBadges {

        @Test
        @DisplayName("Alle Badges abrufen")
        void getAllBadges_returnsBadges() {
            when(badgeRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(testBadge));

            List<Badge> result = badgeService.getAllBadges();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Erste Schritte");
        }

        @Test
        @DisplayName("Keine Badges vorhanden")
        void getAllBadges_noBadges_returnsEmptyList() {
            when(badgeRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());

            List<Badge> result = badgeService.getAllBadges();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getEarnedBadgeCount")
    class GetEarnedBadgeCount {

        @Test
        @DisplayName("Anzahl verdienter Badges")
        void getEarnedBadgeCount_returnCount() {
            when(userBadgeRepository.countByStudentId("student-123")).thenReturn(5L);

            long result = badgeService.getEarnedBadgeCount("student-123");

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Keine Badges verdient")
        void getEarnedBadgeCount_noBadges_returnsZero() {
            when(userBadgeRepository.countByStudentId("student-123")).thenReturn(0L);

            long result = badgeService.getEarnedBadgeCount("student-123");

            assertThat(result).isEqualTo(0);
        }
    }
}
