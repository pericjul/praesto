import { redirect } from '@sveltejs/kit';
import auth, { dashboardForRole } from '$lib/server/auth.service.js';
import { API_BASE } from '$lib/server/api.js';

export const actions = {
	// Demo-Login in einer bestimmten Rolle (Buttons unten auf der Seite).
	role: async ({ request, cookies }) => {
		const data = await request.formData();
		const as = data.get('as'); // student | teacher | admin (optional)

		let user;
		try {
			user = await auth.demoLogin(as, cookies);
		} catch (e) {
			return { error: e.message || 'Demo-Login fehlgeschlagen.' };
		}
		throw redirect(303, dashboardForRole(user.role));
	},

	// Öffentliche Anfrage für eine Demo mit Schreibzugriff an einem Wunschtag.
	request: async ({ request, fetch }) => {
		const data = await request.formData();
		const payload = {
			schoolName: data.get('schoolName'),
			contactName: data.get('contactName'),
			email: data.get('email'),
			preferredDate: data.get('preferredDate') || null,
			message: data.get('message')
		};

		if (!payload.schoolName || !payload.email) {
			return { requestError: true };
		}

		try {
			const res = await fetch(`${API_BASE}/demo-requests`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			});
			if (!res.ok) {
				return { requestError: true };
			}
		} catch (e) {
			console.error('Demo-Anfrage fehlgeschlagen:', e);
			return { requestError: true };
		}

		return { requested: true };
	}
};
