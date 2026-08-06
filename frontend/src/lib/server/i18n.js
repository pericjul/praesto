// Serverseitiger Übersetzer für Validierungsmeldungen in +page.server.js-Actions.
// Nutzt dieselben Übersetzungen wie das Frontend; Sprache kommt aus locals.lang.
import { translations } from "$lib/i18n/translations.js";

export function tr(lang, key) {
	const l = ["de", "en", "fr", "it"].includes(lang) ? lang : "de";
	return translations[l]?.[key] ?? translations.de?.[key] ?? key;
}
