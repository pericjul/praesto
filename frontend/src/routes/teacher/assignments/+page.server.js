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
        // Aufgaben und Klassen parallel laden
        const [assignmentsRes, classesRes] = await Promise.all([
            fetch(`${API_BASE}/assignments/teacher`, { method: "GET", headers }),
            fetch(`${API_BASE}/classes`, { method: "GET", headers })
        ]);

        const assignments = assignmentsRes.ok ? await assignmentsRes.json() : [];
        const classes = classesRes.ok ? await classesRes.json() : [];

        // Für jede Aufgabe: Submissions laden und Stats berechnen
        const assignmentsWithStats = await Promise.all(
            assignments.map(async (assignment) => {
                // Klasse finden für Schüleranzahl
                const schoolClass = classes.find(c => c.id === assignment.classId);
                const totalStudents = schoolClass?.studentIds?.length ?? 0;

                // Submissions für diese Aufgabe laden
                let submissionCount = 0;
                let reviewedCount = 0;
                let pendingFeedback = 0;

                try {
                    const subsRes = await fetch(`${API_BASE}/submissions/assignment/${assignment.id}`, {
                        method: "GET",
                        headers
                    });

                    if (subsRes.ok) {
                        const submissions = await subsRes.json();
                        submissionCount = submissions.length;
                        reviewedCount = submissions.filter(s => s.status === "REVIEWED").length;
                        pendingFeedback = submissions.filter(s => s.status === "SUBMITTED").length;
                    }
                } catch (err) {
                    console.error("Fehler beim Laden der Submissions:", err);
                }

                // Status berechnen
                const now = new Date();
                const dueDate = new Date(assignment.dueDate);
                const isOverdue = dueDate < now;
                const isComplete = submissionCount === totalStudents && totalStudents > 0;

                return {
                    ...assignment,
                    totalStudents,
                    submissionCount,
                    reviewedCount,
                    pendingFeedback,
                    missingCount: totalStudents - submissionCount,
                    isOverdue,
                    isComplete,
                    className: schoolClass?.name ?? "Unbekannt"
                };
            })
        );

        // Sortieren: Offene zuerst, dann nach Deadline
        assignmentsWithStats.sort((a, b) => {
            // Priorität: Offene mit ausstehenden Abgaben/Feedback zuerst
            if (a.pendingFeedback > 0 && b.pendingFeedback === 0) return -1;
            if (b.pendingFeedback > 0 && a.pendingFeedback === 0) return 1;
            
            // Dann nach Deadline (bald fällige zuerst)
            return new Date(a.dueDate) - new Date(b.dueDate);
        });

        return { 
            assignments: assignmentsWithStats, 
            classes 
        };
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

        if (!title) return { error: "Titel ist erforderlich" };
        if (!classId) return { error: "Bitte wähle eine Klasse aus" };
        if (!type) return { error: "Bitte wähle einen Aufgabentyp aus" };
        if (!dueDate) return { error: "Deadline ist erforderlich" };

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

        if (!title) return { error: "Titel ist erforderlich" };
        if (!classId) return { error: "Bitte wähle eine Klasse aus" };
        if (!type) return { error: "Bitte wähle einen Aufgabentyp aus" };
        if (!dueDate) return { error: "Deadline ist erforderlich" };

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