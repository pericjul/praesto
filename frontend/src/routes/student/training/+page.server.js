// frontend/src/routes/student/training/+page.server.js
import { redirect, fail } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, url, fetch }) {
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const token = locals.jwt_token;
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };

  // optional: assignmentId durchreichen (z.B. ?assignmentId=123)
  const assignmentId = url.searchParams.get("assignmentId");

  // Neue Session starten
  const res = await fetch(`${API_BASE}/sessions`, {
    method: "POST",
    headers,
    body: JSON.stringify({
      assignmentId: assignmentId ?? null
    })
  });

  if (!res.ok) {
    console.error("Fehler beim Starten der Session", await res.text());
    throw fail(res.status, { error: "Session konnte nicht gestartet werden." });
  }

  const session = await res.json();

  return {
    session
  };
}