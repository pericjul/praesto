package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.TestEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestEntryRepository extends MongoRepository<TestEntry, String> {}