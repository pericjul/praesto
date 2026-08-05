import { redirect, fail } from '@sveltejs/kit';
import auth, { dashboardForRole } from '$lib/server/auth.service.js';

// Selbst-Registrierung eines Privat-Kontos (ohne Schule/Einladung). Startet mit
// einer 7-tägigen Gratis-Testphase.
export const actions = {
	default: async ({ request, cookies }) => {
		const data = await request.formData();
		const firstName = data.get('firstName');
		const lastName = data.get('lastName');
		const email = data.get('email');
		const password = data.get('password');
		const passwordConfirm = data.get('passwordConfirm');

		if (!firstName || !lastName || !email || !password) {
			return fail(400, { error: 'Bitte fülle alle Felder aus.', firstName, lastName, email });
		}
		if (data.get('acceptTerms') !== 'on') {
			return fail(400, { error: 'Bitte akzeptiere die Datenschutzerklärung und die AGB.', firstName, lastName, email });
		}
		if (data.get('parentConsent') !== 'on') {
			return fail(400, { error: 'Bitte bestätige das Einverständnis eines Elternteils (bei unter 18-Jährigen).', firstName, lastName, email });
		}
		if (password.length < 8) {
			return fail(400, { error: 'Das Passwort muss mindestens 8 Zeichen haben.', firstName, lastName, email });
		}
		if (password !== passwordConfirm) {
			return fail(400, { error: 'Die Passwörter stimmen nicht überein.', firstName, lastName, email });
		}

		let user;
		try {
			user = await auth.signup({ firstName, lastName, email, password }, cookies);
		} catch (e) {
			return fail(400, { error: e.message || 'Registrierung fehlgeschlagen.', firstName, lastName, email });
		}
		// Nach der Registrierung: kurze Willkommens-Umfrage.
		throw redirect(303, '/willkommen');
	}
};
