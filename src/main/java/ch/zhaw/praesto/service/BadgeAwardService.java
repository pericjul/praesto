package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.UserBadge;
import ch.zhaw.praesto.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Vergibt einzelne Badges in einer EIGENEN Transaktion (REQUIRES_NEW) und ist
 * tolerant gegen parallele Doppel-Vergabe. Verhindert, dass eine Race-Condition
 * (z.B. mehrere gleichzeitige close/submit-Requests) mit
 * "duplicate key ... uq_student_badge" die aufrufende Transaktion (Session
 * schliessen) zum Absturz bringt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeAwardService {

    private final UserBadgeRepository userBadgeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean awardIfAbsent(String studentId, String badgeId) {
        if (userBadgeRepository.existsByStudentIdAndBadgeId(studentId, badgeId)) {
            return false;
        }
        try {
            userBadgeRepository.saveAndFlush(UserBadge.builder()
                    .studentId(studentId)
                    .badgeId(badgeId)
                    .earnedAt(Instant.now())
                    .build());
            return true;
        } catch (DataIntegrityViolationException e) {
            // Wurde parallel bereits vergeben – kein Fehler.
            return false;
        }
    }
}
