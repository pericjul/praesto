package ch.zhaw.praesto.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ========================================
    // handleBadRequest
    // ========================================
    @Nested
    @DisplayName("handleBadRequest")
    class HandleBadRequest {

        @Test
        @DisplayName("BadRequestException - returns 400")
        void handleBadRequest_returns400() {
            BadRequestException exception = new BadRequestException("Ungültige Eingabe");

            ResponseEntity<?> response = handler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo("Ungültige Eingabe");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Firmenname ist erforderlich",
            "Status ungültig",
            "Session bereits geschlossen",
            "Dauer muss positiv sein"
        })
        @DisplayName("BadRequestException mit verschiedenen Nachrichten")
        void handleBadRequest_variousMessages(String message) {
            BadRequestException exception = new BadRequestException(message);

            ResponseEntity<?> response = handler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo(message);
        }

        @Test
        @DisplayName("BadRequestException mit leerer Nachricht")
        void handleBadRequest_emptyMessage() {
            BadRequestException exception = new BadRequestException("");

            ResponseEntity<?> response = handler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo("");
        }

        @Test
        @DisplayName("BadRequestException mit null Nachricht")
        void handleBadRequest_nullMessage() {
            BadRequestException exception = new BadRequestException(null);

            ResponseEntity<?> response = handler.handleBadRequest(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNull();
        }
    }

    // ========================================
    // handleForbidden
    // ========================================
    @Nested
    @DisplayName("handleForbidden")
    class HandleForbidden {

        @Test
        @DisplayName("ForbiddenException - returns 403")
        void handleForbidden_returns403() {
            ForbiddenException exception = new ForbiddenException("Kein Zugriff");

            ResponseEntity<?> response = handler.handleForbidden(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isEqualTo("Kein Zugriff");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Nur Schüler dürfen Sessions starten",
            "Nur Lehrer dürfen Klassen erstellen",
            "Kein Zugriff auf fremde Bewerbungen",
            "Nur eigene Notizen bearbeitbar"
        })
        @DisplayName("ForbiddenException mit verschiedenen Nachrichten")
        void handleForbidden_variousMessages(String message) {
            ForbiddenException exception = new ForbiddenException(message);

            ResponseEntity<?> response = handler.handleForbidden(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isEqualTo(message);
        }

        @Test
        @DisplayName("ForbiddenException mit leerer Nachricht")
        void handleForbidden_emptyMessage() {
            ForbiddenException exception = new ForbiddenException("");

            ResponseEntity<?> response = handler.handleForbidden(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isEqualTo("");
        }
    }

    // ========================================
    // handleNotFound
    // ========================================
    @Nested
    @DisplayName("handleNotFound")
    class HandleNotFound {

        @Test
        @DisplayName("NotFoundException - returns 404")
        void handleNotFound_returns404() {
            NotFoundException exception = new NotFoundException("Session nicht gefunden");

            ResponseEntity<?> response = handler.handleNotFound(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("Session nicht gefunden");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Session nicht gefunden",
            "Klasse nicht gefunden",
            "Assignment nicht gefunden",
            "Bewerbung nicht gefunden",
            "Notiz nicht gefunden",
            "Badge nicht gefunden"
        })
        @DisplayName("NotFoundException mit verschiedenen Nachrichten")
        void handleNotFound_variousMessages(String message) {
            NotFoundException exception = new NotFoundException(message);

            ResponseEntity<?> response = handler.handleNotFound(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo(message);
        }

        @Test
        @DisplayName("NotFoundException mit leerer Nachricht")
        void handleNotFound_emptyMessage() {
            NotFoundException exception = new NotFoundException("");

            ResponseEntity<?> response = handler.handleNotFound(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isEqualTo("");
        }
    }

    // ========================================
    // Exception Konstruktor Tests
    // ========================================
    @Nested
    @DisplayName("Exception Konstruktoren")
    class ExceptionConstructors {

        @Test
        @DisplayName("BadRequestException Konstruktor")
        void badRequestException_constructor() {
            BadRequestException exception = new BadRequestException("Test");
            
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Test");
        }

        @Test
        @DisplayName("ForbiddenException Konstruktor")
        void forbiddenException_constructor() {
            ForbiddenException exception = new ForbiddenException("Test");
            
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Test");
        }

        @Test
        @DisplayName("NotFoundException Konstruktor")
        void notFoundException_constructor() {
            NotFoundException exception = new NotFoundException("Test");
            
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Test");
        }
    }
}
