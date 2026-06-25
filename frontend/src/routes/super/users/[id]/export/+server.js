import { error } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

// Proxy: holt die Datenauskunft mit dem httpOnly-JWT vom Backend und liefert sie
// als herunterladbare JSON-Datei zurück.
export async function GET({ params, locals, fetch }) {
	if (!locals.isAuthenticated || locals.user?.role !== "SUPER_ADMIN") {
		throw error(403, "Keine Berechtigung");
	}

	const res = await fetch(`${API_BASE}/super/users/${params.id}/export`, {
		headers: apiHeaders(locals.jwt_token)
	});
	if (!res.ok) {
		throw error(res.status, "Export fehlgeschlagen");
	}

	const text = await res.text();
	return new Response(text, {
		headers: {
			"Content-Type": "application/json",
			"Content-Disposition": `attachment; filename="praesto-userdata-${params.id}.json"`
		}
	});
}
