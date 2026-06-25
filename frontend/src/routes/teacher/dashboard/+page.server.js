// frontend/src/routes/teacher/dashboard/+page.server.js
import { redirect } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
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

    let classes = [];
    let assignments = [];
    let recentSubmissions = [];
    let stats = {
        totalClasses: 0,
        totalStudents: 0,
        totalAssignments: 0,
        pendingSubmissions: 0
    };

    // Klassen laden
    try {
        const classesRes = await fetch(`${API_BASE}/classes`, {
            method: "GET",
            headers
        });

        if (classesRes.ok) {
            classes = await classesRes.json();
            stats.totalClasses = classes.length;
            stats.totalStudents = classes.reduce((sum, c) => sum + (c.studentIds?.length ?? 0), 0);
        }
    } catch (err) {
        console.error("Fehler beim Laden der Klassen:", err);
    }

    // Assignments laden
    try {
        const assignmentsRes = await fetch(`${API_BASE}/assignments/teacher`, {
            method: "GET",
            headers
        });

        if (assignmentsRes.ok) {
            assignments = await assignmentsRes.json();
            stats.totalAssignments = assignments.length;
        }
    } catch (err) {
        console.error("Fehler beim Laden der Aufgaben:", err);
    }

    // Submissions für ALLE Assignments laden (korrekte Pending-Zahl über alle Aufgaben)
    for (const assignment of assignments) {
        try {
            const submissionsRes = await fetch(`${API_BASE}/submissions/assignment/${assignment.id}`, {
                method: "GET",
                headers
            });

            if (submissionsRes.ok) {
                const subs = await submissionsRes.json();
                const pending = subs.filter(s => s.status === "SUBMITTED");
                stats.pendingSubmissions += pending.length;
                
                // Neueste Submissions sammeln
                recentSubmissions.push(...subs.map(s => ({
                    ...s,
                    assignmentId: assignment.id,
                    assignmentTitle: assignment.title,
                    assignmentType: assignment.type
                })));
            }
        } catch (err) {
            console.error("Fehler beim Laden der Submissions:", err);
        }
    }

    // Nach Datum sortieren und nur die neuesten 10
    recentSubmissions.sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt));
    recentSubmissions = recentSubmissions.slice(0, 10);

    // Kalender-Termine (Aufgaben-Deadlines aller Klassen)
    let calendar = [];
    try {
        const calRes = await fetch(`${API_BASE}/teacher/calendar`, { headers });
        if (calRes.ok) {
            calendar = await calRes.json().catch(() => []);
        }
    } catch (err) {
        console.error("Fehler beim Laden des Kalenders:", err);
    }

    return {
        classes,
        assignments,
        recentSubmissions,
        stats,
        calendar,
        user
    };
}