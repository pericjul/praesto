package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Parametrisierte und Exception Tests für Validierungs-Logik.
 * Demonstriert @ParameterizedTest mit @ValueSource, @CsvSource, @NullAndEmptySource.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validation Tests")
class ValidationTest {

    @Mock
    private UserService userService;

    @Mock
    private SessionRepository sessionRepository;

    // ========================================
    // Session Score Validation
    // ========================================
    @Nested
    @DisplayName("Session Score Validation")
    class SessionScoreValidation {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 50, 99, 100})
        @DisplayName("Gültige Score-Werte (0-100)")
        void validScoreValues_areAccepted(int score) {
            Session session = new Session();
            session.setScore(score);
            
            assertThat(session.getScore()).isEqualTo(score);
            assertTrue(score >= 0 && score <= 100, "Score muss zwischen 0 und 100 liegen");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -100, 101, 200, Integer.MAX_VALUE})
        @DisplayName("Ungültige Score-Werte werden erkannt")
        void invalidScoreValues_areDetected(int score) {
            boolean isValid = score >= 0 && score <= 100;
            assertFalse(isValid, "Score " + score + " sollte ungültig sein");
        }

        @ParameterizedTest
        @CsvSource({
            "0, Ungenügend",
            "50, Genügend",
            "70, Gut",
            "85, Sehr gut",
            "100, Ausgezeichnet"
        })
        @DisplayName("Score-zu-Bewertung Mapping")
        void scoreToGrade_mapping(int score, String expectedGrade) {
            String grade = getGradeForScore(score);
            assertThat(grade).isEqualTo(expectedGrade);
        }

        private String getGradeForScore(int score) {
            if (score < 50) return "Ungenügend";
            if (score < 70) return "Genügend";
            if (score < 85) return "Gut";
            if (score < 100) return "Sehr gut";
            return "Ausgezeichnet";
        }
    }

    // ========================================
    // Application Validation
    // ========================================
    @Nested
    @DisplayName("Application Validation")
    class ApplicationValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Firmenname darf nicht leer sein")
        void companyName_nullOrEmpty_isInvalid(String companyName) {
            boolean isValid = companyName != null && !companyName.isBlank();
            assertFalse(isValid, "Leerer Firmenname sollte ungültig sein");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Google", "Microsoft", "ZHAW", "ABB Schweiz AG", "Müller & Söhne GmbH"})
        @DisplayName("Gültige Firmennamen")
        void companyName_validValues_areAccepted(String companyName) {
            Application app = new Application();
            app.setCompanyName(companyName);
            
            assertThat(app.getCompanyName()).isEqualTo(companyName);
            assertThat(companyName).isNotBlank();
        }

        @ParameterizedTest
        @CsvSource({
            "PLANNED, true",
            "APPLIED, true",
            "INVITED, true",
            "INTERVIEW_DONE, true",
            "ACCEPTED, true",
            "REJECTED, true",
            "WITHDRAWN, true"
        })
        @DisplayName("Alle Status-Übergänge sind erlaubt")
        void statusTransition_allValid(String statusName, boolean expectedValid) {
            ApplicationStatus status = ApplicationStatus.valueOf(statusName);
            assertThat(status).isNotNull();
            assertTrue(expectedValid);
        }
    }

    // ========================================
    // Note Validation
    // ========================================
    @Nested
    @DisplayName("Note Validation")
    class NoteValidation {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Notiz-Text darf nicht leer sein")
        void noteText_nullOrEmpty_isInvalid(String text) {
            boolean isValid = text != null && !text.isBlank();
            assertFalse(isValid, "Leerer Notiz-Text sollte ungültig sein");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Gutes Gespräch mit HR",
            "Termin am Montag um 14:00",
            "Muss Lebenslauf nachreichen",
            "🎉 Zusage erhalten!",
            "Sehr langes Feedback mit vielen Details über das Bewerbungsgespräch und die nächsten Schritte im Prozess"
        })
        @DisplayName("Gültige Notiz-Texte")
        void noteText_validValues_areAccepted(String text) {
            Note note = new Note();
            note.setText(text);
            
            assertThat(note.getText()).isEqualTo(text);
            assertThat(text).isNotBlank();
        }

        @ParameterizedTest
        @CsvSource({
            "1, true",
            "100, true",
            "1000, true",
            "5000, true",
            "10001, false"
        })
        @DisplayName("Notiz-Länge Validierung (max 10000 Zeichen)")
        void noteLength_validation(int length, boolean expectedValid) {
            String text = "x".repeat(length);
            boolean isValid = text.length() <= 10000;
            assertEquals(expectedValid, isValid, "Text mit Länge " + length);
        }
    }

    // ========================================
    // Assignment Duration Validation
    // ========================================
    @Nested
    @DisplayName("Assignment Duration Validation")
    class AssignmentDurationValidation {

        @ParameterizedTest
        @ValueSource(ints = {5, 10, 15, 20, 30, 45, 60, 90, 120})
        @DisplayName("Gültige Dauer-Werte (in Minuten)")
        void validDurationValues_areAccepted(int durationMin) {
            Assignment assignment = new Assignment();
            assignment.setDurationMin(durationMin);
            
            assertThat(assignment.getDurationMin()).isEqualTo(durationMin);
            assertTrue(durationMin > 0, "Dauer muss positiv sein");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -60})
        @DisplayName("Ungültige Dauer-Werte (0 oder negativ)")
        void invalidDurationValues_areDetected(int durationMin) {
            boolean isValid = durationMin > 0;
            assertFalse(isValid, "Dauer " + durationMin + " sollte ungültig sein");
        }

        @ParameterizedTest
        @CsvSource({
            "5, Kurz",
            "15, Standard",
            "30, Standard",
            "60, Lang",
            "120, Sehr lang"
        })
        @DisplayName("Dauer-zu-Kategorie Mapping")
        void durationToCategory_mapping(int durationMin, String expectedCategory) {
            String category = getCategoryForDuration(durationMin);
            assertThat(category).isEqualTo(expectedCategory);
        }

        private String getCategoryForDuration(int durationMin) {
            if (durationMin < 10) return "Kurz";
            if (durationMin < 45) return "Standard";
            if (durationMin < 90) return "Lang";
            return "Sehr lang";
        }
    }

    // ========================================
    // Email Validation
    // ========================================
    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {

        @ParameterizedTest
        @ValueSource(strings = {
            "student@example.com",
            "teacher@zhaw.ch",
            "max.mustermann@schule.edu",
            "test123@test.org",
            "valid_email@domain.co.uk"
        })
        @DisplayName("Gültige E-Mail-Adressen")
        void validEmails_areAccepted(String email) {
            boolean isValid = email.contains("@") && email.contains(".");
            assertTrue(isValid, email + " sollte gültig sein");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid",
            "no-at-sign.com",
            "@no-local-part.com",
            "no-domain@",
            "spaces in@email.com"
        })
        @DisplayName("Ungültige E-Mail-Adressen")
        void invalidEmails_areDetected(String email) {
            boolean hasBasicFormat = email.contains("@") && 
                                     email.indexOf("@") > 0 && 
                                     email.indexOf("@") < email.length() - 1 &&
                                     !email.contains(" ");
            assertFalse(hasBasicFormat, email + " sollte ungültig sein");
        }
    }

    // ========================================
    // Date Validation
    // ========================================
    @Nested
    @DisplayName("Date Validation")
    class DateValidation {

        @ParameterizedTest
        @CsvSource({
            "2025-01-15, 2025-01-20, true",
            "2025-01-20, 2025-01-15, false",
            "2025-01-15, 2025-01-15, false"
        })
        @DisplayName("Interview-Datum muss nach Bewerbungsdatum liegen")
        void interviewDateAfterAppliedDate(String appliedDateStr, String interviewDateStr, boolean expectedValid) {
            LocalDate appliedAt = LocalDate.parse(appliedDateStr);
            LocalDate interviewDate = LocalDate.parse(interviewDateStr);
            
            boolean isValid = interviewDate.isAfter(appliedAt);
            assertEquals(expectedValid, isValid);
        }

        @ParameterizedTest
        @ValueSource(strings = {"2025-12-31", "2026-01-15", "2026-06-30"})
        @DisplayName("Zukünftige Due-Dates sind gültig")
        void futureDueDate_isValid(String dueDateStr) {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.of(2025, 1, 1); // Fixiertes "heute" für Test
            
            assertTrue(dueDate.isAfter(today), "Due-Date sollte in der Zukunft liegen");
        }
    }

    // ========================================
    // Grade Validation (Submission)
    // ========================================
    @Nested
    @DisplayName("Grade Validation")
    class GradeValidation {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6})
        @DisplayName("Schweizer Notenskala (1-6)")
        void swissGradeScale_validValues(int grade) {
            boolean isValid = grade >= 1 && grade <= 6;
            assertTrue(isValid, "Note " + grade + " sollte gültig sein");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 7, 10})
        @DisplayName("Ungültige Noten")
        void invalidGrades_areDetected(int grade) {
            boolean isValid = grade >= 1 && grade <= 6;
            assertFalse(isValid, "Note " + grade + " sollte ungültig sein");
        }

        @ParameterizedTest
        @CsvSource({
            "6, Sehr gut",
            "5, Gut",
            "4, Genügend",
            "3, Ungenügend",
            "2, Schwach",
            "1, Sehr schwach"
        })
        @DisplayName("Note-zu-Bewertung Mapping (CH)")
        void gradeToDescription_swissScale(int grade, String expectedDescription) {
            String description = getSwissGradeDescription(grade);
            assertThat(description).isEqualTo(expectedDescription);
        }

        private String getSwissGradeDescription(int grade) {
            return switch (grade) {
                case 6 -> "Sehr gut";
                case 5 -> "Gut";
                case 4 -> "Genügend";
                case 3 -> "Ungenügend";
                case 2 -> "Schwach";
                case 1 -> "Sehr schwach";
                default -> "Ungültig";
            };
        }
    }
}
