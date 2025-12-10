// frontend/src/routes/student/training/+page.server.js
import { redirect } from "@sveltejs/kit";

// Diese Route leitet jetzt zur Session-Übersicht weiter
// Neue Sessions werden dort gestartet
export async function load({ locals }) {
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  // Redirect zur neuen Session-Übersicht
  throw redirect(302, "/student/sessions");
}