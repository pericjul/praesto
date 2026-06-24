import { redirect } from "@sveltejs/kit";

import { API_BASE } from "$lib/server/api.js";

export async function load({ locals, fetch }) {

  if (!locals.isAuthenticated) {
    throw redirect(302, "/login");
  }

  const role = locals.user?.role;

  if (role !== "STUDENT") {
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