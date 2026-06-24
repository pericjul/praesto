package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.SchoolClassRepository;
import ch.zhaw.praesto.repository.UserRepository;
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

/**
 * Unit-Tests für SchoolClassService nach der Multi-Tenant-Umstellung
 * (schoolId-Isolation, studentIds statt studentEmails).
 */
@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    private static final String SCHOOL = "school-1";
    private static final String TEACHER = "teacher-123";
    private static final String CLASS_ID = "class-123";

    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private SchoolClassService schoolClassService;

    private SchoolClass testClass;

    @BeforeEach
    void setUp() {
        testClass = SchoolClass.builder()
                .id(CLASS_ID)
                .schoolId(SCHOOL)
                .name("3SE2")
                .teacherId(TEACHER)
                .studentIds(new ArrayList<>(List.of("stud-1", "stud-2")))
                .createdAt(Instant.now())
                .build();
    }

    private User student(String id, String schoolId) {
        return User.builder().id(id).email(id + "@test.ch").role(UserRole.STUDENT).schoolId(schoolId).build();
    }

    @Nested
    @DisplayName("createClass")
    class CreateClass {

        @Test
        void createClass_valid_createsClass() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("4SE1");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.existsBySchoolIdAndName(SCHOOL, "4SE1")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            SchoolClass result = schoolClassService.createClass(dto);

            assertThat(result.getName()).isEqualTo("4SE1");
            assertThat(result.getSchoolId()).isEqualTo(SCHOOL);
            assertThat(result.getTeacherId()).isEqualTo(TEACHER);
        }

        @Test
        void createClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.createClass(new SchoolClassDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void createClass_emptyName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            assertThatThrownBy(() -> schoolClassService.createClass(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Klassenname");
        }

        @Test
        void createClass_duplicateName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("3SE2");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.existsBySchoolIdAndName(SCHOOL, "3SE2")).thenReturn(true);
            assertThatThrownBy(() -> schoolClassService.createClass(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("existiert bereits");
        }

        @Test
        void createClass_trimsName() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("  4SE1  ");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.existsBySchoolIdAndName(SCHOOL, "4SE1")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThat(schoolClassService.createClass(dto).getName()).isEqualTo("4SE1");
        }
    }

    @Nested
    @DisplayName("getMyClasses")
    class GetMyClasses {

        @Test
        void getMyClasses_asTeacher_returnsList() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.findBySchoolIdAndTeacherId(SCHOOL, TEACHER)).thenReturn(List.of(testClass));

            assertThat(schoolClassService.getMyClasses()).hasSize(1);
        }

        @Test
        void getMyClasses_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.getMyClasses())
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("getClassById")
    class GetClassById {

        @Test
        void getClassById_ownClass_returnsClass() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.userHasRole("STUDENT")).thenReturn(false);
            when(userService.getCurrentUserId()).thenReturn(TEACHER);

            assertThat(schoolClassService.getClassById(CLASS_ID).getName()).isEqualTo("3SE2");
        }

        @Test
        void getClassById_studentInClass_returnsClass() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getCurrentUserId()).thenReturn("stud-1");

            assertThat(schoolClassService.getClassById(CLASS_ID)).isNotNull();
        }

        @Test
        void getClassById_notFound_throwsNotFound() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId("unknown", SCHOOL)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> schoolClassService.getClassById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void getClassById_otherTeacher_throwsForbidden() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentUserId()).thenReturn("other-teacher");
            assertThatThrownBy(() -> schoolClassService.getClassById(CLASS_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void getClassById_studentNotInClass_throwsForbidden() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getCurrentUserId()).thenReturn("stud-xyz");
            assertThatThrownBy(() -> schoolClassService.getClassById(CLASS_ID))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("getMyClass / getMyClassId")
    class GetMyClass {

        @Test
        void getMyClass_studentInClass_returnsClass() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn("stud-1");
            when(schoolClassRepository.findFirstBySchoolIdAndStudentIdsContaining(SCHOOL, "stud-1"))
                    .thenReturn(Optional.of(testClass));
            assertThat(schoolClassService.getMyClass()).isNotNull();
        }

        @Test
        void getMyClass_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.getMyClass())
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void getMyClass_notInAnyClass_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn("stud-x");
            when(schoolClassRepository.findFirstBySchoolIdAndStudentIdsContaining(SCHOOL, "stud-x"))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> schoolClassService.getMyClass())
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void getMyClassId_studentInClass_returnsId() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn("stud-1");
            when(schoolClassRepository.findFirstBySchoolIdAndStudentIdsContaining(SCHOOL, "stud-1"))
                    .thenReturn(Optional.of(testClass));
            assertThat(schoolClassService.getMyClassId()).isEqualTo(CLASS_ID);
        }

        @Test
        void getMyClassId_notInClass_returnsNull() {
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(userService.getCurrentUserId()).thenReturn("stud-x");
            when(schoolClassRepository.findFirstBySchoolIdAndStudentIdsContaining(SCHOOL, "stud-x"))
                    .thenReturn(Optional.empty());
            assertThat(schoolClassService.getMyClassId()).isNull();
        }
    }

    @Nested
    @DisplayName("addStudentToClass")
    class AddStudent {

        @Test
        void addStudent_valid_addsStudent() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(userRepository.findById("stud-new")).thenReturn(Optional.of(student("stud-new", SCHOOL)));
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            SchoolClass result = schoolClassService.addStudentToClass(CLASS_ID, "stud-new");

            assertThat(result.getStudentIds()).contains("stud-new");
        }

        @Test
        void addStudent_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, "stud-new"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void addStudent_blankUserId_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, ""))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("userId");
        }

        @Test
        void addStudent_differentSchool_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(userRepository.findById("stud-other")).thenReturn(Optional.of(student("stud-other", "other-school")));
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, "stud-other"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void addStudent_alreadyInClass_throwsBadRequest() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(userRepository.findById("stud-1")).thenReturn(Optional.of(student("stud-1", SCHOOL)));
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, "stud-1"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("bereits in der Klasse");
        }

        @Test
        void addStudent_studentNotFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(userRepository.findById("ghost")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, "ghost"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void addStudent_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn("other-teacher");
            assertThatThrownBy(() -> schoolClassService.addStudentToClass(CLASS_ID, "stud-new"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("removeStudentFromClass")
    class RemoveStudent {

        @Test
        void removeStudent_valid_removesStudent() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            SchoolClass result = schoolClassService.removeStudentFromClass(CLASS_ID, "stud-1");

            assertThat(result.getStudentIds()).doesNotContain("stud-1");
        }

        @Test
        void removeStudent_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.removeStudentFromClass(CLASS_ID, "stud-1"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("updateClass")
    class UpdateClass {

        @Test
        void updateClass_valid_updatesClass() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("4SE1-Neu");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.existsBySchoolIdAndName(SCHOOL, "4SE1-Neu")).thenReturn(false);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThat(schoolClassService.updateClass(CLASS_ID, dto).getName()).isEqualTo("4SE1-Neu");
        }

        @Test
        void updateClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.updateClass(CLASS_ID, new SchoolClassDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void updateClass_notFound_throwsNotFound() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId("unknown", SCHOOL)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> schoolClassService.updateClass("unknown", new SchoolClassDTO()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void updateClass_duplicateName_throwsBadRequest() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("ExistingClass");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.existsBySchoolIdAndName(SCHOOL, "ExistingClass")).thenReturn(true);
            assertThatThrownBy(() -> schoolClassService.updateClass(CLASS_ID, dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("existiert bereits");
        }

        @Test
        void updateClass_sameName_allowed() {
            SchoolClassDTO dto = new SchoolClassDTO();
            dto.setName("3SE2");
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);
            when(schoolClassRepository.save(any(SchoolClass.class))).thenAnswer(inv -> inv.getArgument(0));

            schoolClassService.updateClass(CLASS_ID, dto);
            verify(schoolClassRepository, never()).existsBySchoolIdAndName(any(), any());
        }
    }

    @Nested
    @DisplayName("deleteClass")
    class DeleteClass {

        @Test
        void deleteClass_ownClass_deletes() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn(TEACHER);

            schoolClassService.deleteClass(CLASS_ID);
            verify(schoolClassRepository).delete(testClass);
        }

        @Test
        void deleteClass_notTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(false);
            assertThatThrownBy(() -> schoolClassService.deleteClass(CLASS_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void deleteClass_otherTeacher_throwsForbidden() {
            when(userService.userHasRole("TEACHER")).thenReturn(true);
            when(userService.getCurrentSchoolId()).thenReturn(SCHOOL);
            when(schoolClassRepository.findByIdAndSchoolId(CLASS_ID, SCHOOL)).thenReturn(Optional.of(testClass));
            when(userService.getCurrentUserId()).thenReturn("other-teacher");
            assertThatThrownBy(() -> schoolClassService.deleteClass(CLASS_ID))
                    .isInstanceOf(ForbiddenException.class);
        }
    }
}
