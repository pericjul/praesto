import { fail } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export function load({ url }) {
	return { token: url.searchParams.get("token") ?? "" };
}

export const actions = {
	default: async ({ request, fetch }) => {
		const data = await request.formData();
		const token = data.get("token")?.toString();
		const res = await fetch(`${API_BASE}/consent/confirm`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ token })
		});
		if (!res.ok) {
			return fail(400, { invalid: true });
		}
		const { name } = await res.json();
		return { done: true, name: name ?? "" };
	}
};
