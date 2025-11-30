import { redirect } from "@sveltejs/kit";

export async function load({ locals }) {
  // Nicht eingeloggt → Login
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const roles = locals.user?.user_roles ?? [];

  if (roles.includes("STUDENT")) {
    throw redirect(302, "/student/dashboard");
  }

  if (roles.includes("TEACHER")) {
    throw redirect(302, "/teacher/dashboard");
  }

  // Falls jemand keine passende Rolle hat
  throw redirect(302, "/login");
}