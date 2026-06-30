// frontend/src/routes/api/sessions/start/+server.js
import { json } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function POST({ request, locals }) {
    if (!locals.isAuthenticated) {
        return json({ error: "Nicht eingeloggt" }, { status: 401 });
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        // UI-Sprache mitgeben, damit die KI-Begruessung in der Sprache der
        // Schueler:in kommt (dieser Endpoint umgeht handleFetch -> Header explizit).
        ...(locals.lang ? { "X-Locale": locals.lang } : {})
    };

    try {
        const body = await request.json();
        
        const res = await fetch(`${API_BASE}/sessions`, {
            method: "POST",
            headers,
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const text = await res.text();
            return json({ error: text }, { status: res.status });
        }

        const session = await res.json();
        return json(session);
    } catch (err) {
        console.error("Session start error:", err);
        return json({ error: "Verbindungsfehler" }, { status: 500 });
    }
}