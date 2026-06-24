package ch.zhaw.praesto.service;

import ch.zhaw.praesto.exception.BadRequestException;
import ch.zhaw.praesto.exception.ForbiddenException;
import ch.zhaw.praesto.model.ContactInquiry;
import ch.zhaw.praesto.model.ContactRequest;
import ch.zhaw.praesto.model.UserRole;
import ch.zhaw.praesto.repository.ContactInquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Nimmt Kontakt-/Offerten-Anfragen entgegen: speichert sie immer in der DB und
 * schickt – falls SMTP konfiguriert ist – zusätzlich eine Benachrichtigungs-Mail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactInquiryRepository inquiryRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final UserService userService;

    @Value("${praesto.contact.recipient:}")
    private String recipient;

    // Absenderadresse (muss beim Mail-Dienst als verifizierter Sender hinterlegt sein).
    // Leer = es wird die Empfängeradresse als Absender verwendet.
    @Value("${praesto.contact.from:}")
    private String from;

    public ContactInquiry submit(ContactRequest req) {
        if (req == null || req.email() == null || !req.email().contains("@")) {
            throw new BadRequestException("Bitte gib eine gültige E-Mail-Adresse an.");
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new BadRequestException("Bitte gib deinen Namen an.");
        }

        ContactInquiry inquiry = ContactInquiry.builder()
                .name(req.name().trim())
                .email(req.email().trim())
                .organisation(req.organisation())
                .role(req.role())
                .interest(req.interest())
                .classes(req.classes())
                .students(req.students())
                .wantsMeeting(req.wantsMeeting())
                .message(req.message())
                .handled(false)
                .createdAt(Instant.now())
                .build();

        ContactInquiry saved = inquiryRepository.save(inquiry);
        sendNotification(saved);
        return saved;
    }

    public List<ContactInquiry> list() {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    public ContactInquiry markHandled(String id, boolean handled) {
        if (!userService.userHasRole(UserRole.SUPER_ADMIN)) {
            throw new ForbiddenException("Keine Berechtigung");
        }
        ContactInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Anfrage nicht gefunden"));
        inquiry.setHandled(handled);
        return inquiryRepository.save(inquiry);
    }

    private void sendNotification(ContactInquiry i) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || recipient == null || recipient.isBlank()) {
            log.info("Neue Kontaktanfrage von {} gespeichert (kein Mailversand konfiguriert).", i.getEmail());
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(recipient);
            msg.setFrom(from != null && !from.isBlank() ? from : recipient);
            String who = i.getOrganisation() != null && !i.getOrganisation().isBlank()
                    ? i.getOrganisation() : i.getName();
            msg.setSubject("Neue Praesto-Anfrage: " + who);
            msg.setText(buildBody(i));
            if (i.getEmail() != null) {
                msg.setReplyTo(i.getEmail());
            }
            sender.send(msg);
            log.info("Kontaktanfrage-Mail an {} versendet.", recipient);
        } catch (Exception e) {
            log.error("Mailversand der Kontaktanfrage fehlgeschlagen: {}", e.getMessage());
        }
    }

    private String buildBody(ContactInquiry i) {
        return """
                Neue Anfrage über das Praesto-Kontaktformular:

                Name:          %s
                E-Mail:        %s
                Organisation:  %s
                Funktion:      %s
                Anliegen:      %s
                Klassen:       %s
                Schüler:innen: %s
                Gespräch gewünscht: %s

                Nachricht:
                %s
                """.formatted(
                nz(i.getName()), nz(i.getEmail()), nz(i.getOrganisation()), nz(i.getRole()),
                nz(i.getInterest()), i.getClasses() != null ? i.getClasses() : "-",
                i.getStudents() != null ? i.getStudents() : "-",
                i.isWantsMeeting() ? "Ja" : "Nein", nz(i.getMessage()));
    }

    private String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
