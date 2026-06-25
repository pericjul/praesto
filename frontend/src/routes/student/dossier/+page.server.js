import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders, uploadFile } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "STUDENT") {
		throw redirect(302, "/");
	}
	const headers = apiHeaders(locals.jwt_token);

	const res = await fetch(`${API_BASE}/student/documents`, { headers });
	const documents = res.ok ? await res.json() : [];

	let quota = null;
	try {
		const q = await fetch(`${API_BASE}/student/ai-quota`, { headers });
		if (q.ok) {
			quota = await q.json().catch(() => null);
		}
	} catch {
		quota = null;
	}

	return { documents, quota };
}

export const actions = {
	upload: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const file = data.get("file");
		const category = data.get("category") || "SONSTIGES";
		const title = data.get("title") || "";

		if (!file || typeof file === "string" || file.size === 0) {
			return { error: true };
		}

		let uploaded;
		try {
			uploaded = await uploadFile(file, locals.jwt_token);
		} catch {
			return { error: true };
		}

		const res = await fetch(`${API_BASE}/student/documents`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({
				category,
				title,
				fileUrl: uploaded.fileUrl,
				fileName: uploaded.fileName
			})
		});
		return { success: res.ok };
	},

	delete: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const res = await fetch(`${API_BASE}/student/documents/${id}`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		return { success: res.ok };
	}
};
