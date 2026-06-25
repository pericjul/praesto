package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.AiFeature;
import ch.zhaw.praesto.model.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiUsageRepository extends JpaRepository<AiUsage, String> {

    Optional<AiUsage> findByUserIdAndFeature(String userId, AiFeature feature);
}
