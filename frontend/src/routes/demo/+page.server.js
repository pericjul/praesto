import { API_BASE } from '$lib/server/api.js';

export const actions = {
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
