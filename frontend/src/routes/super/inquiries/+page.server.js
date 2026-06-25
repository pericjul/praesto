import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}
	const res = await fetch(`${API_BASE}/super/inquiries`, { headers: apiHeaders(locals.jwt_token) });
	return { inquiries: res.ok ? await res.json() : [] };
}

export const actions = {
	toggleHandled: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const handled = data.get("handled") === "true";
		const res = await fetch(`${API_BASE}/super/inquiries/${id}/handled`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ handled })
		});
		if (!res.ok) {
			return { error: true };
		}
		return { success: true };
	}
};
