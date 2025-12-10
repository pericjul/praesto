// frontend/src/routes/teacher/sessions/+page.server.js
import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch }) {
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

    let sessions = [];
    let assignments = [];

    // Assignments des Lehrers laden (für AI_INTERVIEW Filter)
    try {
        const assignmentsRes = await fetch(`${API_BASE}/assignments/teacher`, {
            method: "GET",
            headers
        });

        if (assignmentsRes.ok) {
            const allAssignments = await assignmentsRes.json();
            // Nur AI_INTERVIEW Aufgaben
            assignments = allAssignments.filter(a => a.type === "AI_INTERVIEW");
        }
    } catch (err) {
        console.error("Fehler beim Laden der Aufgaben:", err);
    }

    // Sessions für jede AI_INTERVIEW Aufgabe laden
    for (const assignment of assignments) {
        try {
            // Submissions für diese Aufgabe laden
            const submissionsRes = await fetch(`${API_BASE}/submissions/assignment/${assignment.id}`, {
                method: "GET",
                headers
            });

            if (submissionsRes.ok) {
                const submissions = await submissionsRes.json();
                
                // Für jede Submission mit chatSessionId die Session-Details holen
                for (const sub of submissions) {
                    if (sub.chatSessionId) {
                        try {
                            const sessionRes = await fetch(`${API_BASE}/sessions/${sub.chatSessionId}`, {
                                method: "GET",
                                headers
                            });

                            if (sessionRes.ok) {
                                const session = await sessionRes.json();
                                sessions.push({
                                    ...session,
                                    studentEmail: sub.studentEmail,
                                    assignmentTitle: assignment.title,
                                    submissionId: sub.id,
                                    hasFeedback: !!sub.teacherFeedback,
                                    grade: sub.grade
                                });
                            }
                        } catch (err) {
                            console.error("Fehler beim Laden der Session:", err);
                        }
                    }
                }
            }
        } catch (err) {
            console.error("Fehler beim Laden der Submissions:", err);
        }
    }

    // Nach Datum sortieren (neueste zuerst)
    sessions.sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt));

    return {
        sessions,
        assignments
    };
}