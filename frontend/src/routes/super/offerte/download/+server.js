// Nimmt das Offerten-Formular (POST), ruft serverseitig das Backend (mit JWT aus
// dem Cookie) und liefert die fertige .docx als Download zurück.
import { API_BASE } from "$lib/server/api.js";

export async function POST({ request, locals }) {
    if (!locals.isAuthenticated || locals.user?.role !== "SUPER_ADMIN") {
        return new Response("Nicht berechtigt", { status: 403 });
    }

    const fd = await request.formData();
    const body = {};
    for (const [k, v] of fd.entries()) body[k] = typeof v === "string" ? v : "";

    const token = locals.jwt_token;
    const res = await fetch(`${API_BASE}/super/offerte`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        return new Response(text || "Offerte konnte nicht erstellt werden", { status: res.status });
    }

    const buf = await res.arrayBuffer();
    const nr = (body.offertenNr || "").replace(/[^a-zA-Z0-9._-]/g, "_");
    const filename = `Offerte_Praesto${nr ? "_" + nr : ""}.docx`;
    return new Response(buf, {
        status: 200,
        headers: {
            "Content-Type": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "Content-Disposition": `attachment; filename="${filename}"`
        }
    });
}
