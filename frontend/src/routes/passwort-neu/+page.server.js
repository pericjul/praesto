import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export function load({ url }) {
	return { token: url.searchParams.get("token") ?? "" };
}

export const actions = {
	default: async ({ request, fetch }) => {
		const data = await request.formData();
		const token = data.get("token")?.toString();
		const newPassword = data.get("newPassword")?.toString() ?? "";
		const confirm = data.get("confirm")?.toString() ?? "";

		if (newPassword.length < 8) {
			return fail(400, { error: "Das Passwort muss mindestens 8 Zeichen haben." });
		}
		if (newPassword !== confirm) {
			return fail(400, { error: "Die Passwörter stimmen nicht überein." });
		}
		const res = await fetch(`${API_BASE}/auth/reset-password`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ token, newPassword })
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { error: msg && msg.length < 300 ? msg : "Der Link ist ungültig oder abgelaufen." });
		}
		return { done: true };
	}
};
