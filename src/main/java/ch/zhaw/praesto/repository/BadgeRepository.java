package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.model.BadgeRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, String> {
    
    List<Badge> findAllByOrderBySortOrderAsc();
    
    List<Badge> findByRuleType(BadgeRuleType ruleType);
    
    Optional<Badge> findByRuleTypeAndThreshold(BadgeRuleType ruleType, int threshold);
}