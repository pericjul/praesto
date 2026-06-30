// Badge-Namen/-Beschreibungen werden im Frontend pro ruleType + threshold übersetzt
// (statt aus der deutschen DB). %N = Schwellenwert. DE+EN; FR/IT fallen auf DE zurück.
// Fehlt ein Schlüssel, zeigt die Seite den (deutschen) DB-Titel als Fallback.
export const badgeNames = {
	de: {
		"badge.t.SESSIONS_COMPLETED": "%N× trainiert",
		"badge.d.SESSIONS_COMPLETED": "Schliesse %N Übungsgespräche ab.",
		"badge.t.NOTES_CREATED": "%N Notizen",
		"badge.d.NOTES_CREATED": "Erstelle %N Notizen.",
		"badge.t.APPLICATIONS_CREATED": "%N Bewerbungen",
		"badge.d.APPLICATIONS_CREATED": "Erfasse %N Bewerbungen.",
		"badge.t.SUBMISSIONS_COMPLETED": "%N Aufgaben abgegeben",
		"badge.d.SUBMISSIONS_COMPLETED": "Gib %N Aufgaben ab.",
		"badge.t.FEEDBACK_RECEIVED": "%N× Feedback",
		"badge.d.FEEDBACK_RECEIVED": "Erhalte %N× Feedback von deiner Lehrperson.",
		"badge.t.GRADES_RECEIVED": "%N Noten",
		"badge.d.GRADES_RECEIVED": "Erhalte %N Bewertungen.",
		"badge.as.0": "Erste Bewerbung verschickt",
		"badge.asd.0": "Verschicke deine erste Bewerbung.",
		"badge.as.1": "Zu einem Gespräch eingeladen",
		"badge.asd.1": "Werde zu einem Bewerbungsgespräch eingeladen.",
		"badge.as.2": "Zusage erhalten",
		"badge.asd.2": "Erhalte eine Zusage für eine Lehrstelle.",
		"badge.as.3": "Bewerbungsziel erreicht",
		"badge.asd.3": "Erreiche einen Meilenstein im Bewerbungsprozess."
	},
	en: {
		"badge.t.SESSIONS_COMPLETED": "%N× trained",
		"badge.d.SESSIONS_COMPLETED": "Complete %N practice interviews.",
		"badge.t.NOTES_CREATED": "%N notes",
		"badge.d.NOTES_CREATED": "Create %N notes.",
		"badge.t.APPLICATIONS_CREATED": "%N applications",
		"badge.d.APPLICATIONS_CREATED": "Record %N applications.",
		"badge.t.SUBMISSIONS_COMPLETED": "%N assignments submitted",
		"badge.d.SUBMISSIONS_COMPLETED": "Submit %N assignments.",
		"badge.t.FEEDBACK_RECEIVED": "%N× feedback",
		"badge.d.FEEDBACK_RECEIVED": "Receive feedback %N× from your teacher.",
		"badge.t.GRADES_RECEIVED": "%N grades",
		"badge.d.GRADES_RECEIVED": "Receive %N grades.",
		"badge.as.0": "First application sent",
		"badge.asd.0": "Send your first application.",
		"badge.as.1": "Invited to an interview",
		"badge.asd.1": "Get invited to a job interview.",
		"badge.as.2": "Offer received",
		"badge.asd.2": "Receive an apprenticeship offer.",
		"badge.as.3": "Application milestone reached",
		"badge.asd.3": "Reach a milestone in the application process."
	},
	fr: {},
	it: {}
};
