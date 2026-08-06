package ch.zhaw.praesto.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Leichter Übersetzer für benutzer­sichtbare Backend-Texte (Fehlermeldungen, Reset-Mail).
 * Die Sprache kommt aus dem {@code X-Locale}-Header (vom Frontend gesetzt), Fallback Deutsch.
 * Bewusst als In-Code-Map (analog zum Frontend-i18n), damit kein ResourceBundle-Setup nötig ist.
 */
@Service
public class LocaleMessages {

    private final Map<String, Map<String, String>> messages = new HashMap<>();

    public LocaleMessages() {
        put("err.firstNameRequired",
                "Vorname ist erforderlich", "First name is required", "Le prénom est requis", "Il nome è obbligatorio");
        put("err.lastNameRequired",
                "Nachname ist erforderlich", "Last name is required", "Le nom est requis", "Il cognome è obbligatorio");
        put("err.invalidEmail",
                "Ungültige Email-Adresse", "Invalid email address", "Adresse e-mail non valide", "Indirizzo e-mail non valido");
        put("err.passwordMin8",
                "Passwort muss mindestens 8 Zeichen haben", "Password must be at least 8 characters",
                "Le mot de passe doit contenir au moins 8 caractères", "La password deve avere almeno 8 caratteri");
        put("err.emailExists",
                "Diese Email ist bereits registriert", "This email is already registered",
                "Cet e-mail est déjà enregistré", "Questa e-mail è già registrata");
        put("err.disposableEmail",
                "Bitte verwende eine echte, persönliche E-Mail-Adresse (keine Wegwerf-Adresse).",
                "Please use a real, personal email address (no disposable address).",
                "Utilise une vraie adresse e-mail personnelle (pas une adresse jetable).",
                "Usa un indirizzo e-mail reale e personale (non usa e getta).");
        put("err.currentPasswordWrong",
                "Aktuelles Passwort ist falsch.", "Current password is incorrect.",
                "Le mot de passe actuel est incorrect.", "La password attuale non è corretta.");
        put("err.emailInvalid",
                "Bitte gib eine gültige E-Mail-Adresse ein.", "Please enter a valid email address.",
                "Saisis une adresse e-mail valide.", "Inserisci un indirizzo e-mail valido.");
        put("err.emailTaken",
                "Diese E-Mail-Adresse ist bereits vergeben.", "This email address is already taken.",
                "Cette adresse e-mail est déjà utilisée.", "Questo indirizzo e-mail è già in uso.");
        put("err.tooManyAttempts",
                "Zu viele Versuche. Bitte versuche es in einer Weile erneut.",
                "Too many attempts. Please try again in a while.",
                "Trop de tentatives. Réessaie dans un moment.",
                "Troppi tentativi. Riprova tra un po'.");
        put("err.resetLinkInvalid",
                "Dieser Link ist ungültig.", "This link is invalid.",
                "Ce lien n'est pas valide.", "Questo link non è valido.");
        put("err.resetLinkUsed",
                "Dieser Link wurde bereits verwendet.", "This link has already been used.",
                "Ce lien a déjà été utilisé.", "Questo link è già stato usato.");
        put("err.resetLinkExpired",
                "Dieser Link ist abgelaufen. Bitte fordere einen neuen an.",
                "This link has expired. Please request a new one.",
                "Ce lien a expiré. Demandes-en un nouveau.",
                "Questo link è scaduto. Richiedine uno nuovo.");
        put("err.accountNotFound",
                "Konto nicht gefunden.", "Account not found.", "Compte introuvable.", "Account non trovato.");

        // Passwort-Reset-Mail
        put("mail.reset.subject",
                "Praesto – Passwort zurücksetzen", "Praesto – reset your password",
                "Praesto – réinitialiser le mot de passe", "Praesto – reimposta la password");
        put("mail.reset.greeting", "Hallo %NAME,", "Hi %NAME,", "Bonjour %NAME,", "Ciao %NAME,");
        put("mail.reset.introPlain",
                "du hast angefragt, dein Praesto-Passwort zurückzusetzen. Öffne diesen Link, um ein neues Passwort zu setzen:",
                "you requested to reset your Praesto password. Open this link to set a new password:",
                "tu as demandé à réinitialiser ton mot de passe Praesto. Ouvre ce lien pour définir un nouveau mot de passe :",
                "hai richiesto di reimpostare la tua password Praesto. Apri questo link per impostare una nuova password:");
        put("mail.reset.introHtml",
                "du hast angefragt, dein Passwort zurückzusetzen. Klicke auf den Button, um ein neues Passwort zu setzen:",
                "you requested to reset your password. Click the button to set a new password:",
                "tu as demandé à réinitialiser ton mot de passe. Clique sur le bouton pour en définir un nouveau :",
                "hai richiesto di reimpostare la password. Clicca sul pulsante per impostarne una nuova:");
        put("mail.reset.button",
                "Passwort zurücksetzen", "Reset password", "Réinitialiser le mot de passe", "Reimposta la password");
        put("mail.reset.validity",
                "Der Link ist 1 Stunde gültig. Falls du das nicht warst, ignoriere diese E-Mail einfach – dein Passwort bleibt unverändert.",
                "The link is valid for 1 hour. If this wasn't you, simply ignore this email – your password stays unchanged.",
                "Le lien est valable 1 heure. Si ce n'était pas toi, ignore simplement cet e-mail – ton mot de passe reste inchangé.",
                "Il link è valido 1 ora. Se non sei stato tu, ignora questa e-mail – la tua password resta invariata.");
        put("mail.reset.fallback",
                "Falls der Button nicht geht:", "If the button doesn't work:",
                "Si le bouton ne fonctionne pas :", "Se il pulsante non funziona:");
        put("mail.reset.signature",
                "Freundliche Grüsse\nDein Praesto-Team", "Best regards\nYour Praesto team",
                "Cordialement\nTon équipe Praesto", "Cordiali saluti\nIl tuo team Praesto");

        // Eltern-Einverständnis-Mail
        put("mail.consent.subject",
                "Praesto – Einverständnis für Ihr Kind", "Praesto – consent for your child",
                "Praesto – consentement pour votre enfant", "Praesto – consenso per suo figlio");
        put("mail.consent.greeting", "Guten Tag,", "Hello,", "Bonjour,", "Buongiorno,");
        put("mail.consent.intro",
                "%NAME möchte Praesto nutzen – eine Schweizer Plattform, auf der Jugendliche mit einem KI-Coach Bewerbungsgespräche üben und Bewerbungsunterlagen erstellen. Für die Nutzung durch Minderjährige benötigen wir das Einverständnis eines Elternteils bzw. der erziehungsberechtigten Person.",
                "%NAME would like to use Praesto – a Swiss platform where young people practise job interviews with an AI coach and create application documents. For use by minors, we need the consent of a parent or legal guardian.",
                "%NAME souhaite utiliser Praesto – une plateforme suisse où les jeunes s'entraînent aux entretiens d'embauche avec un coach IA et créent des dossiers de candidature. Pour une utilisation par des mineurs, nous avons besoin du consentement d'un parent ou du représentant légal.",
                "%NAME vorrebbe usare Praesto – una piattaforma svizzera dove i giovani si allenano ai colloqui di lavoro con un coach IA e creano documenti di candidatura. Per l'uso da parte di minori serve il consenso di un genitore o del tutore legale.");
        put("mail.consent.action",
                "Bitte bestätigen Sie das Einverständnis hier:", "Please confirm your consent here:",
                "Merci de confirmer votre consentement ici :", "Confermi il consenso qui:");
        put("mail.consent.button",
                "Einverständnis bestätigen", "Confirm consent", "Confirmer le consentement", "Conferma il consenso");
        put("mail.consent.note",
                "Ohne Bestätigung wird kein Zugang freigeschaltet. Falls Sie diese E-Mail irrtümlich erhalten haben, ignorieren Sie sie einfach.",
                "Without confirmation, no access is granted. If you received this email by mistake, simply ignore it.",
                "Sans confirmation, aucun accès n'est accordé. Si vous avez reçu cet e-mail par erreur, ignorez-le simplement.",
                "Senza conferma non viene attivato alcun accesso. Se ha ricevuto questa e-mail per errore, la ignori.");
        put("err.consentLinkInvalid",
                "Dieser Bestätigungslink ist ungültig oder wurde bereits verwendet.",
                "This confirmation link is invalid or has already been used.",
                "Ce lien de confirmation n'est pas valide ou a déjà été utilisé.",
                "Questo link di conferma non è valido o è già stato usato.");
        put("err.parentEmailRequired",
                "Bitte gib die E-Mail-Adresse eines Elternteils an.", "Please provide a parent's email address.",
                "Merci d'indiquer l'adresse e-mail d'un parent.", "Indica l'indirizzo e-mail di un genitore.");
        // Vorwarn-Mail vor automatischer Löschung
        put("mail.retention.subject",
                "Praesto – dein Konto wird bald gelöscht", "Praesto – your account will be deleted soon",
                "Praesto – ton compte sera bientôt supprimé", "Praesto – il tuo account sarà presto eliminato");
        put("mail.retention.intro",
                "deine Praesto-Testphase ist abgelaufen und dein Konto ist seit einer Weile inaktiv. In %DAYS Tagen löschen wir dein Konto und alle deine Daten automatisch.",
                "your Praesto trial has ended and your account has been inactive for a while. In %DAYS days we will automatically delete your account and all your data.",
                "ta période d'essai Praesto est terminée et ton compte est inactif depuis un moment. Dans %DAYS jours, nous supprimerons automatiquement ton compte et toutes tes données.",
                "la tua prova Praesto è terminata e il tuo account è inattivo da un po'. Tra %DAYS giorni elimineremo automaticamente il tuo account e tutti i tuoi dati.");
        put("mail.retention.action",
                "Wenn du dein Konto behalten möchtest, melde dich an oder löse ein Abo:",
                "If you'd like to keep your account, log in or subscribe:",
                "Si tu souhaites conserver ton compte, connecte-toi ou souscris un abonnement :",
                "Se vuoi mantenere il tuo account, accedi o abbonati:");
        put("mail.retention.button",
                "Konto behalten", "Keep my account", "Conserver mon compte", "Mantieni l'account");
        put("mail.retention.note",
                "Wenn du nichts unternimmst, ist keine weitere Aktion nötig – dein Konto wird dann gelöscht.",
                "If you do nothing, no further action is needed – your account will then be deleted.",
                "Si tu ne fais rien, aucune action n'est nécessaire – ton compte sera alors supprimé.",
                "Se non fai nulla, non serve alcuna azione – il tuo account verrà eliminato.");
        put("err.parentEmailSame",
                "Die Eltern-E-Mail muss sich von deiner eigenen unterscheiden.",
                "The parent's email must differ from your own.",
                "L'e-mail du parent doit être différent du tien.",
                "L'e-mail del genitore deve essere diverso dal tuo.");
    }

