package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.Document;
import ch.zhaw.praesto.model.Submission;
import ch.zhaw.praesto.model.User;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.DocumentRepository;
import ch.zhaw.praesto.repository.SubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAccessServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private FileAccessService fileAccessService;

    private User user(String id, UserRole role, String schoolId) {
        return User.builder().id(id).role(role).schoolId(schoolId).build();
    }

    @Test
    void superAdmin_alwaysAllowed() {
        when(userService.getCurrentUser()).thenReturn(user("sa", UserRole.SUPER_ADMIN, null));
        assertThatCode(() -> fileAccessService.assertCanAccess("any.pdf")).doesNotThrowAnyException();
    }

    @Test
    void owner_allowed() {
        when(userService.getCurrentUser()).thenReturn(user("u1", UserRole.STUDENT, "s1"));
        when(documentRepository.findFirstByFileUrl("f"))
                .thenReturn(Optional.of(Document.builder().studentId("u1").schoolId("s1").build()));
        assertThatCode(() -> fileAccessService.assertCanAccess("f")).doesNotThrowAnyException();
    }

    @Test
    void otherStudent_denied() {
        when(userService.getCurrentUser()).thenReturn(user("u2", UserRole.STUDENT, "s1"));
        when(documentRepository.findFirstByFileUrl("f"))
                .thenReturn(Optional.of(Document.builder().studentId("u1").schoolId("s1").build()));
        assertThatThrownBy(() -> fileAccessService.assertCanAccess("f"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void teacherSameSchool_allowed() {
        when(userService.getCurrentUser()).thenReturn(user("t1", UserRole.TEACHER, "s1"));
        when(documentRepository.findFirstByFileUrl("f")).thenReturn(Optional.empty());
        when(submissionRepository.findFirstByFileUrl("f"))
                .thenReturn(Optional.of(Submission.builder().studentId("u1").schoolId("s1").build()));
        assertThatCode(() -> fileAccessService.assertCanAccess("f")).doesNotThrowAnyException();
    }

    @Test
    void orphanFile_denied() {
        when(userService.getCurrentUser()).thenReturn(user("u1", UserRole.STUDENT, "s1"));
        when(documentRepository.findFirstByFileUrl("f")).thenReturn(Optional.empty());
        when(submissionRepository.findFirstByFileUrl("f")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> fileAccessService.assertCanAccess("f"))
                .isInstanceOf(ForbiddenException.class);
    }
}
