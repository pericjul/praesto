import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

const ADMIN_ROLES = ["SCHOOL_ADMIN", "DEMO_USER"];

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (!ADMIN_ROLES.includes(locals.user?.role)) {
		throw redirect(302, "/");
	}

	const res = await fetch(`${API_BASE}/admin/users`, { headers: apiHeaders(locals.jwt_token) });
	return {
		users: res.ok ? await res.json() : [],
		isDemo: locals.user?.role === "DEMO_USER"
	};
}

export const actions = {
	deactivate: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const res = await fetch(`${API_BASE}/admin/users/${id}/deactivate`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token)
		});
		if (!res.ok) {
			return { error: "Benutzer konnte nicht deaktiviert werden." };
		}
		return { success: true };
	}
};
