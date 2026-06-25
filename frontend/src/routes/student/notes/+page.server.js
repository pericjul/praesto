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
        const res = await fetch(`${API_BASE}/notes`, {
            method: "GET",
            headers
        });

        if (!res.ok) {
            console.error("Fehler beim Laden der Notizen", await res.text());
            return { notes: [], error: "Notizen konnten nicht geladen werden" };
        }

        const notes = await res.json();
        return { notes };
    } catch (err) {
        console.error("Netzwerkfehler", err);
        return { notes: [], error: "Verbindungsfehler" };
    }
}

export const actions = {
    // Neue Notiz erstellen
    create: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const companyName = formData.get("companyName")?.toString().trim() || null;
        const position = formData.get("position")?.toString().trim() || null;
        const text = formData.get("text")?.toString().trim();

        if (!text) {
            return { error: "Bitte gib einen Notiztext ein" };
        }

        const res = await fetch(`${API_BASE}/notes`, {
            method: "POST",
            headers,
            body: JSON.stringify({ companyName, position, text })
        });

        if (!res.ok) {
            return { error: "Notiz konnte nicht erstellt werden" };
        }

        return { success: true, action: "created" };
    },

    // Notiz bearbeiten
    update: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const noteId = formData.get("noteId")?.toString();
        const companyName = formData.get("companyName")?.toString().trim() || null;
        const position = formData.get("position")?.toString().trim() || null;
        const text = formData.get("text")?.toString().trim();

        if (!text) {
            return { error: "Bitte gib einen Notiztext ein" };
        }

        const res = await fetch(`${API_BASE}/notes/${noteId}`, {
            method: "PUT",
            headers,
            body: JSON.stringify({ companyName, position, text })
        });

        if (!res.ok) {
            return { error: "Notiz konnte nicht aktualisiert werden" };
        }

        return { success: true, action: "updated" };
    },

    // Notiz löschen
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
        const noteId = formData.get("noteId")?.toString();

        const res = await fetch(`${API_BASE}/notes/${noteId}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Notiz konnte nicht gelöscht werden" };
        }

        return { success: true, action: "deleted" };
    }
};