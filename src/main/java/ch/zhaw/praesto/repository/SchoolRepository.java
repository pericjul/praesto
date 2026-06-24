package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.School;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SchoolRepository extends MongoRepository<School, String> {

    Optional<School> findByName(String name);

    boolean existsByName(String name);
}
