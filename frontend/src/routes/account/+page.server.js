import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

const COOKIE = {
	path: "/",
	maxAge: 60 * 60 * 24 * 7,
	sameSite: "lax",
	httpOnly: true,
	secure: process.env.NODE_ENV === "production"
};

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	let billing = null;
	try {
		const res = await fetch(`${API_BASE}/billing/status`, { headers: apiHeaders(locals.jwt_token) });
		if (res.ok) billing = await res.json();
	} catch {
		billing = null;
	}
	return { billing };
}

export const actions = {
	profile: async ({ request, locals, fetch, cookies }) => {
		const data = await request.formData();
		const firstName = data.get("firstName")?.toString().trim();
		const lastName = data.get("lastName")?.toString().trim();
		if (!firstName || !lastName) {
			return fail(400, { profileError: true });
		}
		const res = await fetch(`${API_BASE}/users/me`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ firstName, lastName })
		});
		if (!res.ok) {
			return fail(400, { profileError: true });
		}
		const user = await res.json();
		// user_info Cookie aktualisieren, damit Header/Anzeige sofort stimmen
		cookies.set("user_info", encodeURIComponent(JSON.stringify(user)), COOKIE);
		return { profileSuccess: true };
	},

	// Stripe-Kundenportal öffnen (Abo verwalten/kündigen).
	portal: async ({ locals, fetch }) => {
		const res = await fetch(`${API_BASE}/billing/portal`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({})
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { portalError: msg && msg.length < 300 ? msg : "Kundenportal konnte nicht geöffnet werden." });
		}
		const { url } = await res.json();
		throw redirect(303, url);
	},

	// Login-E-Mail ändern (mit aktuellem Passwort).
	email: async ({ request, locals, fetch, cookies }) => {
		const data = await request.formData();
		const newEmail = data.get("newEmail")?.toString().trim();
		const currentPassword = data.get("emailPassword");
		if (!newEmail || !newEmail.includes("@")) {
			return fail(400, { emailError: "Bitte gib eine gültige E-Mail-Adresse ein." });
		}
		const res = await fetch(`${API_BASE}/users/me/email`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ newEmail, currentPassword })
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => "");
			return fail(400, { emailError: msg && msg.length < 300 ? msg : "E-Mail konnte nicht geändert werden." });
		}
		const user = await res.json();
		cookies.set("user_info", encodeURIComponent(JSON.stringify(user)), COOKIE);
		return { emailSuccess: true };
	},

	password: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const currentPassword = data.get("currentPassword");
		const newPassword = data.get("newPassword");
		const confirm = data.get("confirm");

		if (!newPassword || newPassword.toString().length < 8) {
			return fail(400, { pwError: "short" });
		}
		if (newPassword !== confirm) {
			return fail(400, { pwError: "mismatch" });
		}

		const res = await fetch(`${API_BASE}/users/me/password`, {
			method: "PUT",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({ currentPassword, newPassword })
		});
		if (!res.ok) {
			return fail(400, { pwError: "wrong" });
		}
		return { pwSuccess: true };
	}
};
