package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.InviteToken;
import ch.zhaw.praesto.model.InviteType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InviteTokenRepository extends MongoRepository<InviteToken, String> {

    Optional<InviteToken> findByToken(String token);

    List<InviteToken> findBySchoolIdAndIsActiveTrue(String schoolId);

    List<InviteToken> findBySchoolIdAndTypeAndIsActiveTrue(String schoolId, InviteType type);

    List<InviteToken> findByClassIdAndIsActiveTrue(String classId);

    void deleteBySchoolId(String schoolId);
}
