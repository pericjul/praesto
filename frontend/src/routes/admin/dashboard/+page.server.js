import { redirect } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

const ADMIN_ROLES = ["SCHOOL_ADMIN", "DEMO_USER"];

export async function load({ locals, fetch, url }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (!ADMIN_ROLES.includes(locals.user?.role)) {
		throw redirect(302, "/");
	}

	const headers = apiHeaders(locals.jwt_token);
	const [statsRes, insightsRes, teachersRes, studentsRes, invitesRes] = await Promise.all([
		fetch(`${API_BASE}/admin/stats`, { headers }),
		fetch(`${API_BASE}/admin/insights`, { headers }),
		fetch(`${API_BASE}/admin/users/teachers`, { headers }),
		fetch(`${API_BASE}/admin/users/students`, { headers }),
		fetch(`${API_BASE}/admin/invites`, { headers })
	]);

	return {
		stats: statsRes.ok ? await statsRes.json() : { teachers: 0, students: 0, classes: 0, assignments: 0 },
		insights: insightsRes.ok ? await insightsRes.json() : null,
		teachers: teachersRes.ok ? await teachersRes.json() : [],
		students: studentsRes.ok ? await studentsRes.json() : [],
		invites: invitesRes.ok ? await invitesRes.json() : [],
		isDemo: locals.user?.role === "DEMO_USER",
		origin: url.origin
	};
}

export const actions = {
	createTeacherInvite: async ({ locals, fetch }) => {
		const res = await fetch(`${API_BASE}/admin/invites/teacher`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify({})
		});
		if (!res.ok) {
			return { error: "Einladung konnte nicht erstellt werden." };
		}
		return { invite: await res.json() };
	},

	deleteInvite: async ({ request, locals, fetch }) => {
		const data = await request.formData();
		const id = data.get("id");
		const res = await fetch(`${API_BASE}/admin/invites/${id}`, {
			method: "DELETE",
			headers: apiHeaders(locals.jwt_token)
		});
		if (!res.ok) {
			return { error: "Einladung konnte nicht deaktiviert werden." };
		}
		return { success: true };
	}
};
