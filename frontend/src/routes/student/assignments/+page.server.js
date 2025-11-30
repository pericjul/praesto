// frontend/src/routes/student/assignments/+page.server.js
import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch }) {
  // 1) Nicht eingeloggt → Login
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const user = locals.user ?? {};
  const roles = user.user_roles ?? [];

  // 2) Nur Studenten sollen diese Seite sehen
  if (!roles.includes("STUDENT")) {
    // Optional: Lehrer könnten später eine eigene Ansicht bekommen
    throw redirect(302, "/dashboard");
  }

  const token = locals.jwt_token;
  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  let assignments = [];
  let loadError = null;

  // 3) ClassId aus dem JWT / user-info
  const classId = user.classId;

  if (!classId) {
    // Kein classId → wir geben einfach leere Liste + Hinweis zurück
    loadError =
      "Für dein Profil ist aktuell keine Klasse hinterlegt. Bitte wende dich an deine Lehrperson.";
    return {
      assignments,
      error: loadError
    };
  }

  // 4) Assignments für die Klasse vom Backend holen
  try {
    const res = await fetch(`${API_BASE}/assignments/${classId}`, {
      headers
    });

    if (res.ok) {
      assignments = await res.json();
    } else if (res.status === 403) {
      loadError = "Du hast keine Berechtigung, diese Aufgaben zu sehen.";
    } else {
      loadError = `Fehler beim Laden der Aufgaben (Status ${res.status}).`;
    }
  } catch (err) {
    console.error("Fehler beim Laden der Assignments:", err);
    loadError = "Die Aufgaben konnten nicht geladen werden. Versuche es später erneut.";
  }

  return {
    assignments,
    error: loadError
  };
}