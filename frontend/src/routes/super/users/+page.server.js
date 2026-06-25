import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch, url }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}

	const q = url.searchParams.get("q") ?? "";
	let results = [];
	let searched = false;

	if (q.trim().length >= 2) {
		searched = true;
		const res = await fetch(`${API_BASE}/super/users/search?q=${encodeURIComponent(q)}`, {
			headers: apiHeaders(locals.jwt_token)
		});
		if (res.ok) {
			results = await res.json();
		}
	}

	return { q, results, searched };
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
