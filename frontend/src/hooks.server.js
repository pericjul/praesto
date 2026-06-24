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
