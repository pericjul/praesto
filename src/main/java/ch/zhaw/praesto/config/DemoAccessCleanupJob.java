package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.DemoRequestStatus;
import ch.zhaw.praesto.model.School;
import ch.zhaw.praesto.repository.DemoRequestRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import ch.zhaw.praesto.service.SchoolDataCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Entfernt stündlich abgelaufene, zeitlich begrenzte Demo-Schulen samt aller Daten,
 * sobald ihr gebuchter Tag vorbei ist. Markiert die zugehörige Anfrage als erledigt.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoAccessCleanupJob {

    private final SchoolRepository schoolRepository;
    private final DemoRequestRepository demoRequestRepository;
    private final SchoolDataCleaner schoolDataCleaner;

    @Scheduled(cron = "0 15 * * * *", zone = "Europe/Zurich")
    public void cleanupExpiredDemoSchools() {
        List<School> expired = schoolRepository.findByIsDemoTrueAndDemoAccessUntilBefore(Instant.now());
        for (School school : expired) {
            demoRequestRepository.findByApprovedSchoolId(school.getId()).ifPresent(request -> {
                request.setStatus(DemoRequestStatus.DONE);
                demoRequestRepository.save(request);
            });
            schoolDataCleaner.deleteSchoolAndData(school.getId());
            log.info("Abgelaufene Demo-Schule '{}' entfernt", school.getName());
        }
    }
}
