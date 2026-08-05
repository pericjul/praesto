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

	let overview = null;
	if (selectedId) {
		const res = await fetch(`${API_BASE}/teacher/class/${selectedId}/applications`, { headers });
		if (res.ok) {
			overview = await res.json();
		}
	}

	return { classes, overview, selectedId };
}
