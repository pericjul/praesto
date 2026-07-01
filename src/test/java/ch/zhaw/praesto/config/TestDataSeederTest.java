package ch.zhaw.praesto.config;

import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.SchoolRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import ch.zhaw.praesto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "praesto.seed-test-data=true")
class TestDataSeederTest {

    @Autowired SchoolRepository schools;
    @Autowired UserRepository users;
    @Autowired SchoolClassRepository classes;
    @Autowired SubmissionRepository submissions;

    @Test
    void seedsCompleteTestScenario() {
        assertThat(schools.findByName("Testschule Pilot")).isPresent();

        var teacher = users.findByEmail("lehrer.test@praesto.ch");
        var admin = users.findByEmail("admin.test@praesto.ch");
        var student = users.findByEmail("schueler1.test@praesto.ch");
        assertThat(teacher).isPresent();
        assertThat(teacher.get().getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(admin).isPresent();
        assertThat(student).isPresent();

        String schoolId = teacher.get().getSchoolId();
        assertThat(classes.findBySchoolIdAndTeacherId(schoolId, teacher.get().getId())).hasSize(1);
        // mind. eine Abgabe zum Bewerten + eine bewertete
        assertThat(submissions.count()).isGreaterThanOrEqualTo(2);
    }
}
