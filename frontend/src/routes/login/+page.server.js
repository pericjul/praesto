import { redirect } from '@sveltejs/kit';
import auth, { dashboardForRole } from '$lib/server/auth.service.js';

export const actions = {
  default: async ({ request, cookies }) => {
    const data = await request.formData();
    const email = data.get('email');
    const password = data.get('password');

    let user;
    try {
      user = await auth.login(email, password, cookies);
    } catch (error) {
      return {
        error: error.message || 'Login fehlgeschlagen. Bitte überprüfe deine Zugangsdaten.'
      };
    }
    throw redirect(303, dashboardForRole(user.role));
  }
};
