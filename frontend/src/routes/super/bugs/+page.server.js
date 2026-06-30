import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}
	const res = await fetch(`${API_BASE}/super/bugs`, { headers: apiHeaders(locals.jwt_token) });
	const reports = res.ok ? await res.json() : [];
	return { reports };
}

export const actions = {
	setStatus: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const id = d.get("id");
		const status = d.get("status");
		await fetch(`${API_BASE}/super/bugs/${id}/status`, {
			method: "PATCH",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ status })
		});
		return { success: true };
	},

	delete: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const id = d.get("id");
		await fetch(`${API_BASE}/super/bugs/${id}`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		return { success: true };
	}
};
