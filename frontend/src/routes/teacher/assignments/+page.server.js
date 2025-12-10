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
        // Aufgaben und Klassen parallel laden
        const [assignmentsRes, classesRes] = await Promise.all([
            fetch(`${API_BASE}/assignments/teacher`, { method: "GET", headers }),
            fetch(`${API_BASE}/classes`, { method: "GET", headers })
        ]);

        const assignments = assignmentsRes.ok ? await assignmentsRes.json() : [];
        const classes = classesRes.ok ? await classesRes.json() : [];

        return { assignments, classes };
    } catch (err) {
        console.error("Netzwerkfehler", err);
        return { assignments: [], classes: [], error: "Verbindungsfehler" };
    }
}

export const actions = {
    // Neue Aufgabe erstellen
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
        
        const title = formData.get("title")?.toString().trim();
        const description = formData.get("description")?.toString().trim() || null;
        const durationMin = formData.get("durationMin")?.toString();
        const dueDate = formData.get("dueDate")?.toString();
        const classId = formData.get("classId")?.toString();
        const type = formData.get("type")?.toString();

        // Validierung
        if (!title) {
            return { error: "Titel ist erforderlich" };
        }
        if (!classId) {
            return { error: "Bitte wähle eine Klasse aus" };
        }
        if (!type) {
            return { error: "Bitte wähle einen Aufgabentyp aus" };
        }
        if (!dueDate) {
            return { error: "Deadline ist erforderlich" };
        }

        // Deadline in Zukunft prüfen
        const deadlineDate = new Date(dueDate);
        if (deadlineDate <= new Date()) {
            return { error: "Deadline muss in der Zukunft liegen" };
        }

        const dto = {
            title,
            description,
            durationMin: durationMin ? parseInt(durationMin) : null,
            dueDate: deadlineDate.toISOString(),
            classId,
            type
        };

        const res = await fetch(`${API_BASE}/assignment`, {
            method: "POST",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const text = await res.text();
            console.error("Fehler beim Erstellen:", text);
            return { error: "Aufgabe konnte nicht erstellt werden" };
        }

        return { success: true, action: "created" };
    },

    // Aufgabe löschen
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
        const assignmentId = formData.get("assignmentId")?.toString();

        const res = await fetch(`${API_BASE}/assignments/${assignmentId}`, {
            method: "DELETE",
            headers
        });

        if (!res.ok) {
            return { error: "Aufgabe konnte nicht gelöscht werden" };
        }

        return { success: true, action: "deleted" };
    },

    // Aufgabe bearbeiten
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
        
        const assignmentId = formData.get("assignmentId")?.toString();
        const title = formData.get("title")?.toString().trim();
        const description = formData.get("description")?.toString().trim() || null;
        const durationMin = formData.get("durationMin")?.toString();
        const dueDate = formData.get("dueDate")?.toString();
        const classId = formData.get("classId")?.toString();
        const type = formData.get("type")?.toString();

        // Validierung
        if (!title) {
            return { error: "Titel ist erforderlich" };
        }
        if (!classId) {
            return { error: "Bitte wähle eine Klasse aus" };
        }
        if (!type) {
            return { error: "Bitte wähle einen Aufgabentyp aus" };
        }
        if (!dueDate) {
            return { error: "Deadline ist erforderlich" };
        }

        const deadlineDate = new Date(dueDate);

        const dto = {
            title,
            description,
            durationMin: durationMin ? parseInt(durationMin) : null,
            dueDate: deadlineDate.toISOString(),
            classId,
            type
        };

        const res = await fetch(`${API_BASE}/assignments/${assignmentId}`, {
            method: "PUT",
            headers,
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const text = await res.text();
            console.error("Fehler beim Aktualisieren:", text);
            return { error: "Aufgabe konnte nicht aktualisiert werden" };
        }

        return { success: true, action: "updated" };
    }
};