import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

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
        const [applicationsRes, statsRes] = await Promise.all([
            fetch(`${API_BASE}/applications`, { method: "GET", headers }),
            fetch(`${API_BASE}/applications/stats`, { method: "GET", headers })
        ]);

        if (!applicationsRes.ok) {
            console.error("Fehler beim Laden der Bewerbungen", await applicationsRes.text());
            return { applications: [], stats: null, error: "Bewerbungen konnten nicht geladen werden" };
        }

        const applications = await applicationsRes.json();
        const stats = statsRes.ok ? await statsRes.json() : null;

        return { applications, stats };
    } catch (err) {
        console.error("Netzwerkfehler", err);
        return { applications: [], stats: null, error: "Verbindungsfehler" };
    }
}

export const actions = {
    // Neue Bewerbung erstellen
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
            companyName: formData.get("companyName")?.toString().trim() || null,
            position: formData.get("position")?.toString().trim() || null,
            status: formData.get("status")?.toString() || "PLANNED",
            appliedAt: formData.get("appliedAt")?.toString() || null,
            interviewDate: formData.get("interviewDate")?.toString() || null,
            notes: formData.get("notes")?.toString().trim() || null
        };

        if (!dto.companyName) {
            return { error: "Bitte gib einen Firmennamen ein" };
        }

        const res = await fetch(`${API_BASE}/applications`, {
            method: "POST",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            return { error: "Bewerbung konnte nicht erstellt werden" };
        }

        return { success: true, action: "created" };
    },

    // Bewerbung bearbeiten
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
        const applicationId = formData.get("applicationId")?.toString();
        const dto = {
            companyName: formData.get("companyName")?.toString().trim() || null,
            position: formData.get("position")?.toString().trim() || null,
            status: formData.get("status")?.toString() || "PLANNED",
            appliedAt: formData.get("appliedAt")?.toString() || null,
            interviewDate: formData.get("interviewDate")?.toString() || null,
            notes: formData.get("notes")?.toString().trim() || null
        };

        if (!dto.companyName) {
            return { error: "Bitte gib einen Firmennamen ein" };
        }

        const res = await fetch(`${API_BASE}/applications/${applicationId}`, {
            method: "PUT",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            return { error: "Bewerbung konnte nicht aktualisiert werden" };
        }

        return { success: true, action: "updated" };
    },

    // Status ändern
    updateStatus: async ({ locals, fetch, request }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const applicationId = formData.get("applicationId")?.toString();
        const status = formData.get("status")?.toString();

        const res = await fetch(`${API_BASE}/applications/${applicationId}/status`, {
            method: "PUT",
            headers,
            body: JSON.stringify({ status })
        });

        if (!res.ok) {
            return { error: "Status konnte nicht geändert werden" };
        }

        return { success: true, action: "statusUpdated" };
    },

    // Bewerbung löschen
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
        const applicationId = formData.get("applicationId")?.toString();

        const res = await fetch(`${API_BASE}/applications/${applicationId}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Bewerbung konnte nicht gelöscht werden" };
        }

        return { success: true, action: "deleted" };
    }
};