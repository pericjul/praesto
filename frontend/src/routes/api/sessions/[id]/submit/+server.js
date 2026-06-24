// frontend/src/routes/api/sessions/[id]/submit/+server.js
import { json } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function PUT({ params, locals }) {
    if (!locals.isAuthenticated) {
        return json({ error: "Nicht eingeloggt" }, { status: 401 });
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const sessionId = params.id;

    try {
        const res = await fetch(`${API_BASE}/sessions/${sessionId}/submit`, {
            method: "PUT",
            headers
        });

        if (!res.ok) {
            const text = await res.text();
            return json({ error: text }, { status: res.status });
        }

        const session = await res.json();
        return json(session);
    } catch (err) {
        console.error("Session submit error:", err);
        return json({ error: "Verbindungsfehler" }, { status: 500 });
    }
}