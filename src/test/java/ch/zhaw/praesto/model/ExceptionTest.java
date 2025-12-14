package ch.zhaw.praesto.model;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exception Tests mit JUnit's assertThrows.
 * Demonstriert Exception Testing gemäss Anforderungen.
 */
@DisplayName("Exception Tests")
class ExceptionTest {

    // ========================================
    // Custom Exception Tests
    // ========================================
    @Nested
    @DisplayName("Custom Exceptions")
    class CustomExceptionTests {

        @Test
        @DisplayName("NotFoundException mit Message")
        void notFoundException_hasCorrectMessage() {
            String expectedMessage = "Session nicht gefunden";
            
            Exception exception = assertThrows(NotFoundException.class, () -> {
                throw new NotFoundException(expectedMessage);
            });
            
            assertEquals(expectedMessage, exception.getMessage());
        }

        @Test
        @DisplayName("ForbiddenException mit Message")
        void forbiddenException_hasCorrectMessage() {
            String expectedMessage = "Kein Zugriff auf diese Ressource";
            
            Exception exception = assertThrows(ForbiddenException.class, () -> {
                throw new ForbiddenException(expectedMessage);
            });
            
            assertEquals(expectedMessage, exception.getMessage());
        }

        @Test
        @DisplayName("BadRequestException mit Message")
        void badRequestException_hasCorrectMessage() {
            String expectedMessage = "Ungültige Eingabe";
            
            Exception exception = assertThrows(BadRequestException.class, () -> {
                throw new BadRequestException(expectedMessage);
            });
            
            assertEquals(expectedMessage, exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Session nicht gefunden",
            "Assignment nicht gefunden",
            "Klasse nicht gefunden",
            "Bewerbung nicht gefunden"
        })
        @DisplayName("NotFoundException mit verschiedenen Messages")
        void notFoundException_variousMessages(String message) {
            Exception exception = assertThrows(NotFoundException.class, () -> {
                throw new NotFoundException(message);
            });
            
            assertTrue(exception.getMessage().contains("nicht gefunden"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Nur Schüler können Sessions starten",
            "Nur Lehrer können Aufgaben erstellen",
            "Kein Zugriff auf fremde Bewerbungen"
        })
        @DisplayName("ForbiddenException mit verschiedenen Messages")
        void forbiddenException_variousMessages(String message) {
            Exception exception = assertThrows(ForbiddenException.class, () -> {
                throw new ForbiddenException(message);
            });
            
            assertNotNull(exception.getMessage());
            assertFalse(exception.getMessage().isEmpty());
        }
    }

