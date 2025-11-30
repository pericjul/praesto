import { redirect } from "@sveltejs/kit";

export async function load({ locals }) {
  // Nicht eingeloggt → Login
  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  // Rollen aus dem JWT (wie bei dir: user.user_roles)
  const roles = locals.user?.user_roles ?? [];

  if (roles.includes("STUDENT")) {
    throw redirect(302, "/dashboard/student");
  }

  if (roles.includes("TEACHER")) {
    throw redirect(302, "/dashboard/teacher");
  }

  // Falls jemand keine passende Rolle hat
  throw redirect(302, "/login");
}