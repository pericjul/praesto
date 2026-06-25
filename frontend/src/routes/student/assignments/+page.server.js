// frontend/src/routes/student/assignments/+page.server.js
import { redirect } from "@sveltejs/kit";

import { API_BASE, uploadFile } from "$lib/server/api.js";

export async function load({ locals, fetch }) {
    // 1) Nicht eingeloggt → Login
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const user = locals.user ?? {};
    const role = user.role;

    // 2) Nur Studenten sollen diese Seite sehen
    if (role !== "STUDENT") {
        throw redirect(302, "/dashboard");
    }

    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    let myClass = null;
    let assignments = [];
    let submissions = [];
    let loadError = null;

    // 3) Zuerst die Klasse des Schülers holen (per Email-Match im Backend)
    try {
        const classRes = await fetch(`${API_BASE}/classes/my`, {
            method: "GET",
            headers
        });

        if (classRes.ok) {
            myClass = await classRes.json();
        } else if (classRes.status === 404) {
            // Schüler ist keiner Klasse zugeordnet
            loadError = "Du bist noch keiner Klasse zugeordnet. Bitte wende dich an deine Lehrperson.";
            return { assignments: [], submissions: [], myClass: null, error: loadError };
        } else {
            loadError = "Fehler beim Laden deiner Klasse.";
            return { assignments: [], submissions: [], myClass: null, error: loadError };
        }
    } catch (err) {
        console.error("Fehler beim Laden der Klasse:", err);
        loadError = "Verbindungsfehler. Bitte versuche es später erneut.";
        return { assignments: [], submissions: [], myClass: null, error: loadError };
    }

    // 4) Assignments für die Klasse holen
    try {
        const assignmentsRes = await fetch(`${API_BASE}/assignments/class/${myClass.id}`, {
            method: "GET",
            headers
        });

        if (assignmentsRes.ok) {
            assignments = await assignmentsRes.json();
        } else {
            console.error("Fehler beim Laden der Assignments:", assignmentsRes.status);
        }
    } catch (err) {
        console.error("Fehler beim Laden der Assignments:", err);
    }

    // 5) Eigene Submissions laden
    try {
        const submissionsRes = await fetch(`${API_BASE}/submissions/my`, {
            method: "GET",
            headers
        });

        if (submissionsRes.ok) {
            submissions = await submissionsRes.json();
        } else {
            console.error("Fehler beim Laden der Submissions:", submissionsRes.status);
        }
    } catch (err) {
        console.error("Fehler beim Laden der Submissions:", err);
    }

    return {
        assignments,
        submissions,
        myClass,
        error: null
    };
}

export const actions = {
    // Dokument abgeben
    submitDocument: async ({ locals, request, fetch }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const assignmentId = formData.get("assignmentId");
        const document = formData.get("document");
        const comment = formData.get("comment");

        if (!document || document.size === 0) {
            return { error: "Bitte wähle eine Datei aus." };
        }

        let uploaded;
        try {
            uploaded = await uploadFile(document, token);
        } catch {
            return { error: "Datei-Upload fehlgeschlagen. Bitte versuche es erneut." };
        }

        const dto = {
            assignmentId,
            fileUrl: uploaded.fileUrl,
            fileName: uploaded.fileName,
            comment: comment || null
        };

        try {
            const res = await fetch(`${API_BASE}/submissions`, {
                method: "POST",
                headers,
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const text = await res.text();
                console.error("Submission error:", text);
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Dokument wurde erfolgreich abgegeben!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    },

    // Selbstreflexion abgeben
    submitReflection: async ({ locals, request, fetch }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const assignmentId = formData.get("assignmentId");
        const reflection = formData.get("reflection");

        if (!reflection || reflection.trim().length < 50) {
            return { error: "Bitte schreibe mindestens 50 Zeichen." };
        }

        const dto = {
            assignmentId,
            textContent: reflection.trim()
        };

        try {
            const res = await fetch(`${API_BASE}/submissions`, {
                method: "POST",
                headers,
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const text = await res.text();
                console.error("Submission error:", text);
                if (text.includes("bereits abgegeben")) {
                    return { error: "Du hast diese Aufgabe bereits abgegeben." };
                }
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Reflexion wurde erfolgreich abgegeben!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    },

    // Video abgeben
    submitVideo: async ({ locals, request, fetch }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const assignmentId = formData.get("assignmentId");
        const video = formData.get("video");

        if (!video || video.size === 0) {
            return { error: "Bitte wähle ein Video aus." };
        }

        let uploaded;
        try {
            uploaded = await uploadFile(video, token);
        } catch {
            return { error: "Datei-Upload fehlgeschlagen. Bitte versuche es erneut." };
        }

        const dto = {
            assignmentId,
            fileUrl: uploaded.fileUrl,
            fileName: uploaded.fileName
        };

        try {
            const res = await fetch(`${API_BASE}/submissions`, {
                method: "POST",
                headers,
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const text = await res.text();
                console.error("Submission error:", text);
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Video wurde erfolgreich hochgeladen!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    },

    // Recherche abgeben
    submitResearch: async ({ locals, request, fetch }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const assignmentId = formData.get("assignmentId");
        const researchText = formData.get("researchText");
        const linksRaw = formData.get("links");

        if (!researchText || researchText.trim().length < 50) {
            return { error: "Bitte schreibe mindestens 50 Zeichen." };
        }

        // Links parsen (eine pro Zeile)
        const links = linksRaw 
            ? linksRaw.split("\n").map(l => l.trim()).filter(l => l.length > 0)
            : [];

        const dto = {
            assignmentId,
            textContent: researchText.trim(),
            links
        };

        try {
            const res = await fetch(`${API_BASE}/submissions`, {
                method: "POST",
                headers,
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const text = await res.text();
                console.error("Submission error:", text);
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Recherche wurde erfolgreich abgegeben!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    }
};