// frontend/src/routes/teacher/sessions/[id]/+page.server.js
import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch, params }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const user = locals.user ?? {};
    const roles = user.user_roles ?? [];

    if (!roles.includes("TEACHER")) {
        throw redirect(302, "/dashboard");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const sessionId = params.id;

    try {
        const res = await fetch(`${API_BASE}/sessions/${sessionId}`, { headers });
        if (res.ok) {
            const session = await res.json();
            return { session, error: null };
        } else if (res.status === 404) {
            return { session: null, error: "Session nicht gefunden" };
        } else {
            return { session: null, error: "Fehler beim Laden" };
        }
    } catch (err) {
        console.error("Error loading session:", err);
        return { session: null, error: "Verbindungsfehler" };
    }
}