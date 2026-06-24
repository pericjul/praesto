// frontend/src/routes/teacher/sessions/[id]/+page.server.js
import { redirect, fail } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch, params }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const user = locals.user ?? {};
    const role = user.role;

    if (role !== "TEACHER") {
        throw redirect(302, "/dashboard");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    const sessionId = params.id;
    let session = null;
    let student = null;

    // Session laden
    try {
        const sessionRes = await fetch(`${API_BASE}/sessions/${sessionId}`, {
            method: "GET",
            headers
        });

        if (sessionRes.ok) {
            session = await sessionRes.json();
        } else {
            console.error("Session nicht gefunden:", sessionId);
        }
    } catch (err) {
        console.error("Fehler beim Laden der Session:", err);
    }

    // Wenn Session geladen, versuche Student-Info zu laden
    if (session?.studentId) {
        try {
            const studentRes = await fetch(`${API_BASE}/users/${session.studentId}`, {
                method: "GET",
                headers
            });

            if (studentRes.ok) {
                student = await studentRes.json();
            }
        } catch (err) {
            console.error("Fehler beim Laden des Studenten:", err);
        }
    }

    // Wenn Session eine Assignment-Session ist, lade zusätzliche Infos
    if (session?.assignmentId) {
        try {
            const assignmentRes = await fetch(`${API_BASE}/assignments/${session.assignmentId}`, {
                method: "GET",
                headers
            });

            if (assignmentRes.ok) {
                const assignment = await assignmentRes.json();
                session.assignmentTitle = assignment.title;
            }
        } catch (err) {
            console.error("Fehler beim Laden der Aufgabe:", err);
        }

        // Submission-Infos laden (für Feedback/Note)
        try {
            const submissionsRes = await fetch(`${API_BASE}/submissions/assignment/${session.assignmentId}`, {
                method: "GET",
                headers
            });

            if (submissionsRes.ok) {
                const submissions = await submissionsRes.json();
                const submission = submissions.find(s => s.chatSessionId === sessionId);
                if (submission) {
                    session.submissionId = submission.id;
                    session.teacherFeedback = submission.teacherFeedback;
                    session.grade = submission.grade;
                    session.submittedAsAssignment = submission.status === "SUBMITTED" || 
                                                    submission.status === "REVIEWED" || 
                                                    submission.status === "IN_REVIEW";
                    
                    // Student-Email aus Submission
                    if (!student) {
                        student = { email: submission.studentEmail };
                    }
                }
            }
        } catch (err) {
            console.error("Fehler beim Laden der Submission:", err);
        }
    }

    return {
        session,
        student
    };
}

export const actions = {
    saveFeedback: async ({ request, locals, fetch, params }) => {
        if (!locals.isAuthenticated) {
            throw redirect(302, "/login");
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const feedback = formData.get("feedback")?.toString() ?? "";
        const gradeStr = formData.get("grade")?.toString() ?? "";
        const grade = gradeStr ? parseFloat(gradeStr) : null;

        const sessionId = params.id;

        // Zuerst Session laden um submissionId zu bekommen
        let session = null;
        try {
            const sessionRes = await fetch(`${API_BASE}/sessions/${sessionId}`, {
                method: "GET",
                headers
            });

            if (sessionRes.ok) {
                session = await sessionRes.json();
            }
        } catch (err) {
            return fail(500, { error: "Session konnte nicht geladen werden" });
        }

        if (!session?.assignmentId) {
            return fail(400, { error: "Keine Aufgabe mit dieser Session verknüpft" });
        }

        // Submission finden
        let submissionId = null;
        try {
            const submissionsRes = await fetch(`${API_BASE}/submissions/assignment/${session.assignmentId}`, {
                method: "GET",
                headers
            });

            if (submissionsRes.ok) {
                const submissions = await submissionsRes.json();
                const submission = submissions.find(s => s.chatSessionId === sessionId);
                if (submission) {
                    submissionId = submission.id;
                }
            }
        } catch (err) {
            return fail(500, { error: "Submission konnte nicht gefunden werden" });
        }

        if (!submissionId) {
            return fail(400, { error: "Keine Abgabe gefunden" });
        }

        // Feedback speichern
        try {
            const res = await fetch(`${API_BASE}/submissions/${submissionId}/feedback`, {
                method: "PUT",
                headers,
                body: JSON.stringify({
                    teacherFeedback: feedback,
                    grade: grade
                })
            });

            if (!res.ok) {
                const errorText = await res.text();
                return fail(res.status, { error: errorText || "Feedback konnte nicht gespeichert werden" });
            }

            return { success: true, message: "Feedback gespeichert!" };
        } catch (err) {
            console.error("Fehler beim Speichern:", err);
            return fail(500, { error: "Verbindungsfehler" });
        }
    }
};
