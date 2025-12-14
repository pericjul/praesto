package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.NoteRepository;
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
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserService userService;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private NoteService noteService;

    private Note testNote;

    @BeforeEach
    void setUp() {
        testNote = Note.builder()
                .id("note-123")
                .studentId("student-123")
                .companyName("Google")
                .position("Software Engineer")
                .text("Wichtige Infos")
                .createdAt(Instant.now())
                .lastUpdated(Instant.now())
                .build();
    }

    // ========================================
    // createNote
    // ========================================
    @Nested
    @DisplayName("createNote")
    class CreateNote {

        @Test
        @DisplayName("Notiz erfolgreich erstellen")
        void createNote_asStudent_createsNote() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.save(any(Note.class))).thenReturn(testNote);

            Note result = noteService.createNote("Google", "Dev", "Text");

            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("Google");
            verify(noteRepository).save(any(Note.class));
            verify(badgeService).checkAndAwardBadges("student-123");
        }

        @Test
        @DisplayName("Nicht-Student darf keine Notiz erstellen")
        void createNote_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> noteService.createNote("Google", "Dev", "Text"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Schueler");
        }
    }

    // ========================================
    // getMyNotes
    // ========================================
    @Nested
    @DisplayName("getMyNotes")
    class GetMyNotes {

        @Test
        @DisplayName("Eigene Notizen abrufen")
        void getMyNotes_asStudent_returnsList() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.findByStudentIdOrderByCreatedAtDesc("student-123"))
                    .thenReturn(List.of(testNote));

            List<Note> result = noteService.getMyNotes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCompanyName()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Leere Liste wenn keine Notizen")
        void getMyNotes_noNotes_returnsEmptyList() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.findByStudentIdOrderByCreatedAtDesc("student-123"))
                    .thenReturn(List.of());

            List<Note> result = noteService.getMyNotes();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Nicht-Student hat keinen Zugriff")
        void getMyNotes_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> noteService.getMyNotes())
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getNoteById
    // ========================================
    @Nested
    @DisplayName("getNoteById")
    class GetNoteById {

        @Test
        @DisplayName("Eigene Notiz abrufen")
        void getNoteById_ownNote_returnsNote() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.findById("note-123")).thenReturn(Optional.of(testNote));

            Note result = noteService.getNoteById("note-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("note-123");
        }

        @Test
        @DisplayName("Notiz nicht gefunden")
        void getNoteById_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(noteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.getNoteById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Notiz - kein Zugriff")
        void getNoteById_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(noteRepository.findById("note-123")).thenReturn(Optional.of(testNote));

            assertThatThrownBy(() -> noteService.getNoteById("note-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // updateNote
    // ========================================
    @Nested
    @DisplayName("updateNote")
    class UpdateNote {

        @Test
        @DisplayName("Notiz erfolgreich aktualisieren")
        void updateNote_ownNote_updatesNote() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.findById("note-123")).thenReturn(Optional.of(testNote));
            when(noteRepository.save(any(Note.class))).thenReturn(testNote);

            Note result = noteService.updateNote("note-123", "Microsoft", "PM", "Neuer Text");

            assertThat(result).isNotNull();
            verify(noteRepository).save(any(Note.class));
        }

        @Test
        @DisplayName("Nicht-Student darf nicht bearbeiten")
        void updateNote_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> noteService.updateNote("note-123", "X", "Y", "Z"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Notiz nicht gefunden")
        void updateNote_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(noteRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.updateNote("unknown", "X", "Y", "Z"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ========================================
    // deleteNote
    // ========================================
    @Nested
    @DisplayName("deleteNote")
    class DeleteNote {

        @Test
        @DisplayName("Notiz erfolgreich löschen")
        void deleteNote_ownNote_deletesNote() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(noteRepository.findById("note-123")).thenReturn(Optional.of(testNote));

            noteService.deleteNote("note-123");

            verify(noteRepository).delete(testNote);
        }

        @Test
        @DisplayName("Nicht-Student darf nicht löschen")
        void deleteNote_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> noteService.deleteNote("note-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Fremde Notiz - kann nicht löschen")
        void deleteNote_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(noteRepository.findById("note-123")).thenReturn(Optional.of(testNote));

            assertThatThrownBy(() -> noteService.deleteNote("note-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }
}
