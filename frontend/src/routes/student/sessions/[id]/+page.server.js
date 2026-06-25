import { redirect, error } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch, params }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const sessionId = params.id;

    try {
        const res = await fetch(`${API_BASE}/sessions/${sessionId}`, {
            method: "GET",
            headers
        });

        if (res.status === 404) {
            throw error(404, "Session nicht gefunden");
        }

        if (res.status === 403) {
            throw error(403, "Keine Berechtigung für diese Session");
        }

        if (!res.ok) {
            throw error(res.status, "Fehler beim Laden der Session");
        }

        const session = await res.json();

        return {
            session
        };
    } catch (err) {
        if (err.status) throw err;
        console.error("Netzwerkfehler", err);
        throw error(500, "Verbindungsfehler");
    }
}

export const actions = {
    // Nachricht senden
    send: async ({ locals, fetch, request, params }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const message = formData.get("message")?.toString().trim();

        if (!message) {
            return { error: "Bitte gib eine Nachricht ein" };
        }

        const sessionId = params.id;

        const res = await fetch(`${API_BASE}/sessions/${sessionId}/messages`, {
            method: "POST",
            headers,
            body: JSON.stringify({ message })
        });

        if (!res.ok) {
            const text = await res.text();
            console.error("Fehler beim Senden", text);
            return { error: "Nachricht konnte nicht gesendet werden" };
        }

        // Session wird automatisch neu geladen durch SvelteKit
        return { success: true };
    },

    // Session schliessen
    close: async ({ locals, fetch, params }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const sessionId = params.id;

        const res = await fetch(`${API_BASE}/sessions/${sessionId}/close`, {
            method: "PUT",
            headers
        });

        if (!res.ok) {
            return { error: "Session konnte nicht geschlossen werden" };
        }

        // Redirect zur Session-Übersicht
        throw redirect(302, "/student/sessions");
    }
};
