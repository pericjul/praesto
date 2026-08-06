import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";
import { tr } from "$lib/server/i18n.js";

export function load({ url }) {
	return { token: url.searchParams.get("token") ?? "" };
}

export const actions = {
	default: async ({ request, fetch, locals }) => {
		const data = await request.formData();
		const token = data.get("token")?.toString();
		const newPassword = data.get("newPassword")?.toString() ?? "";
		const confirm = data.get("confirm")?.toString() ?? "";
		const lang = locals.lang;

		if (newPassword.length < 8) {
			return fail(400, { error: tr(lang, "verr.pwMin8") });
		}
		if (newPassword !== confirm) {
			return fail(400, { error: tr(lang, "verr.pwMismatch") });
		}
		const res = await fetch(`${API_BASE}/auth/reset-password`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ token, newPassword })
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { error: msg && msg.length < 300 ? msg : tr(lang, "verr.linkInvalidExpired") });
		}
		return { done: true };
	}
};
