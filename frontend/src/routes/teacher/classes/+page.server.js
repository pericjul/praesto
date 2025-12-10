import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const roles = locals.user?.user_roles ?? [];
    if (!roles.includes("TEACHER")) {
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
        return { classes };
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

    // Schüler hinzufügen (per Email)
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
        const email = formData.get("email")?.toString().trim();

        if (!email) {
            return { error: "Bitte gib eine Schüler-Email ein" };
        }

        const res = await fetch(`${API_BASE}/classes/${classId}/students`, {
            method: "POST",
            headers,
            body: JSON.stringify({ email })
        });

        if (!res.ok) {
            const text = await res.text();
            if (text.includes("bereits in der Klasse")) {
                return { error: "Dieser Schüler ist bereits in der Klasse" };
            }
            if (text.includes("Ungueltige Email")) {
                return { error: "Ungültige Email-Adresse" };
            }
            return { error: "Schüler konnte nicht hinzugefügt werden" };
        }

        return { success: true, action: "studentAdded" };
    },

    // Schüler entfernen
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
        const email = formData.get("email")?.toString();

        const res = await fetch(`${API_BASE}/classes/${classId}/students/${encodeURIComponent(email)}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Schüler konnte nicht entfernt werden" };
        }

        return { success: true, action: "studentRemoved" };
    }
};