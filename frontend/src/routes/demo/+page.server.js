import { redirect } from '@sveltejs/kit';
import auth, { dashboardForRole } from '$lib/server/auth.service.js';

export const actions = {
	default: async ({ request, cookies }) => {
		const data = await request.formData();
		const as = data.get('as'); // student | teacher | admin (optional)

		let user;
		try {
			user = await auth.demoLogin(as, cookies);
		} catch (e) {
			return { error: e.message || 'Demo-Login fehlgeschlagen.' };
		}
		throw redirect(303, dashboardForRole(user.role));
	}
};
