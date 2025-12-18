import { redirect } from '@sveltejs/kit';
import 'dotenv/config'; 
const AUTH0_DOMAIN =  process.env.AUTH0_DOMAIN;
const AUTH0_CLIENT_ID =  process.env.AUTH0_CLIENT_ID

export const actions = {
	default: async ({ cookies, url }) => {
		cookies.set('jwt_token', '', { path: '/', maxAge: 0 });
		cookies.set('user_info', '', { path: '/', maxAge: 0 });
		
		const returnTo = encodeURIComponent(`${url.origin}/`);
		const logoutUrl = `https://${AUTH0_DOMAIN}/v2/logout?client_id=${AUTH0_CLIENT_ID}&returnTo=${returnTo}`;
		
		throw redirect(303, logoutUrl);
	}
};
