package ch.zhaw.praesto.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    /** Nur zur Diagnose (Pilot) den Fehlertyp mitgeben. In Produktion: false. */
    @Value("${praesto.expose-errors:false}")
    private boolean exposeErrors;

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

    /**
     * Auffangnetz: alles Unerwartete wird mit vollem Stacktrace geloggt. Standardmässig
     * bekommt der/die Nutzer:in nur eine generische Meldung (kein Information-Disclosure).
     * Nur wenn {@code praesto.expose-errors=true} gesetzt ist (Diagnose/Pilot), wird der
     * Fehlertyp mitgegeben. In Produktion auf {@code false} lassen.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception e) {
        log.error("Unerwarteter Fehler", e);
        String body = "Es ist ein unerwarteter Fehler aufgetreten. Bitte versuche es später erneut.";
        if (exposeErrors) {
            body += " (" + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? ": " + e.getMessage() : "") + ")";
        }
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}