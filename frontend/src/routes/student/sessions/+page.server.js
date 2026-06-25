import { redirect } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    try {
        const res = await fetch(`${API_BASE}/sessions`, {
            method: "GET",
            headers
        });

        if (!res.ok) {
            console.error("Fehler beim Laden der Sessions", await res.text());
            return { sessions: [], error: "Sessions konnten nicht geladen werden" };
        }

        const sessions = await res.json();

        // KI-Kontingent (für „Noch X/3")
        let quota = null;
        try {
            const qRes = await fetch(`${API_BASE}/student/ai-quota`, { headers });
            if (qRes.ok) {
                quota = await qRes.json().catch(() => null);
            }
        } catch {
            quota = null;
        }

        return {
            sessions,
            quota
        };
    } catch (err) {
        console.error("Netzwerkfehler", err);
        return { sessions: [], error: "Verbindungsfehler" };
    }
}

export const actions = {
    // Neue Session starten
    start: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const assignmentId = formData.get("assignmentId") || null;
        const roast = formData.get("roast") === "on";

        const res = await fetch(`${API_BASE}/sessions`, {
            method: "POST",
            headers,
            body: JSON.stringify({ assignmentId, roast })
        });

        if (!res.ok) {
            return { error: "Session konnte nicht gestartet werden" };
        }

        const session = await res.json();
        
        // Redirect zur neuen Session
        throw redirect(302, `/student/sessions/${session.id}`);
    },

    // Session schliessen
    close: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const sessionId = formData.get("sessionId");

        const res = await fetch(`${API_BASE}/sessions/${sessionId}/close`, {
            method: "PUT",
            headers
        });

        if (!res.ok) {
            return { error: "Session konnte nicht geschlossen werden" };
        }

        return { success: true };
    },

    // Session löschen
    delete: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const sessionId = formData.get("sessionId");

        const res = await fetch(`${API_BASE}/sessions/${sessionId}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Session konnte nicht gelöscht werden" };
        }

        return { success: true };
    }
};
