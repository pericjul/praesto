import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

const COOKIE = {
	path: "/",
	maxAge: 60 * 60 * 24 * 7,
	sameSite: "lax",
	httpOnly: true,
	secure: process.env.NODE_ENV === "production"
};

export async function load({ locals }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	return {};
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
