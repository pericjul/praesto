import { redirect } from "@sveltejs/kit";

export async function load({ locals }) {
  // Nicht eingeloggt → Login
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const role = locals.user?.role;

  if (role === "STUDENT") {
    throw redirect(302, "/student/dashboard");
  }

  if (role === "TEACHER") {
    throw redirect(302, "/teacher/dashboard");
  }

  if (role === "SCHOOL_ADMIN" || role === "DEMO_USER") {
    throw redirect(302, "/admin/dashboard");
  }

  if (role === "SUPER_ADMIN") {
    throw redirect(302, "/super/dashboard");
  }

  // Falls jemand keine passende Rolle hat
  throw redirect(302, "/login");
}