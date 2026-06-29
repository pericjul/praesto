package ch.zhaw.praesto.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Wandelt Exceptions in saubere, für Schüler:innen/Lehrer verständliche Meldungen um.
 * Wichtig: KEINE Stacktraces oder technischen Details an den Client – nur ein
 * freundlicher Satz. So sieht niemand einen nackten 500.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbidden(ForbiddenException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    /** Datei zu gross (Multipart-Limit überschritten). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUpload(MaxUploadSizeExceededException e) {
        return new ResponseEntity<>(
                "Die Datei ist zu gross (max. 20 MB). Bitte wähle eine kleinere Datei.",
                HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /** Ungültige/unlesbare Anfrage-Daten (kaputtes JSON, falscher Typ im Body). */
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class })
    public ResponseEntity<String> handleBadInput(Exception e) {
        return new ResponseEntity<>("Die Eingabe ist ungültig. Bitte prüfe die Felder und versuche es erneut.",
                HttpStatus.BAD_REQUEST);
    }

    /** Auffangnetz: alles Unerwartete wird geloggt, aber sauber gemeldet (kein nackter 500). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception e) {
        log.error("Unerwarteter Fehler", e);
        return new ResponseEntity<>(
                "Es ist ein unerwarteter Fehler aufgetreten. Bitte versuche es später erneut.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}