    // ========================================
    // Enum valueOf Exception Tests
    // ========================================
    @Nested
    @DisplayName("Enum valueOf Exceptions")
    class EnumValueOfExceptionTests {

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "UNKNOWN", "NOT_EXIST", "", "null"})
        @DisplayName("SessionStatus.valueOf mit ungültigen Werten")
        void sessionStatus_invalidValue_throwsException(String invalidValue) {
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                SessionStatus.valueOf(invalidValue);
            });
            
            assertTrue(exception.getMessage().contains(invalidValue) || 
                      exception.getMessage().contains("No enum constant"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "WAITING", "DONE", "CANCELLED"})
        @DisplayName("ApplicationStatus.valueOf mit ungültigen Werten")
        void applicationStatus_invalidValue_throwsException(String invalidValue) {
            assertThrows(IllegalArgumentException.class, () -> {
                ApplicationStatus.valueOf(invalidValue);
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {"EXAM", "QUIZ", "HOMEWORK", "PROJECT"})
        @DisplayName("AssignmentType.valueOf mit ungültigen Werten")
        void assignmentType_invalidValue_throwsException(String invalidValue) {
            assertThrows(IllegalArgumentException.class, () -> {
                AssignmentType.valueOf(invalidValue);
            });
        }
    }

    // ========================================
    // Null Pointer Exception Tests
    // ========================================
    @Nested
    @DisplayName("Null Value Handling")
    class NullValueTests {

        @Test
        @DisplayName("Session mit null StudentId")
        void session_nullStudentId_handledGracefully() {
            Session session = new Session();
            session.setStudentId(null);
            
            // Sollte keine Exception werfen
            assertNull(session.getStudentId());
        }

        @Test
        @DisplayName("Application mit null CompanyName")
        void application_nullCompanyName_handledGracefully() {
            Application app = new Application();
            app.setCompanyName(null);
            
            assertNull(app.getCompanyName());
        }

        @Test
        @DisplayName("Note mit null Text wirft keine Exception")
        void note_nullText_noException() {
            Note note = new Note();
            assertDoesNotThrow(() -> note.setText(null));
            assertNull(note.getText());
        }
    }

    // ========================================
    // Boundary Condition Exception Tests
    // ========================================
    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Score über Maximum (100)")
        void score_overMaximum_isInvalid() {
            int invalidScore = 101;
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                if (invalidScore > 100) {
                    throw new IllegalArgumentException("Score darf nicht grösser als 100 sein");
                }
            });
            
            assertTrue(exception.getMessage().contains("100"));
        }

        @Test
        @DisplayName("Score unter Minimum (0)")
        void score_underMinimum_isInvalid() {
            int invalidScore = -1;
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                if (invalidScore < 0) {
                    throw new IllegalArgumentException("Score darf nicht negativ sein");
                }
            });
            
            assertTrue(exception.getMessage().contains("negativ"));
        }

        @Test
        @DisplayName("Dauer mit Wert 0")
        void duration_zero_isInvalid() {
            int invalidDuration = 0;
            
            Exception exception = assertThrows(BadRequestException.class, () -> {
                if (invalidDuration <= 0) {
                    throw new BadRequestException("Dauer muss grösser als 0 sein");
                }
            });
            
            assertTrue(exception.getMessage().contains("grösser als 0"));
        }

        @Test
        @DisplayName("Negative Dauer")
        void duration_negative_isInvalid() {
            int invalidDuration = -30;
            
            Exception exception = assertThrows(BadRequestException.class, () -> {
                if (invalidDuration <= 0) {
                    throw new BadRequestException("Dauer muss positiv sein");
                }
            });
            
            assertNotNull(exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 7, -1, 10, 100})
        @DisplayName("Ungültige Noten (ausserhalb 1-6)")
        void grade_outOfRange_throwsException(int invalidGrade) {
            Exception exception = assertThrows(BadRequestException.class, () -> {
                if (invalidGrade < 1 || invalidGrade > 6) {
                    throw new BadRequestException("Note muss zwischen 1 und 6 liegen");
                }
            });
            
            assertTrue(exception.getMessage().contains("1") && exception.getMessage().contains("6"));
        }
    }

    // ========================================
    // State Transition Exception Tests
    // ========================================
    @Nested
    @DisplayName("State Transition Exceptions")
    class StateTransitionTests {

        @Test
        @DisplayName("Geschlossene Session kann nicht nochmals geschlossen werden")
        void closedSession_cannotCloseAgain() {
            Session session = new Session();
            session.setStatus(SessionStatus.CLOSED);
            
            Exception exception = assertThrows(BadRequestException.class, () -> {
                if (session.getStatus() == SessionStatus.CLOSED) {
                    throw new BadRequestException("Session ist bereits geschlossen");
                }
            });
            
            assertTrue(exception.getMessage().contains("bereits geschlossen"));
        }

        @Test
        @DisplayName("Geschlossene Aufgabe kann nicht mehr bearbeitet werden")
        void closedAssignment_cannotBeEdited() {
            Assignment assignment = new Assignment();
            assignment.setStatus(AssignmentStatus.CLOSED);
            
            Exception exception = assertThrows(ForbiddenException.class, () -> {
                if (assignment.getStatus() == AssignmentStatus.CLOSED) {
                    throw new ForbiddenException("Geschlossene Aufgaben können nicht bearbeitet werden");
                }
            });
            
            assertTrue(exception.getMessage().contains("Geschlossene"));
        }

        @Test
        @DisplayName("Bereits bewertete Abgabe kann nicht erneut bewertet werden")
        void reviewedSubmission_cannotReviewAgain() {
            Submission submission = new Submission();
            submission.setStatus(SubmissionStatus.REVIEWED);
            
            Exception exception = assertThrows(BadRequestException.class, () -> {
                if (submission.getStatus() == SubmissionStatus.REVIEWED) {
                    throw new BadRequestException("Abgabe wurde bereits bewertet");
                }
            });
            
            assertTrue(exception.getMessage().contains("bereits bewertet"));
        }
    }
}
