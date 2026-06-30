import { redirect } from "@sveltejs/kit";

export async function load({ locals }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}
	return {};
}
