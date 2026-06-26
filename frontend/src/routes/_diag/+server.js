// Temporärer Diagnose-Endpunkt: zeigt, ob der Frontend-Server das Backend
// erreicht und das Token korrekt weiterreicht. Wird nach dem Debuggen entfernt.
import { API_BASE } from "$lib/server/api.js";

export async function GET({ locals, fetch }) {
    const token = locals.jwt_token;
    let badgeStatus = null;
    let err = null;
    try {
        const r = await fetch(`${API_BASE}/badges/my`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        badgeStatus = r.status;
    } catch (e) {
        err = String(e);
    }
    return new Response(
        JSON.stringify({
            build: "diag-v1",
            apiBase: API_BASE,
            internalApi: process.env.INTERNAL_API_BASE ?? "http://localhost:8080",
            hasToken: Boolean(token),
            tokenLen: token ? token.length : 0,
            badgeStatus,
            err
        }),
        { headers: { "content-type": "application/json" } }
    );
}
