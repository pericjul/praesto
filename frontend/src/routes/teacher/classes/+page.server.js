import { redirect } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const role = locals.user?.role;
    if (role !== "TEACHER") {
        throw redirect(302, "/");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    try {
        const res = await fetch(`${API_BASE}/classes`, { method: "GET", headers });

        if (!res.ok) {
            console.error("Fehler beim Laden der Klassen", await res.text());
            return { classes: [], error: "Klassen konnten nicht geladen werden" };
        }

        const classes = await res.json();

        // studentIds → Schüler-Details auflösen (für Namens-Anzeige)
        const ids = [...new Set(classes.flatMap((c) => c.studentIds ?? []))];
        const idToUser = {};
        await Promise.all(
            ids.map(async (id) => {
                const r = await fetch(`${API_BASE}/users/${id}`, { headers });
                if (r.ok) {
                    idToUser[id] = await r.json();
                }
            })
        );

        const enriched = classes.map((c) => ({
            ...c,
            students: (c.studentIds ?? []).map(
                (id) => idToUser[id] ?? { id, firstName: "?", lastName: "", email: id }
            )
        }));

        return { classes: enriched };
    } catch (err) {
        console.error("Netzwerkfehler", err);
        return { classes: [], error: "Verbindungsfehler" };
    }
}

export const actions = {
    // Neue Klasse erstellen
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
        const dto = {
            name: formData.get("name")?.toString().trim() || null
        };

        if (!dto.name) {
            return { error: "Bitte gib einen Klassennamen ein" };
        }

        const res = await fetch(`${API_BASE}/classes`, {
            method: "POST",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const text = await res.text();
            if (text.includes("existiert bereits")) {
                return { error: "Eine Klasse mit diesem Namen existiert bereits" };
            }
            return { error: "Klasse konnte nicht erstellt werden" };
        }

        return { success: true, action: "created" };
    },

    // Klasse bearbeiten
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
        const classId = formData.get("classId")?.toString();
        const dto = {
            name: formData.get("name")?.toString().trim() || null
        };

        if (!dto.name) {
            return { error: "Bitte gib einen Klassennamen ein" };
        }

        const res = await fetch(`${API_BASE}/classes/${classId}`, {
            method: "PUT",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            return { error: "Klasse konnte nicht aktualisiert werden" };
        }

        return { success: true, action: "updated" };
    },

    // Klasse löschen
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
        const classId = formData.get("classId")?.toString();

        const res = await fetch(`${API_BASE}/classes/${classId}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Klasse konnte nicht gelöscht werden" };
        }

        return { success: true, action: "deleted" };
    },

    // Schüler hinzufügen (per User-Id, via Suchfeld ausgewählt)
    addStudent: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const classId = formData.get("classId")?.toString();
        const userId = formData.get("userId")?.toString().trim();

        if (!userId) {
            return { error: "Bitte wähle einen Schüler aus" };
        }

        const res = await fetch(`${API_BASE}/classes/${classId}/students`, {
            method: "POST",
            headers,
            body: JSON.stringify({ userId })
        });

        if (!res.ok) {
            const text = await res.text();
            if (text.includes("bereits in der Klasse")) {
                return { error: "Dieser Schüler ist bereits in der Klasse" };
            }
            if (text.includes("gehört nicht zu deiner Schule")) {
                return { error: "Dieser Schüler gehört nicht zu deiner Schule" };
            }
            return { error: "Schüler konnte nicht hinzugefügt werden" };
        }

        return { success: true, action: "studentAdded" };
    },

    // Schüler entfernen (per User-Id)
    removeStudent: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const classId = formData.get("classId")?.toString();
        const userId = formData.get("userId")?.toString();

        const res = await fetch(`${API_BASE}/classes/${classId}/students/${encodeURIComponent(userId)}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Schüler konnte nicht entfernt werden" };
        }

        return { success: true, action: "studentRemoved" };
    }
};