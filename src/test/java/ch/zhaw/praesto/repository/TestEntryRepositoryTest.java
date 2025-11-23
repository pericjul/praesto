package ch.zhaw.praesto.repository;

import ch.zhaw.praesto.model.TestEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TestEntryRepositoryTest {

    @Autowired
    private TestEntryRepository repo;

    @Test
    void crud_works() {
        // create
        TestEntry saved = repo.save(
                TestEntry.builder().text("hello mongo").build()
        );
        assertThat(saved.getId()).isNotNull();

        // read
        Optional<TestEntry> found = repo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("hello mongo");

        // update
        saved.setText("updated");
        repo.save(saved);
        TestEntry updated = repo.findById(saved.getId()).get();
        assertThat(updated.getText()).isEqualTo("updated");

        // delete
        repo.deleteById(saved.getId());
        assertThat(repo.findById(saved.getId())).isEmpty();
    }
}