    private void put(String key, String de, String en, String fr, String it) {
        Map<String, String> m = new HashMap<>();
        m.put("de", de);
        m.put("en", en);
        m.put("fr", fr);
        m.put("it", it);
        messages.put(key, m);
    }

    /** Aktuelle UI-Sprache aus dem X-Locale-Header (Fallback Deutsch). */
    public String currentLang() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                String h = sra.getRequest().getHeader("X-Locale");
                if (h != null && !h.isBlank()) {
                    String lang = h.trim().toLowerCase(Locale.ROOT);
                    lang = lang.length() >= 2 ? lang.substring(0, 2) : lang;
                    if (messages.getOrDefault("mail.reset.button", Map.of()).containsKey(lang)) {
                        return lang;
                    }
                }
            }
        } catch (Exception ignored) {
            // kein Request-Kontext -> Deutsch
        }
        return "de";
    }

    /** Übersetzten Text holen (Fallback Deutsch, dann der Key selbst). */
    public String get(String key) {
        Map<String, String> m = messages.get(key);
        if (m == null) {
            return key;
        }
        return m.getOrDefault(currentLang(), m.getOrDefault("de", key));
    }

    /** Wie {@link #get(String)}, aber mit einfacher Platzhalter-Ersetzung (%NAME etc.). */
    public String get(String key, Map<String, String> vars) {
        String s = get(key);
        if (vars != null) {
            for (var e : vars.entrySet()) {
                s = s.replace("%" + e.getKey(), e.getValue() == null ? "" : e.getValue());
            }
        }
        return s;
    }
}
