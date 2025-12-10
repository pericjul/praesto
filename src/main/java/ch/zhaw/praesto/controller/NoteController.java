package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.Note;
import ch.zhaw.praesto.model.NoteDTO;
import ch.zhaw.praesto.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * POST /api/notes - Neue Notiz erstellen
     */
    @PostMapping("")
    public ResponseEntity<Note> createNote(@RequestBody NoteDTO dto) {
        Note note = noteService.createNote(
                dto.getCompanyName(),
                dto.getPosition(),
                dto.getText()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    /**
     * GET /api/notes - Alle eigenen Notizen
     */
    @GetMapping("")
    public ResponseEntity<List<Note>> getMyNotes() {
        List<Note> notes = noteService.getMyNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * GET /api/notes/{id} - Eine Notiz
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    /**
     * PUT /api/notes/{id} - Notiz bearbeiten
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable String id,
            @RequestBody NoteDTO dto) {
        Note note = noteService.updateNote(
                id,
                dto.getCompanyName(),
                dto.getPosition(),
                dto.getText()
        );
        return ResponseEntity.ok(note);
    }

    /**
     * DELETE /api/notes/{id} - Notiz loeschen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}