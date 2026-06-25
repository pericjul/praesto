package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.ClassChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassChallengeRepository extends JpaRepository<ClassChallenge, String> {

    Optional<ClassChallenge> findByClassIdAndActiveTrue(String classId);
}
