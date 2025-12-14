package ch.zhaw.praesto.service;

import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        testAssignment = Assignment.builder()
                .id("assign-123")
                .title("Bewerbungstraining")
                .type(AssignmentType.AI_INTERVIEW)
                .status(AssignmentStatus.ASSIGNED)
                .classId("class-123")
                .createdByTeacherId("teacher-123")
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("createAssignment")
    class CreateAssignment {

        @Test
        @DisplayName("Assignment erfolgreich erstellen")
        void createAssignment_valid_returnsAssignment() {
            Assignment newAssignment = Assignment.builder()
                    .title("Neues Assignment")
                    .type(AssignmentType.RESEARCH)
                    .classId("class-123")
                    .build();

            when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

            Assignment result = assignmentService.createAssignment(newAssignment, "teacher-123");

            assertThat(result).isNotNull();
            verify(assignmentRepository).save(any(Assignment.class));
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("Status ändern")
        void updateStatus_valid_updatesStatus() {
            when(assignmentRepository.findById("assign-123")).thenReturn(Optional.of(testAssignment));
            when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

            Assignment result = assignmentService.updateStatus("assign-123", AssignmentStatus.CLOSED);

            assertThat(result).isNotNull();
            verify(assignmentRepository).save(any(Assignment.class));
        }

        @Test
        @DisplayName("Assignment nicht gefunden")
        void updateStatus_notFound_throwsException() {
            when(assignmentRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignmentService.updateStatus("unknown", AssignmentStatus.CLOSED))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getAssignmentsForClass")
    class GetAssignmentsForClass {

        @Test
        @DisplayName("Assignments für Klasse abrufen")
        void getAssignmentsForClass_returnsList() {
            when(assignmentRepository.findByClassId("class-123"))
                    .thenReturn(List.of(testAssignment));

            List<Assignment> result = assignmentService.getAssignmentsForClass("class-123");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Keine Assignments für Klasse")
        void getAssignmentsForClass_noAssignments_returnsEmptyList() {
            when(assignmentRepository.findByClassId("empty-class")).thenReturn(List.of());

            List<Assignment> result = assignmentService.getAssignmentsForClass("empty-class");

            assertThat(result).isEmpty();
        }
    }
}
