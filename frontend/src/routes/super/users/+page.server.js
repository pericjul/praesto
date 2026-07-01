import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}

	let users = [];
	let loadError = null;
	const res = await fetch(`${API_BASE}/super/users/all`, { headers: apiHeaders(locals.jwt_token) });
	if (res.ok) {
		users = await res.json();
	} else {
		loadError = (await res.text().catch(() => "")) || `Fehler ${res.status}`;
	}

	return { users, loadError };
}

export const actions = {
	delete: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const res = await fetch(`${API_BASE}/super/users/${id}`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		if (!res.ok) {
			return { error: true };
		}
		return { deleted: true, id };
	}
};
