package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, String> {
    
    List<UserBadge> findByStudentIdOrderByEarnedAtDesc(String studentId);
    
    Optional<UserBadge> findByStudentIdAndBadgeId(String studentId, String badgeId);
    
    boolean existsByStudentIdAndBadgeId(String studentId, String badgeId);
    
    long countByStudentId(String studentId);

    void deleteByStudentIdIn(java.util.Collection<String> studentIds);
}