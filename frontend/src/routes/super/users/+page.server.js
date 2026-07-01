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
	},

	setActive: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const active = data.get("active") === "true";
		const res = await fetch(`${API_BASE}/super/users/${id}/active`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ active })
		});
		if (!res.ok) {
			return { error: true };
		}
		return { activeChanged: true, id, active };
	},

	resetPassword: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const newPassword = data.get("newPassword");
		if (!newPassword || String(newPassword).length < 8) {
			return { error: true, resetFailed: true };
		}
		const res = await fetch(`${API_BASE}/admin/users/${id}/reset-password`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ newPassword })
		});
		if (!res.ok) {
			return { error: true, resetFailed: true };
		}
		return { resetDone: true };
	}
};
