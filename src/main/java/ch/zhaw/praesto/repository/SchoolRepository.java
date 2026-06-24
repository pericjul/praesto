package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, String> {

    Optional<School> findByName(String name);

    boolean existsByName(String name);

    /** Zeitlich begrenzte Demo-Schulen, deren Fenster vor dem Stichzeitpunkt abgelaufen ist. */
    List<School> findByIsDemoTrueAndDemoAccessUntilBefore(Instant cutoff);
}
