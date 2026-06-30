import { redirect, fail } from "@sveltejs/kit";
import { API_BASE, apiHeaders, uploadFile } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw redirect(302, "/login");
	}
	if (locals.user?.role !== "STUDENT") {
		throw redirect(302, "/");
	}
	let quota = null;
	try {
		const q = await fetch(`${API_BASE}/student/ai-quota`, { headers: apiHeaders(locals.jwt_token) });
		if (q.ok) {
			quota = await q.json().catch(() => null);
		}
	} catch {
		quota = null;
	}
	return { quota, user: locals.user };
}

export const actions = {
	default: async ({ request, locals, fetch }) => {
		const d = await request.formData();

		// Optionales Bewerbungsfoto zuerst hochladen, dann als photoUrl mitschicken.
		let photoUrl = null;
		const photo = d.get("photo");
		if (photo && typeof photo !== "string" && photo.size > 0) {
			try {
				const up = await uploadFile(photo, locals.jwt_token);
				photoUrl = up.fileUrl;
			} catch {
				photoUrl = null; // Foto ist optional
			}
		}

		const payload = {
			firstName: d.get("firstName"),
			lastName: d.get("lastName"),
			address: d.get("address"),
			zipCity: d.get("zipCity"),
			phone: d.get("phone"),
			email: d.get("email"),
			birthDate: d.get("birthDate"),
			hometown: d.get("hometown"),
			nationality: d.get("nationality"),
			parents: d.get("parents"),
			siblings: d.get("siblings"),
			photoUrl,
			aboutMe: d.get("aboutMe"),
			targetJob: d.get("targetJob"),
			education: d.get("education"),
			experience: d.get("experience"),
			skills: d.get("skills"),
			languages: d.get("languages"),
			hobbies: d.get("hobbies"),
			references: d.get("references")
		};
		const res = await fetch(`${API_BASE}/student/cv`, {
			method: "POST",
			headers: apiHeaders(locals.jwt_token),
			body: JSON.stringify(payload)
		});
		if (!res.ok) {
			const msg = await res.text().catch(() => null);
			return fail(400, { error: msg || "error" });
		}
		throw redirect(303, "/student/dossier");
	}
};
