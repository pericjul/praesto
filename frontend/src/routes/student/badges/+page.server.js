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
    const headers = {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };

    try {
        const res = await fetch(`${API_BASE}/badges/my`, { headers });
        
        if (!res.ok) {
            console.error("Fehler beim Laden der Badges:", res.status);
            return { badges: [], error: "Fehler beim Laden der Badges" };
        }

        const badges = await res.json();
        return { badges };
    } catch (err) {
        console.error("Netzwerkfehler:", err);
        return { badges: [], error: "Verbindungsfehler" };
    }
}