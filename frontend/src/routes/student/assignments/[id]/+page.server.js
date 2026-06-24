import { redirect, error } from "@sveltejs/kit";

import { API_BASE, uploadFile } from "$lib/server/api.js";

export async function load({ locals, fetch, params }) {
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

    const assignmentId = params.id;
    const token = locals.jwt_token;
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    let assignment = null;
    let submission = null;
    let session = null;

    // 3) Assignment laden
    try {
        const res = await fetch(`${API_BASE}/assignments/${assignmentId}`, { headers });
        
        if (res.status === 404) {
            throw error(404, "Aufgabe nicht gefunden");
        }
        
        if (!res.ok) {
            throw error(500, "Fehler beim Laden der Aufgabe");
        }
        
        assignment = await res.json();
    } catch (err) {
        if (err.status) throw err;
        console.error("Fehler beim Laden der Aufgabe:", err);
        throw error(500, "Verbindungsfehler");
    }

    // 4) Prüfen ob bereits abgegeben
    try {
        const checkRes = await fetch(`${API_BASE}/submissions/check/${assignmentId}`, { headers });
        
        if (checkRes.ok) {
            const checkData = await checkRes.json();
            if (checkData.hasSubmitted && checkData.submission?.id) {
                submission = checkData.submission;
                
                // Falls AI_INTERVIEW: Session laden
                if (submission.chatSessionId) {
                    try {
                        const sessionRes = await fetch(`${API_BASE}/sessions/${submission.chatSessionId}`, { headers });
                        if (sessionRes.ok) {
                            session = await sessionRes.json();
                        }
                    } catch (e) {
                        console.error("Session konnte nicht geladen werden:", e);
                    }
                }
            }
        }
    } catch (err) {
        console.error("Fehler beim Prüfen der Submission:", err);
    }

    return {
        assignment,
        submission,
        session
    };
}

export const actions = {
    // Selbstreflexion abgeben
    submitReflection: async ({ locals, request, fetch, params }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const reflection = formData.get("reflection");

        if (!reflection || reflection.trim().length < 50) {
            return { error: "Bitte schreibe mindestens 50 Zeichen." };
        }

        const dto = {
            assignmentId: params.id,
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

    // Recherche abgeben
    submitResearch: async ({ locals, request, fetch, params }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
        const researchText = formData.get("researchText");
        const linksRaw = formData.get("links");

        if (!researchText || researchText.trim().length < 50) {
            return { error: "Bitte schreibe mindestens 50 Zeichen." };
        }

        const links = linksRaw 
            ? linksRaw.split("\n").map(l => l.trim()).filter(l => l.length > 0)
            : [];

        const dto = {
            assignmentId: params.id,
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
                if (text.includes("bereits abgegeben")) {
                    return { error: "Du hast diese Aufgabe bereits abgegeben." };
                }
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Recherche wurde erfolgreich abgegeben!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    },

    // Dokument abgeben
    submitDocument: async ({ locals, request, fetch, params }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
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
            assignmentId: params.id,
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
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Dokument wurde erfolgreich abgegeben!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    },

    // Video abgeben
    submitVideo: async ({ locals, request, fetch, params }) => {
        if (!locals.isAuthenticated) {
            return { error: "Nicht eingeloggt" };
        }

        const token = locals.jwt_token;
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        };

        const formData = await request.formData();
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
            assignmentId: params.id,
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
                return { error: "Abgabe fehlgeschlagen. Bitte versuche es erneut." };
            }

            return { success: true, message: "Video wurde erfolgreich hochgeladen!" };
        } catch (err) {
            console.error("Submission error:", err);
            return { error: "Verbindungsfehler. Bitte versuche es erneut." };
        }
    }
};