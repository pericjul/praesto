package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.exception.NotFoundException;
import ch.zhaw.praesto.model.*;
import ch.zhaw.praesto.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserService userService;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private ApplicationService applicationService;

    private Application testApplication;

    @BeforeEach
    void setUp() {
        testApplication = Application.builder()
                .id("app-123")
                .studentId("student-123")
                .companyName("Google")
                .position("Software Engineer")
                .status(ApplicationStatus.APPLIED)
                .createdAt(Instant.now())
                .build();
    }

    // ========================================
    // createApplication
    // ========================================
    @Nested
    @DisplayName("createApplication")
    class CreateApplication {

        @Test
        @DisplayName("Bewerbung erfolgreich erstellen")
        void createApplication_valid_createsApplication() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setPosition("Dev");
            dto.setStatus("APPLIED");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

            Application result = applicationService.createApplication(dto);

            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("Google");
            verify(applicationRepository).save(any(Application.class));
            verify(badgeService).checkAndAwardBadges("student-123");
        }

        @Test
        @DisplayName("Nicht-Student darf keine Bewerbung erstellen")
        void createApplication_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.createApplication(new ApplicationDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Ohne Firmenname nicht erlaubt")
        void createApplication_noCompanyName_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setStatus("APPLIED");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Firmenname");
        }

        @Test
        @DisplayName("Ohne Status nicht erlaubt")
        void createApplication_noStatus_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Status");
        }
    }

    // ========================================
    // getMyApplications
    // ========================================
    @Nested
    @DisplayName("getMyApplications")
    class GetMyApplications {

        @Test
        @DisplayName("Eigene Bewerbungen abrufen")
        void getMyApplications_asStudent_returnsList() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findByStudentIdOrderByCreatedAtDesc("student-123"))
                    .thenReturn(List.of(testApplication));

            List<Application> result = applicationService.getMyApplications();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCompanyName()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Nicht-Student hat keinen Zugriff")
        void getMyApplications_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.getMyApplications())
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getApplicationById
    // ========================================
    @Nested
    @DisplayName("getApplicationById")
    class GetApplicationById {

        @Test
        @DisplayName("Eigene Bewerbung abrufen")
        void getApplicationById_ownApplication_returnsApplication() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            Application result = applicationService.getApplicationById("app-123");

            assertThat(result).isNotNull();
            assertThat(result.getCompanyName()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Bewerbung nicht gefunden")
        void getApplicationById_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(applicationRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.getApplicationById("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Bewerbung - kein Zugriff")
        void getApplicationById_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.getApplicationById("app-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // updateStatus
    // ========================================
    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @ParameterizedTest
        @EnumSource(ApplicationStatus.class)
        @DisplayName("Alle Status-Werte sind gültig")
        void updateStatus_allStatuses_updatesStatus(ApplicationStatus status) {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

            Application result = applicationService.updateStatus("app-123", status.name());

            assertThat(result).isNotNull();
            verify(applicationRepository).save(any(Application.class));
            verify(badgeService).checkAndAwardBadges("student-123");
        }

        @Test
        @DisplayName("Ungültiger Status")
        void updateStatus_invalidStatus_throwsBadRequest() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.updateStatus("app-123", "INVALID_STATUS"))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Nicht-Student darf Status nicht ändern")
        void updateStatus_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.updateStatus("app-123", "INVITED"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // updateApplication
    // ========================================
    @Nested
    @DisplayName("updateApplication")
    class UpdateApplication {

        @Test
        @DisplayName("Bewerbung erfolgreich aktualisieren")
        void updateApplication_valid_updatesApplication() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Microsoft");
            dto.setPosition("PM");
            dto.setStatus("INVITED");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

            Application result = applicationService.updateApplication("app-123", dto);

            assertThat(result).isNotNull();
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("Nicht-Student darf nicht bearbeiten")
        void updateApplication_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.updateApplication("app-123", new ApplicationDTO()))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Fremde Bewerbung bearbeiten nicht erlaubt")
        void updateApplication_otherStudent_throwsForbidden() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("X");
            dto.setStatus("APPLIED");
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.updateApplication("app-123", dto))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // deleteApplication
    // ========================================
    @Nested
    @DisplayName("deleteApplication")
    class DeleteApplication {

        @Test
        @DisplayName("Bewerbung erfolgreich löschen")
        void deleteApplication_ownApplication_deletesApplication() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            applicationService.deleteApplication("app-123");

            verify(applicationRepository).delete(testApplication);
        }

        @Test
        @DisplayName("Nicht-Student darf nicht löschen")
        void deleteApplication_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.deleteApplication("app-123"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Fremde Bewerbung löschen nicht erlaubt")
        void deleteApplication_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.deleteApplication("app-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // getMyStats
    // ========================================
    @Nested
    @DisplayName("getMyStats")
    class GetMyStats {

        @Test
        @DisplayName("Statistiken berechnen")
        void getMyStats_returnsStats() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.PLANNED)).thenReturn(2L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.APPLIED)).thenReturn(3L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INVITED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INTERVIEW_DONE)).thenReturn(0L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.ACCEPTED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.REJECTED)).thenReturn(0L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.WITHDRAWN)).thenReturn(0L);

            ApplicationStats result = applicationService.getMyStats();

            assertThat(result.getTotal()).isEqualTo(7);
            assertThat(result.getPlanned()).isEqualTo(2);
            assertThat(result.getApplied()).isEqualTo(3);
            assertThat(result.getAccepted()).isEqualTo(1);
        }

        @Test
        @DisplayName("Nicht-Student hat keinen Zugriff")
        void getMyStats_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.getMyStats())
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Keine Bewerbungen - alle 0")
        void getMyStats_noApplications_returnsZeros() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.countByStudentIdAndStatus(eq("student-123"), any())).thenReturn(0L);

            ApplicationStats result = applicationService.getMyStats();

            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    // ========================================
    // createApplication - Edge Cases
    // ========================================
    @Nested
    @DisplayName("createApplication - Edge Cases")
    class CreateApplicationEdgeCases {

        @Test
        @DisplayName("Mit Bewerbungsdatum erstellen")
        void createApplication_withAppliedAt_setsDate() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("APPLIED");
            dto.setAppliedAt("2024-12-01");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.createApplication(dto);

            assertThat(result.getAppliedAt()).isNotNull();
            assertThat(result.getAppliedAt().toString()).isEqualTo("2024-12-01");
        }

        @Test
        @DisplayName("Mit Interview-Datum erstellen")
        void createApplication_withInterviewDate_setsDate() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("INVITED");
            dto.setInterviewDate("2024-12-15");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.createApplication(dto);

            assertThat(result.getInterviewDate()).isNotNull();
            assertThat(result.getInterviewDate().toString()).isEqualTo("2024-12-15");
        }

        @Test
        @DisplayName("Ungültiges Datum wirft Exception")
        void createApplication_invalidDate_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("APPLIED");
            dto.setAppliedAt("not-a-date");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ungueltiges Datum");
        }

        @Test
        @DisplayName("Ungültiges Interview-Datum wirft Exception")
        void createApplication_invalidInterviewDate_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("INVITED");
            dto.setInterviewDate("32-13-2024");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ungueltiges Datum");
        }

        @Test
        @DisplayName("Status wird zu Uppercase konvertiert")
        void createApplication_lowercaseStatus_convertsToUppercase() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("applied");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.createApplication(dto);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        }

        @Test
        @DisplayName("Leerer Firmenname (nur Whitespace) nicht erlaubt")
        void createApplication_blankCompanyName_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("   ");
            dto.setStatus("APPLIED");

            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Firmenname");
        }

        @Test
        @DisplayName("Leerer Status (nur Whitespace) nicht erlaubt")
        void createApplication_blankStatus_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("   ");

            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Status");
        }

        @Test
        @DisplayName("Mit Position und Notizen erstellen")
        void createApplication_withPositionAndNotes_setsFields() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("PLANNED");
            dto.setPosition("Software Engineer");
            dto.setNotes("Interessante Stelle");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.createApplication(dto);

            assertThat(result.getPosition()).isEqualTo("Software Engineer");
            assertThat(result.getNotes()).isEqualTo("Interessante Stelle");
        }

        @Test
        @DisplayName("Leeres Datum wird als null gespeichert")
        void createApplication_emptyDate_setsNull() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("PLANNED");
            dto.setAppliedAt("");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.createApplication(dto);

            assertThat(result.getAppliedAt()).isNull();
        }

        @Test
        @DisplayName("Ungültiger Status wirft Exception")
        void createApplication_invalidStatus_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("UNKNOWN_STATUS");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");

            assertThatThrownBy(() -> applicationService.createApplication(dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ungueltiger Status");
        }
    }

    // ========================================
    // getMyApplications - Edge Cases
    // ========================================
    @Nested
    @DisplayName("getMyApplications - Edge Cases")
    class GetMyApplicationsEdgeCases {

        @Test
        @DisplayName("Keine Bewerbungen - leere Liste")
        void getMyApplications_noApplications_returnsEmptyList() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findByStudentIdOrderByCreatedAtDesc("student-123"))
                    .thenReturn(List.of());

            List<Application> result = applicationService.getMyApplications();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Mehrere Bewerbungen werden zurückgegeben")
        void getMyApplications_multipleApplications_returnsAll() {
            Application app2 = Application.builder()
                    .id("app-456")
                    .studentId("student-123")
                    .companyName("Microsoft")
                    .build();

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findByStudentIdOrderByCreatedAtDesc("student-123"))
                    .thenReturn(List.of(testApplication, app2));

            List<Application> result = applicationService.getMyApplications();

            assertThat(result).hasSize(2);
        }
    }

    // ========================================
    // getApplicationById - Edge Cases
    // ========================================
    @Nested
    @DisplayName("getApplicationById - Edge Cases")
    class GetApplicationByIdEdgeCases {

        @Test
        @DisplayName("Nicht-Student hat keinen Zugriff")
        void getApplicationById_notStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(false);

            assertThatThrownBy(() -> applicationService.getApplicationById("app-123"))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    // ========================================
    // updateApplication - Edge Cases
    // ========================================
    @Nested
    @DisplayName("updateApplication - Edge Cases")
    class UpdateApplicationEdgeCases {

        @Test
        @DisplayName("Bewerbung nicht gefunden")
        void updateApplication_notFound_throwsNotFound() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("APPLIED");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(applicationRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateApplication("unknown", dto))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Update mit allen Feldern")
        void updateApplication_allFields_updatesAllFields() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Microsoft");
            dto.setPosition("PM");
            dto.setStatus("INVITED");
            dto.setAppliedAt("2024-11-01");
            dto.setInterviewDate("2024-12-01");
            dto.setNotes("Zweites Gespräch");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.updateApplication("app-123", dto);

            assertThat(result.getCompanyName()).isEqualTo("Microsoft");
            assertThat(result.getPosition()).isEqualTo("PM");
            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.INVITED);
            assertThat(result.getAppliedAt()).isNotNull();
            assertThat(result.getInterviewDate()).isNotNull();
            assertThat(result.getNotes()).isEqualTo("Zweites Gespräch");
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Update ohne Firmenname nicht erlaubt")
        void updateApplication_noCompanyName_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setStatus("APPLIED");

            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.updateApplication("app-123", dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Firmenname");
        }

        @Test
        @DisplayName("Update ohne Status nicht erlaubt")
        void updateApplication_noStatus_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");

            when(userService.userHasRole("STUDENT")).thenReturn(true);

            assertThatThrownBy(() -> applicationService.updateApplication("app-123", dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Status");
        }

        @Test
        @DisplayName("Update mit ungültigem Datum")
        void updateApplication_invalidDate_throwsBadRequest() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("APPLIED");
            dto.setAppliedAt("invalid-date");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.updateApplication("app-123", dto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Ungueltiges Datum");
        }

        @Test
        @DisplayName("Badge-Check wird nach Update aufgerufen")
        void updateApplication_valid_checksBadges() {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setCompanyName("Google");
            dto.setStatus("ACCEPTED");

            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

            applicationService.updateApplication("app-123", dto);

            verify(badgeService).checkAndAwardBadges("student-123");
        }
    }

    // ========================================
    // updateStatus - Edge Cases
    // ========================================
    @Nested
    @DisplayName("updateStatus - Edge Cases")
    class UpdateStatusEdgeCases {

        @Test
        @DisplayName("Bewerbung nicht gefunden")
        void updateStatus_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(applicationRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateStatus("unknown", "APPLIED"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Fremde Bewerbung - Status ändern nicht erlaubt")
        void updateStatus_otherStudent_throwsForbidden() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("other-student");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));

            assertThatThrownBy(() -> applicationService.updateStatus("app-123", "INVITED"))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Null Status wird zu PLANNED")
        void updateStatus_nullStatus_defaultsToPlanned() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.updateStatus("app-123", null);

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PLANNED);
        }

        @Test
        @DisplayName("Leerer Status wird zu PLANNED")
        void updateStatus_emptyStatus_defaultsToPlanned() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.updateStatus("app-123", "");

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PLANNED);
        }

        @Test
        @DisplayName("Status lowercase wird konvertiert")
        void updateStatus_lowercaseStatus_convertsToUppercase() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.updateStatus("app-123", "invited");

            assertThat(result.getStatus()).isEqualTo(ApplicationStatus.INVITED);
        }

        @Test
        @DisplayName("UpdatedAt wird gesetzt")
        void updateStatus_valid_setsUpdatedAt() {
            Instant before = Instant.now();
            
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.findById("app-123")).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            Application result = applicationService.updateStatus("app-123", "INVITED");

            assertThat(result.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    // ========================================
    // deleteApplication - Edge Cases
    // ========================================
    @Nested
    @DisplayName("deleteApplication - Edge Cases")
    class DeleteApplicationEdgeCases {

        @Test
        @DisplayName("Bewerbung nicht gefunden")
        void deleteApplication_notFound_throwsNotFound() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(applicationRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.deleteApplication("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ========================================
    // getMyStats - Edge Cases
    // ========================================
    @Nested
    @DisplayName("getMyStats - Edge Cases")
    class GetMyStatsEdgeCases {

        @Test
        @DisplayName("Active berechnet korrekt")
        void getMyStats_calculatesActiveCorrectly() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.PLANNED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.APPLIED)).thenReturn(2L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INVITED)).thenReturn(3L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INTERVIEW_DONE)).thenReturn(4L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.ACCEPTED)).thenReturn(0L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.REJECTED)).thenReturn(0L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.WITHDRAWN)).thenReturn(0L);

            ApplicationStats result = applicationService.getMyStats();

            // active = planned + applied + invited + interviewDone
            assertThat(result.getActive()).isEqualTo(10);
        }

        @Test
        @DisplayName("Alle Status werden gezählt")
        void getMyStats_countsAllStatuses() {
            when(userService.userHasRole("STUDENT")).thenReturn(true);
            when(userService.getUserId()).thenReturn("student-123");
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.PLANNED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.APPLIED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INVITED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.INTERVIEW_DONE)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.ACCEPTED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.REJECTED)).thenReturn(1L);
            when(applicationRepository.countByStudentIdAndStatus("student-123", ApplicationStatus.WITHDRAWN)).thenReturn(1L);

            ApplicationStats result = applicationService.getMyStats();

            assertThat(result.getTotal()).isEqualTo(7);
            assertThat(result.getPlanned()).isEqualTo(1);
            assertThat(result.getApplied()).isEqualTo(1);
            assertThat(result.getInvited()).isEqualTo(1);
            assertThat(result.getInterviewDone()).isEqualTo(1);
            assertThat(result.getAccepted()).isEqualTo(1);
            assertThat(result.getRejected()).isEqualTo(1);
            assertThat(result.getWithdrawn()).isEqualTo(1);
        }
    }
}
