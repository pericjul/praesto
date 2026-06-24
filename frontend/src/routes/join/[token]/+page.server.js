import { redirect, fail } from '@sveltejs/kit';
import auth, { dashboardForRole } from '$lib/server/auth.service.js';
import { API_BASE } from '$lib/server/api.js';

export async function load({ params, fetch }) {
	const res = await fetch(`${API_BASE}/auth/invite/${encodeURIComponent(params.token)}`);
	if (!res.ok) {
		return { invalid: true };
	}
	const invite = await res.json();
	return { invite };
}

export const actions = {
	default: async ({ request, params, cookies }) => {
		const data = await request.formData();
		const firstName = data.get('firstName');
		const lastName = data.get('lastName');
		const email = data.get('email');
		const password = data.get('password');
		const passwordConfirm = data.get('passwordConfirm');

		if (!firstName || !lastName || !email || !password) {
			return fail(400, { error: 'Bitte fülle alle Felder aus.' });
		}
		if (password.length < 8) {
			return fail(400, { error: 'Das Passwort muss mindestens 8 Zeichen haben.' });
		}
		if (password !== passwordConfirm) {
			return fail(400, { error: 'Die Passwörter stimmen nicht überein.' });
		}

		let user;
		try {
			user = await auth.register(params.token, { firstName, lastName, email, password }, cookies);
		} catch (e) {
			return fail(400, { error: e.message || 'Registrierung fehlgeschlagen.' });
		}
		throw redirect(303, dashboardForRole(user.role));
	}
};
