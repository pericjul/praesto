package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByAuth0Id(String auth0Id);
}