// Server-Proxy für die Schüler-Suche: der Browser kann das JWT (httpOnly-Cookie)
// nicht als Bearer-Header mitschicken. Darum läuft die Suche serverseitig mit
// Token gegen das Backend.
import { json } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export async function GET({ locals, fetch, url }) {
    if (!locals.isAuthenticated) {
        return json([], { status: 401 });
    }
    const q = url.searchParams.get("q") ?? "";
    const token = locals.jwt_token;
    try {
        const res = await fetch(`${API_BASE}/users/students/search?q=${encodeURIComponent(q)}`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        if (!res.ok) return json([]);
        return json(await res.json());
    } catch {
        return json([]);
    }
}
