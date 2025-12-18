import { redirect } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';

export const actions = {
  signup: async ({ request, cookies }) => {
    const data = await request.formData();
    const email = data.get('email');
    const password = data.get('password');
    const firstName = data.get('firstName');
    const lastName = data.get('lastName');

    try {
      await auth.signup(email, password, firstName, lastName, cookies);
    } catch (error) {
      console.error('Signup error:', error);
      return {
        error: 'Signup failed. Please try again.'
      };
    }
    
    throw redirect(303, '/');
  }
};
