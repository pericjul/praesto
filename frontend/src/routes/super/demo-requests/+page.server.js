import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}
	const res = await fetch(`${API_BASE}/super/demo-requests`, { headers: apiHeaders(locals.jwt_token) });
	return { requests: res.ok ? await res.json() : [] };
}

export const actions = {
	approve: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const date = data.get("date") || null;
		const res = await fetch(`${API_BASE}/super/demo-requests/${id}/approve`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ date })
		});
		if (!res.ok) {
			return { error: true };
		}
		return { success: true };
	},

	setStatus: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const status = data.get("status");
		const res = await fetch(`${API_BASE}/super/demo-requests/${id}/status`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ status })
		});
		if (!res.ok) {
			return { error: true };
		}
		return { success: true };
	}
};
