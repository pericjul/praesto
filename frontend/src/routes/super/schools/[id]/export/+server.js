import { error } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

// Proxy: holt das komplette Schul-CSV mit dem httpOnly-JWT vom Backend und
// liefert es als herunterladbare Datei zurück (nur Super-Admin).
export async function GET({ params, locals, fetch }) {
	if (!locals.isAuthenticated || locals.user?.role !== "SUPER_ADMIN") {
		throw error(403, "Keine Berechtigung");
	}

	const res = await fetch(`${API_BASE}/super/schools/${params.id}/export.csv`, {
		headers: apiHeaders(locals.jwt_token)
	});
	if (!res.ok) {
		throw error(res.status, "CSV-Export fehlgeschlagen");
	}

	const body = await res.text();
	const disposition = res.headers.get("content-disposition") || `attachment; filename="praesto-schule.csv"`;
	return new Response(body, {
		headers: {
			"Content-Type": "text/csv; charset=utf-8",
			"Content-Disposition": disposition
		}
	});
}
