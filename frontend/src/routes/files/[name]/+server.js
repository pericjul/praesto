import { error } from "@sveltejs/kit";
import { API_BASE, apiHeaders } from "$lib/server/api.js";

// Download-Proxy: holt die Datei mit dem httpOnly-JWT vom Backend und streamt sie
// an den Browser zurück. So funktioniert ein normaler Download-Link (gleicher
// Origin, Cookie vorhanden), obwohl das Backend einen Bearer-Header verlangt.
export async function GET({ params, locals, fetch }) {
	if (!locals.isAuthenticated) {
		throw error(401, "Nicht angemeldet");
	}

	const res = await fetch(`${API_BASE}/files/${encodeURIComponent(params.name)}`, {
		headers: apiHeaders(locals.jwt_token)
	});

	if (!res.ok) {
		throw error(res.status, "Datei nicht gefunden");
	}

	return new Response(res.body, {
		status: 200,
		headers: {
			"Content-Type": res.headers.get("content-type") ?? "application/octet-stream",
			"Content-Disposition": res.headers.get("content-disposition") ?? "attachment"
		}
	});
}
