import { redirect } from "@sveltejs/kit";

export function load({ locals }) {
	// Eingeloggte direkt ins Dashboard; Gäste sehen die Schaufenster-Seite.
	if (locals.isAuthenticated) {
		throw redirect(302, "/dashboard");
	}
	return {};
}
