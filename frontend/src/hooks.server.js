// Backend und Frontend laufen im selben Container; das Backend hört intern auf
// Port 8080. Jeder serverseitige /api-Aufruf (load/action via event.fetch) wird
// hart dorthin geleitet. Wichtig, weil SvelteKits event.fetch einen Aufruf auf
// die eigene (öffentliche) Origin als interne Route behandeln würde → 404, und
// weil so eine falsch gesetzte API_BASE_URL (z.B. öffentliche Azure-URL) keine
// Rolle mehr spielt. Lokale Entwicklung (5173→8080) funktioniert unverändert.
const INTERNAL_API = process.env.INTERNAL_API_BASE ?? "http://localhost:8080";

export async function handleFetch({ event, request, fetch }) {
    const url = new URL(request.url);
    if (url.pathname.startsWith("/api")) {
        const headers = new Headers(request.headers);
        // Token aus event.locals (von handle() aus dem Cookie gesetzt) – diese
        // Quelle ist nachweislich vorhanden. SvelteKits event.fetch streift den
        // Authorization-Header bei origin-fremden Requests sonst ab -> 403.
        // globalThis.fetch ans interne Backend liefert nachweislich 200.
        const token = event?.locals?.jwt_token;
        if (token) headers.set("authorization", `Bearer ${token}`);
        // UI-Sprache ans Backend durchreichen, damit KI (Coach + Bewertung)
        // in der Sprache der Nutzer:in antwortet statt immer auf Deutsch.
        const lang = event?.locals?.lang;
        if (lang) headers.set("x-locale", lang);
        const init = { method: request.method, headers, redirect: "manual" };
        if (request.method !== "GET" && request.method !== "HEAD") {
            init.body = await request.arrayBuffer();
            init.duplex = "half";
        }
        return globalThis.fetch(`${INTERNAL_API}${url.pathname}${url.search}`, init);
    }
    return fetch(request);
}

// Reads the JWT and user-info cookies set at login and exposes them on
// event.locals for load functions and actions. The token's signature is verified
// by the backend on every API call — here we only check that a token and a user
// id are present.
export async function handle({ event, resolve }) {
    const jwt_token = event.cookies.get("jwt_token");
    const userInfoCookie = event.cookies.get("user_info");

    event.locals.jwt_token = jwt_token;

    if (userInfoCookie) {
        try {
            event.locals.user = JSON.parse(decodeURIComponent(userInfoCookie));
        } catch (error) {
            console.error("Failed to parse user info cookie:", error);
            event.locals.user = {};
        }
    } else {
        event.locals.user = {};
    }

    event.locals.isAuthenticated = Boolean(jwt_token && event.locals.user && event.locals.user.id);
    event.locals.isDemo = event.cookies.get("demo_mode") === "1";

    // Sprache aus Cookie (Standard: Deutsch)
    const cookieLang = event.cookies.get("lang");
    event.locals.lang = ["de", "en", "fr", "it"].includes(cookieLang) ? cookieLang : "de";

    return resolve(event);
}

// Zentrales Logging unerwarteter Fehler (SSR/Load/Actions). Dem Nutzer wird nur eine
// generische Meldung gezeigt; die Details landen im Server-Log (Azure) mit Kontext.
// Anknüpfpunkt für ein späteres Error-Monitoring (z.B. Sentry).
export function handleError({ error, event, status }) {
    if (status !== 404) {
        console.error(`[SSR-Fehler] ${event.request.method} ${event.url.pathname}:`, error);
    }
    return { message: "Es ist ein unerwarteter Fehler aufgetreten. Bitte lade die Seite neu." };
}
