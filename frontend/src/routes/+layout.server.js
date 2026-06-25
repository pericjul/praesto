export const load = async ({ locals }) => {
	return {
		user: locals.user || {},
		isAuthenticated: locals.isAuthenticated || false,
		isDemo: locals.isDemo || false,
		lang: locals.lang || "de"
	};
};
