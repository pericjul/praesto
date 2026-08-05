import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "SUPER_ADMIN") {
		throw redirect(302, "/");
	}
	const res = await fetch(`${API_BASE}/super/knowledge`, { headers: apiHeaders(locals.jwt_token) });
	const sources = res.ok ? await res.json() : [];
	return { sources };
}

// Backend-Fehlermeldung (z.B. "Die Seite konnte nicht gelesen werden …") an die UI durchreichen.
async function errorText(res) {
	const msg = await res.text().catch(() => "");
	return msg && msg.length < 400 ? msg : "Aktion fehlgeschlagen.";
}

export const actions = {
	addLink: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const res = await fetch(`${API_BASE}/super/knowledge/link`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ url: d.get("url"), title: d.get("title") })
		});
		if (!res.ok) return fail(400, { error: await errorText(res) });
		return { success: true };
	},

	addText: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const res = await fetch(`${API_BASE}/super/knowledge/text`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ title: d.get("title"), text: d.get("text") })
		});
		if (!res.ok) return fail(400, { error: await errorText(res) });
		return { success: true };
	},

	addDocument: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		const file = d.get("file");
		if (!file || typeof file === "string" || file.size === 0) {
			return fail(400, { error: "Keine Datei ausgewählt." });
		}
		const fd = new FormData();
		fd.append("file", file);
		const title = d.get("title");
		if (title) fd.append("title", title);
		// Kein Content-Type setzen -> fetch setzt die multipart-boundary selbst.
		const res = await fetch(`${API_BASE}/super/knowledge/document`, {
			method: "POST",
			headers: locals.jwt_token ? { Authorization: `Bearer ${locals.jwt_token}` } : {},
			body: fd
		});
		if (!res.ok) return fail(400, { error: await errorText(res) });
		return { success: true };
	},

	setEnabled: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		await fetch(`${API_BASE}/super/knowledge/${d.get("id")}/enabled`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ enabled: d.get("enabled") === "true" })
		});
		return { success: true };
	},

	delete: async ({ request, locals, fetch }) => {
		const d = await request.formData();
		await fetch(`${API_BASE}/super/knowledge/${d.get("id")}`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		return { success: true };
	}
};
