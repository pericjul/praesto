package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.Badge;
import ch.zhaw.praesto.model.BadgeRuleType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {
    
    List<Badge> findAllByOrderBySortOrderAsc();
    
    List<Badge> findByRuleType(BadgeRuleType ruleType);
    
    Optional<Badge> findByRuleTypeAndThreshold(BadgeRuleType ruleType, int threshold);
}