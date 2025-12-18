package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SchoolClassService schoolClassService;

    private SchoolClass testClass;

    @BeforeEach
    void setUp() {
        testClass = SchoolClass.builder()
                .id("class-123")
                .name("3SE2")
                .teacherId("teacher-123")
                .studentEmails(new ArrayList<>(List.of("student1@test.ch", "student2@test.ch")))
                .createdAt(Instant.now())
                .build();
    }

    // ========================================
    // createClass
    // ========================================
    @Nested
    @DisplayName("createClass")
    class CreateClass {

        @Test
        @DisplayName("Klasse erfolgreich erstellen")
        void createClass_valid_createsClass() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("4SE1");
            
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.existsByName("4SE1")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            SchoolClass result = schoolClassService.createClass(dto);

            assertThat(result).isNotNull();
            verify(schoolClassRepository).save(any(SchoolClass.class));
        }

        @Test
        @DisplayName("Nicht-Teacher darf keine Klasse erstellen")
        void createClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.createClass(new SchoolClassDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Leerer Name nicht erlaubt")
        void createClass_emptyName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("");
            
            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.createClass(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Klassenname");
        }

        @Test
        @DisplayName("Doppelter Name nicht erlaubt")
        void createClass_duplicateName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("3SE2");
            
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(schoolClassRepository.existsByName("3SE2")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.createClass(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("existiert bereits");
        }
    }

    // ========================================
    // getMyClasses
    // ========================================
    @Nested
    @DisplayName("getMyClasses")
    class GetMyClasses {

        @Test
        @DisplayName("Lehrer sieht eigene Klassen")
        void getMyClasses_asTeacher_returnsList() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findByTeacherId("teacher-123")).thenReturn(List.of(testClass));

            List<SchoolClass> result = schoolClassService.getMyClasses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("3SE2");
        }

        @Test
        @DisplayName("Nicht-Teacher hat keinen Zugriff")
        void getMyClasses_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.getMyClasses())
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Leere Liste wenn keine Klassen")
        void getMyClasses_noClasses_returnsEmptyList() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findByTeacherId("teacher-123")).thenReturn(List.of());

            List<SchoolClass> result = schoolClassService.getMyClasses();

            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // getClassById
    // ========================================
    @Nested
    @DisplayName("getClassById")
    class GetClassById {

        @Test
        @DisplayName("Teacher sieht eigene Klasse")
        void getClassById_ownClass_returnsClass() {
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(userService.getUserId()).thenReturn("teacher-123");
            when(userService.getEmail()).thenReturn("teacher@test.ch");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            SchoolClass result = schoolClassService.getClassById("class-123");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("3SE2");
        }

        @Test
        @DisplayName("Student in Klasse kann sie sehen")
        void getClassById_studentInClass_returnsClass() {
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(userService.getUserId()).thenReturn("student-123");
            when(userService.getEmail()).thenReturn("student1@test.ch");
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(userService.userHasRole("STUDENT")).thenReturn(true);

            SchoolClass result = schoolClassService.getClassById("class-123");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Klasse nicht gefunden")
        void getClassById_notFound_throwsNotFound() {
            when(schoolClassRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.getClassById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Anderer Teacher hat keinen Zugriff")
        void getClassById_otherTeacher_throwsForbidden() {
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(userService.getUserId()).thenReturn("other-teacher");
            when(userService.getEmail()).thenReturn("other@test.ch");
            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.getClassById("class-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Student nicht in Klasse hat keinen Zugriff")
        void getClassById_studentNotInClass_throwsForbidden() {
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(userService.getUserId()).thenReturn("student-xyz");
            when(userService.getEmail()).thenReturn("notinclass@test.ch");
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.getClassById("class-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getMyClass
    // ========================================
    @Nested
    @DisplayName("getMyClass")
    class GetMyClass {

        @Test
        @DisplayName("Student sieht eigene Klasse")
        void getMyClass_studentInClass_returnsClass() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getEmail()).thenReturn("student1@test.ch");
            when(schoolClassRepository.findFirstByStudentEmailsContaining("student1@test.ch"))
                    .thenReturn(Optional.of(testClass));

            SchoolClass result = schoolClassService.getMyClass();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Nicht-Student hat keine Klasse")
        void getMyClass_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.getMyClass())
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Student nicht in Klasse")
        void getMyClass_notInAnyClass_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getEmail()).thenReturn("notinclass@test.ch");
            when(schoolClassRepository.findFirstByStudentEmailsContaining("notinclass@test.ch"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.getMyClass())
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ========================================
    // addStudentToClass
    // ========================================
    @Nested
    @DisplayName("addStudentToClass")
    class AddStudent {

        @Test
        @DisplayName("Schüler erfolgreich hinzufügen")
        void addStudent_valid_addsStudent() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            SchoolClass result = schoolClassService.addStudentToClass("class-123", "newstudent@test.ch");

            assertThat(result).isNotNull();
            verify(schoolClassRepository).save(any(SchoolClass.class));
        }

        @Test
        @DisplayName("Nicht-Teacher kann nicht hinzufügen")
        void addStudent_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", "test@test.ch"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Leere Email nicht erlaubt")
        void addStudent_emptyEmail_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", ""))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("Ungültige Email nicht erlaubt")
        void addStudent_invalidEmail_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", "notanemail"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ungueltige");
        }

        @Test
        @DisplayName("Schüler bereits in Klasse")
        void addStudent_alreadyInClass_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", "student1@test.ch"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits in der Klasse");
        }

        @Test
        @DisplayName("Anderer Teacher kann nicht hinzufügen")
        void addStudent_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-teacher");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", "new@test.ch"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // removeStudentFromClass
    // ========================================
    @Nested
    @DisplayName("removeStudentFromClass")
    class RemoveStudent {

        @Test
        @DisplayName("Schüler erfolgreich entfernen")
        void removeStudent_valid_removesStudent() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            SchoolClass result = schoolClassService.removeStudentFromClass("class-123", "student1@test.ch");

            assertThat(result).isNotNull();
            verify(schoolClassRepository).save(any(SchoolClass.class));
        }

        @Test
        @DisplayName("Nicht-Teacher kann nicht entfernen")
        void removeStudent_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.removeStudentFromClass("class-123", "test@test.ch"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Anderer Teacher kann nicht entfernen")
        void removeStudent_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-teacher");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            assertThatThrownBy(() -> schoolClassService.removeStudentFromClass("class-123", "student1@test.ch"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // deleteClass
    // ========================================
    @Nested
    @DisplayName("deleteClass")
    class DeleteClass {

        @Test
        @DisplayName("Klasse erfolgreich löschen")
        void deleteClass_ownClass_deletesClass() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            schoolClassService.deleteClass("class-123");

            verify(schoolClassRepository).delete(testClass);
        }

        @Test
        @DisplayName("Nicht-Teacher kann nicht löschen")
        void deleteClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.deleteClass("class-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Klasse nicht gefunden")
        void deleteClass_notFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(schoolClassRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.deleteClass("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Anderer Teacher kann nicht löschen")
        void deleteClass_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-teacher");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            assertThatThrownBy(() -> schoolClassService.deleteClass("class-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getMyClassId
    // ========================================
    @Nested
    @DisplayName("getMyClassId")
    class GetMyClassId {

        @Test
        @DisplayName("Student in Klasse erhält ClassId")
        void getMyClassId_studentInClass_returnsId() {
            when(userService.getEmail()).thenReturn("student1@test.ch");
            when(schoolClassRepository.findFirstByStudentEmailsContaining("student1@test.ch"))
                    .thenReturn(Optional.of(testClass));

            String result = schoolClassService.getMyClassId();

            assertThat(result).isEqualTo("class-123");
        }

        @Test
        @DisplayName("Student nicht in Klasse erhält null")
        void getMyClassId_notInClass_returnsNull() {
            when(userService.getEmail()).thenReturn("notinclass@test.ch");
            when(schoolClassRepository.findFirstByStudentEmailsContaining("notinclass@test.ch"))
                    .thenReturn(Optional.empty());

            String result = schoolClassService.getMyClassId();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Null Email gibt null zurück")
        void getMyClassId_nullEmail_returnsNull() {
            when(userService.getEmail()).thenReturn(null);

            String result = schoolClassService.getMyClassId();

            assertThat(result).isNull();
            verify(schoolClassRepository, never()).findFirstByStudentEmailsContaining(any());
        }
    }

    // ========================================
    // updateClass
    // ========================================
    @Nested
    @DisplayName("updateClass")
    class UpdateClass {

        @Test
        @DisplayName("Klasse erfolgreich aktualisieren")
        void updateClass_valid_updatesClass() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("4SE1-Neu");

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.existsByName("4SE1-Neu")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            SchoolClass result = schoolClassService.updateClass("class-123", dto);

            assertThat(result).isNotNull();
            verify(schoolClassRepository).save(any(SchoolClass.class));
        }

        @Test
        @DisplayName("Nicht-Teacher darf nicht aktualisieren")
        void updateClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);

            assertThatThrownBy(() -> schoolClassService.updateClass("class-123", new SchoolClassDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Klasse nicht gefunden")
        void updateClass_notFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(schoolClassRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.updateClass("unknown", new SchoolClassDTO()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Anderer Teacher darf nicht aktualisieren")
        void updateClass_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-teacher");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));

            assertThatThrownBy(() -> schoolClassService.updateClass("class-123", new SchoolClassDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Doppelter Name bei Update nicht erlaubt")
        void updateClass_duplicateName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("ExistingClass");

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.existsByName("ExistingClass")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.updateClass("class-123", dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("existiert bereits");
        }

        @Test
        @DisplayName("Gleicher Name erlaubt (keine Änderung)")
        void updateClass_sameName_allowed() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("3SE2");

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            SchoolClass result = schoolClassService.updateClass("class-123", dto);

            assertThat(result).isNotNull();
            verify(schoolClassRepository, never()).existsByName(any());
        }

        @Test
        @DisplayName("Null Name wird ignoriert")
        void updateClass_nullName_keepsOldName() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName(null);

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            schoolClassService.updateClass("class-123", dto);

            assertThat(testClass.getName()).isEqualTo("3SE2");
        }

        @Test
        @DisplayName("Leerer Name wird ignoriert")
        void updateClass_blankName_keepsOldName() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("   ");

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            schoolClassService.updateClass("class-123", dto);

            assertThat(testClass.getName()).isEqualTo("3SE2");
        }
    }

    // ========================================
    // Zusätzliche Edge Cases
    // ========================================
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("createClass mit null DTO Name")
        void createClass_nullName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName(null);

            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.createClass(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Klassenname");
        }

        @Test
        @DisplayName("createClass trimmt Whitespace")
        void createClass_trimmedName_savesCorrectly() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("  4SE1  ");

            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.existsByName("4SE1")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            SchoolClass result = schoolClassService.createClass(dto);

            assertThat(result.getName()).isEqualTo("4SE1");
        }

        @Test
        @DisplayName("addStudent normalisiert Email zu Lowercase")
        void addStudent_uppercaseEmail_normalized() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getUserId()).thenReturn("teacher-123");
            when(schoolClassRepository.findById("class-123")).thenReturn(Optional.of(testClass));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(testClass);

            schoolClassService.addStudentToClass("class-123", "NEW@TEST.CH");

            assertThat(testClass.getStudentEmails()).contains("new@test.ch");
        }

        @Test
        @DisplayName("addStudent Klasse nicht gefunden")
        void addStudent_classNotFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(schoolClassRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("unknown", "test@test.ch"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("addStudent null Email")
        void addStudent_nullEmail_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);

            assertThatThrownBy(() -> schoolClassService.addStudentToClass("class-123", null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("removeStudent Klasse nicht gefunden")
        void removeStudent_classNotFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(schoolClassRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> schoolClassService.removeStudentFromClass("unknown", "test@test.ch"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("getMyClass Email wird zu Lowercase konvertiert")
        void getMyClass_uppercaseEmail_normalized() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getEmail()).thenReturn("STUDENT1@TEST.CH");
            when(schoolClassRepository.findFirstByStudentEmailsContaining("student1@test.ch"))
                    .thenReturn(Optional.of(testClass));

            SchoolClass result = schoolClassService.getMyClass();

            assertThat(result).isNotNull();
            verify(schoolClassRepository).findFirstByStudentEmailsContaining("student1@test.ch");
        }
    }
}
