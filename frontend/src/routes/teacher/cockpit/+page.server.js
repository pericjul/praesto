import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch, url }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "TEACHER") {
		throw redirect(302, "/");
	}
	const headers = apiHeaders(locals.jwt_token);

	const classesRes = await fetch(`${API_BASE}/classes`, { headers });
	const classes = classesRes.ok ? await classesRes.json() : [];

	const selectedId = url.searchParams.get("class") || (classes[0]?.id ?? null);

	let cockpit = null;
	let challenge = null;
	if (selectedId) {
		const res = await fetch(`${API_BASE}/classes/${selectedId}/cockpit`, { headers });
		if (res.ok) {
			cockpit = await res.json();
		}
		const chRes = await fetch(`${API_BASE}/classes/${selectedId}/challenge`, { headers });
		if (chRes.ok) {
			challenge = await chRes.json().catch(() => null);
		}
	}

	return { classes, cockpit, challenge, selectedId };
}

export const actions = {
	startChallenge: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const classId = data.get("classId");
		const res = await fetch(`${API_BASE}/classes/${classId}/challenge`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({})
		});
		return { success: res.ok };
	},

	endChallenge: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const classId = data.get("classId");
		const res = await fetch(`${API_BASE}/classes/${classId}/challenge`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		return { success: res.ok };
	}
};
