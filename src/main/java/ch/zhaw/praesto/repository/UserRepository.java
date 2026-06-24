package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findBySchoolId(String schoolId);

    List<User> findBySchoolIdAndRole(String schoolId, UserRole role);

    long countBySchoolId(String schoolId);

    long countBySchoolIdAndRole(String schoolId, UserRole role);

    Optional<User> findByRole(UserRole role);

    /**
     * Schüler einer Schule per Freitext-Suche (Vorname / Nachname / Email).
     * Case-insensitive Teilstring-Suche, gefiltert auf Schule + Rolle.
     */
    List<User> findBySchoolIdAndRoleAndFirstNameContainingIgnoreCase(String schoolId, UserRole role, String q);

    List<User> findBySchoolIdAndRoleAndLastNameContainingIgnoreCase(String schoolId, UserRole role, String q);

    List<User> findBySchoolIdAndRoleAndEmailContainingIgnoreCase(String schoolId, UserRole role, String q);

    void deleteBySchoolId(String schoolId);
}
