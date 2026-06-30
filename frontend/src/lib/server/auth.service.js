// Server-side auth helper. Talks to the Spring Boot backend's self-hosted JWT
// endpoints (no Auth0, no axios). Stores the JWT and a small user-info object in
// httpOnly cookies.
import { API_BASE } from "$lib/server/api.js";

const COOKIE_OPTIONS = {
  path: "/",
  maxAge: 60 * 60 * 24 * 7, // 7 days
  sameSite: "lax",
  httpOnly: true,
  secure: process.env.NODE_ENV === "production"
};

function setSession(cookies, token, user) {
  cookies.set("jwt_token", token, COOKIE_OPTIONS);
  cookies.set("user_info", encodeURIComponent(JSON.stringify(user)), COOKIE_OPTIONS);
}

async function parseError(res, fallback) {
  // Das Backend liefert Fehler als reinen Text (ResponseEntity<String>), teils als
  // JSON. Beides unterstützen, damit der echte Grund sichtbar wird (statt Fallback).
  try {
    const text = await res.text();
    if (!text || !text.trim()) return fallback;
    try {
      const body = JSON.parse(text);
      return body.message || body.error || text;
    } catch {
      return text.trim();
    }
  } catch {
    return fallback;
  }
}

async function login(email, password, cookies) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });
  if (!res.ok) {
    throw new Error(await parseError(res, "Login fehlgeschlagen"));
  }
  const data = await res.json();
  setSession(cookies, data.token, data.user);
  cookies.delete("demo_mode", { path: "/" }); // echtes Login → kein Demo-Modus
  return data.user;
}

async function register(token, userData, cookies) {
  const res = await fetch(`${API_BASE}/auth/register/${encodeURIComponent(token)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData)
  });
  if (!res.ok) {
    throw new Error(await parseError(res, "Registrierung fehlgeschlagen"));
  }
  const data = await res.json();
  setSession(cookies, data.token, data.user);
  cookies.delete("demo_mode", { path: "/" });
  return data.user;
}

async function demoLogin(as, cookies) {
  const url = as ? `${API_BASE}/demo/login?as=${encodeURIComponent(as)}` : `${API_BASE}/demo/login`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(await parseError(res, "Demo-Login fehlgeschlagen"));
  }
  const data = await res.json();
  setSession(cookies, data.token, data.user);
  // Demo-Modus merken, damit der Rollen-Switcher angezeigt wird
  cookies.set("demo_mode", "1", COOKIE_OPTIONS);
  return data.user;
}

function logout(cookies) {
  cookies.delete("jwt_token", { path: "/" });
  cookies.delete("user_info", { path: "/" });
  cookies.delete("demo_mode", { path: "/" });
}

// Maps a user role to its landing dashboard.
export function dashboardForRole(role) {
  switch (role) {
    case "STUDENT":
      return "/student/dashboard";
    case "TEACHER":
      return "/teacher/dashboard";
    case "SCHOOL_ADMIN":
    case "DEMO_USER":
      return "/admin/dashboard";
    case "SUPER_ADMIN":
      return "/super/dashboard";
    default:
      return "/";
  }
}

const auth = { login, register, demoLogin, logout };
export default auth;
