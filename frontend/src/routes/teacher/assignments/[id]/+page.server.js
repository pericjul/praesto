// frontend/src/routes/teacher/assignments/[id]/+page.server.js
import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch, params }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const user = locals.user ?? {};
    const roles = user.user_roles ?? [];

    if (!roles.includes("TEACHER")) {
        throw redirect(302, "/dashboard");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const assignmentId = params.id;
    let assignment = null;
    let submissions = [];
    let schoolClass = null;

    // Assignment laden
    try {
        const res = await fetch(`${API_BASE}/assignments/${assignmentId}`, { headers });
        if (res.ok) {
            assignment = await res.json();
        } else if (res.status === 404) {
            return { error: "Aufgabe nicht gefunden" };
        } else {
            return { error: "Fehler beim Laden der Aufgabe" };
        }
    } catch (err) {
        console.error("Error loading assignment:", err);
        return { error: "Verbindungsfehler" };
    }

    // Submissions laden
    try {
        const res = await fetch(`${API_BASE}/submissions/assignment/${assignmentId}`, { headers });
        if (res.ok) {
            submissions = await res.json();
        }
    } catch (err) {
        console.error("Error loading submissions:", err);
    }

    // Klasse laden für Schülerliste
    try {
        const res = await fetch(`${API_BASE}/classes/${assignment.classId}`, { headers });
        if (res.ok) {
            schoolClass = await res.json();
        }
    } catch (err) {
        console.error("Error loading class:", err);
    }

    return {
        assignment,
        submissions,
        schoolClass,
        error: null
    };
}

export const actions = {
    // Feedback geben
    feedback: async ({ locals, fetch, request, params }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const submissionId = formData.get("submissionId");
        const feedback = formData.get("feedback");
        const grade = formData.get("grade");

        try {
            const res = await fetch(`${API_BASE}/submissions/${submissionId}/feedback`, {
                method: "PUT",
                headers,
                body: JSON.stringify({
                    feedback: feedback || null,
                    grade: grade ? parseInt(grade) : null
                })
            });

            if (!res.ok) {
                return { error: "Feedback konnte nicht gespeichert werden" };
            }

            return { success: true, message: "Feedback gespeichert!" };
        } catch (err) {
            console.error("Error saving feedback:", err);
            return { error: "Verbindungsfehler" };
        }
    }
};