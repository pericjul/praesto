// frontend/src/routes/dashboard/student/+page.server.js
import { redirect } from "@sveltejs/kit";

const API_BASE = "http://localhost:8080/api/student/dashboard";

export async function load({ locals, fetch }) {
    if (!locals.isAuthenticated) {
        throw redirect(302, "/login");
    }

    const token = locals.jwt_token;

    if (!token) {
        throw redirect(302, "/login");
    }

    try {
        const res = await fetch(API_BASE, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (!res.ok) {
            return {
                dashboard: null,
                error: `Backend Error ${res.status}`
            };
        }

        const dashboard = await res.json();
        return { dashboard };
    } catch (err) {
        console.error("Dashboard Laden fehlgeschlagen:", err);

        return {
            dashboard: null,
            error: "Das Dashboard konnte nicht geladen werden."
        };
    }
}