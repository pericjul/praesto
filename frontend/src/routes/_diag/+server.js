// Temporärer Diagnose-Endpunkt. Wird nach dem Debuggen entfernt.
import { API_BASE } from "$lib/server/api.js";

export async function GET({ locals, fetch }) {
    const token = locals.jwt_token;
    const auth = token ? { Authorization: `Bearer ${token}` } : {};
    let viaEvent = null;
    let viaGlobal = null;
    try {
        viaEvent = (await fetch(`${API_BASE}/badges/my`, { headers: auth })).status;
    } catch (e) {
        viaEvent = "err:" + e;
    }
    try {
        viaGlobal = (await globalThis.fetch("http://localhost:8080/api/badges/my", { headers: auth })).status;
    } catch (e) {
        viaGlobal = "err:" + e;
    }
    return new Response(
        JSON.stringify({
            build: "diag-v10",
            hasToken: Boolean(token),
            viaEvent,   // durch handleFetch (mit Token-Fix)
            viaGlobal   // direkt ans Backend
        }),
        { headers: { "content-type": "application/json" } }
    );
}
