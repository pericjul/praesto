import { redirect } from "@sveltejs/kit";
import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
    if (!locals.isAuthenticated || locals.user?.role !== "SUPER_ADMIN") {
        throw redirect(302, "/login");
    }
    const token = locals.jwt_token;
    let logs = [];
    let error = null;
    try {
        const res = await fetch(`${API_BASE}/super/logs`, {
            headers: token ? { Authorization: `Bearer ${token}` } : {}
        });
        if (res.ok) {
            logs = await res.json();
        } else {
            error = `Konnte Logs nicht laden (${res.status})`;
        }
    } catch (e) {
        error = "Verbindungsfehler beim Laden der Logs";
    }
    return { logs, error };
}
