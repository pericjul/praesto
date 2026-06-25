import { json } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

// Proxies the teacher student-search to the backend, attaching the httpOnly JWT
// (which the browser itself cannot read). Used by the debounced search field on
// the teacher classes page.
export async function GET({ url, locals, fetch }) {
	if (!locals.isAuthenticated || locals.user?.role !== "TEACHER") {
		return json([], { status: 403 });
	}

	const q = url.searchParams.get("q") ?? "";
	if (q.trim().length < 2) {
		return json([]);
	}

	const res = await fetch(`${API_BASE}/users/students/search?q=${encodeURIComponent(q)}`, {
		headers: apiHeaders(locals.jwt_token)
	});
	if (!res.ok) {
		return json([]);
	}
	return json(await res.json());
}
