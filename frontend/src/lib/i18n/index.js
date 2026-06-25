import { writable, derived } from "svelte/store";
import { translations, LANGUAGES } from "./translations.js";

export { LANGUAGES };

const SUPPORTED = LANGUAGES.map((l) => l.code);

// Aktuelle Sprache (de|en|fr|it). Wird im Layout aus dem lang-Cookie initialisiert.
export const locale = writable("de");

// Übersetzungs-Funktion als Store: in Komponenten {$t('key')} verwenden.
export const t = derived(locale, ($locale) => {
	const dict = translations[$locale] ?? translations.de;
	return (key) => dict[key] ?? translations.de[key] ?? key;
});

// Sprache setzen + im Cookie speichern (für SSR beim nächsten Laden).
export function setLocale(lang) {
	const value = SUPPORTED.includes(lang) ? lang : "de";
	locale.set(value);
	if (typeof document !== "undefined") {
		document.cookie = `lang=${value}; path=/; max-age=${60 * 60 * 24 * 365}; samesite=lax`;
	}
}

export function normalizeLang(lang) {
	return SUPPORTED.includes(lang) ? lang : "de";
}
