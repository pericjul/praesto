import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api";

export async function load({ locals, fetch }) {

  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const roles = locals.user?.user_roles ?? [];

  if (!roles.includes("STUDENT")) {
    throw redirect(302, "/");
  }

  const token = locals.jwt_token;
  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  try {
    const res = await fetch(`${API_BASE}/student/dashboard`, { headers });

    if (!res.ok) {
      throw new Error("Dashboard konnte nicht geladen werden.");
    }

    const dashboard = await res.json();

    return {
      dashboard,
      user: locals.user
    };

  } catch (err) {
    return {
      dashboard: null,
      error: err.message,
      user: locals.user
    };
  }
}