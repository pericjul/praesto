import { redirect, fail } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';
import { tr } from '$lib/server/i18n.js';

// Selbst-Registrierung eines Privat-Kontos (ohne Schule/Einladung). Startet mit
// einer 7-tägigen Gratis-Testphase.
export const actions = {
	default: async ({ request, cookies, locals }) => {
		const data = await request.formData();
		const firstName = data.get('firstName');
		const lastName = data.get('lastName');
		const email = data.get('email');
		const password = data.get('password');
		const passwordConfirm = data.get('passwordConfirm');
		const parentEmail = data.get('parentEmail');
		const lang = locals.lang;
		const back = { firstName, lastName, email, parentEmail };

		if (!firstName || !lastName || !email || !password) {
			return fail(400, { error: tr(lang, 'verr.fillAll'), ...back });
		}
		if (!parentEmail || !parentEmail.includes('@')) {
			return fail(400, { error: tr(lang, 'verr.parentEmailRequired'), ...back });
		}
		if (data.get('acceptTerms') !== 'on') {
			return fail(400, { error: tr(lang, 'verr.acceptTerms'), ...back });
		}
		if (data.get('parentConsent') !== 'on') {
			return fail(400, { error: tr(lang, 'verr.parentConsent'), ...back });
		}
		if (password.length < 8) {
			return fail(400, { error: tr(lang, 'verr.pwMin8'), ...back });
		}
		if (password !== passwordConfirm) {
			return fail(400, { error: tr(lang, 'verr.pwMismatch'), ...back });
		}

		try {
			await auth.signup({ firstName, lastName, email, password, parentEmail }, cookies, lang);
		} catch (e) {
			return fail(400, { error: e.message || tr(lang, 'verr.registerFailed'), ...back });
		}
		// Zugang erst nach Eltern-Bestätigung -> Warte-Seite.
		throw redirect(303, '/eltern-warten');
	}
};
