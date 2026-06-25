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

	const classesRes = await fetch(`${API_BASE}/classes/my`, { headers });
	const classes = classesRes.ok ? await classesRes.json() : [];

	const selectedId = url.searchParams.get("class") || (classes[0]?.id ?? null);

	let cockpit = null;
	if (selectedId) {
		const res = await fetch(`${API_BASE}/classes/${selectedId}/cockpit`, { headers });
		if (res.ok) {
			cockpit = await res.json();
		}
	}

	return { classes, cockpit, selectedId };
}
