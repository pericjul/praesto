import { redirect } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';

export const actions = {
	default: async ({ cookies }) => {
		auth.logout(cookies);
		throw redirect(303, '/login');
	}
};
