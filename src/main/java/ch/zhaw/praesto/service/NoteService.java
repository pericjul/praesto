package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.Note;
import ch.zhaw.praesto.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String NOTIZ_NICHT_GEFUNDEN = "Notiz nicht gefunden";
    private static final String STUDENT = "STUDENT";
    private final NoteRepository noteRepository;
    private final UserService userService;
    private final BadgeService badgeService;  // NEU: BadgeService injiziert

    /**
     * Neue Notiz erstellen.
     */
    public Note createNote(String companyName, String position, String text) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Notizen erstellen");
        }

        String studentId = userService.getUserId();
        Instant now = Instant.now();

        Note note = Note.builder()
                .studentId(studentId)
                .companyName(companyName)
                .position(position)
                .text(text)
                .createdAt(now)
                .lastUpdated(now)
                .build();

        Note saved = noteRepository.save(note);

        // Badge-Check NACH dem Speichern
        badgeService.checkAndAwardBadges(studentId);

        return saved;
    }

    /**
     * Alle Notizen eines Schuelers (neueste zuerst).
     */
    public List<Note> getMyNotes() {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Notizen sehen");
        }

        String studentId = userService.getUserId();
        return noteRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Eine einzelne Notiz holen.
     */
    public Note getNoteById(String noteId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Notizen sehen");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException(NOTIZ_NICHT_GEFUNDEN));

        checkOwnership(note);
        return note;
    }

    /**
     * Notiz bearbeiten.
     */
    public Note updateNote(String noteId, String companyName, String position, String text) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Notizen bearbeiten");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException(NOTIZ_NICHT_GEFUNDEN));

        checkOwnership(note);

        note.setCompanyName(companyName);
        note.setPosition(position);
        note.setText(text);
        note.setLastUpdated(Instant.now());

        return noteRepository.save(note);
    }

    /**
     * Notiz loeschen.
     */
    public void deleteNote(String noteId) {
        if (!userService.userHasRole(STUDENT)) {
            throw new ForbiddenException("Nur Schueler duerfen Notizen loeschen");
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException(NOTIZ_NICHT_GEFUNDEN));

        checkOwnership(note);

        noteRepository.delete(note);
    }

    /**
     * Prueft ob der aktuelle User der Besitzer der Notiz ist.
     */
    private void checkOwnership(Note note) {
        String studentId = userService.getUserId();
        if (!studentId.equals(note.getStudentId())) {
            throw new ForbiddenException("Keine Berechtigung fuer diese Notiz");
        }
    }
}