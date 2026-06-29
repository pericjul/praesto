// Shared API configuration for all server-side load functions and actions.
// The base URL points at the Spring Boot backend. In production it is provided
// via the API_BASE_URL environment variable (Azure App Settings); locally it
// defaults to the dev backend on port 8080.
export const API_BASE = process.env.API_BASE_URL ?? "http://localhost:8080/api";

// Builds the standard JSON headers for a backend request, attaching the
// Authorization bearer token when one is available.
export const apiHeaders = (token) => ({
  "Content-Type": "application/json",
  ...(token ? { Authorization: `Bearer ${token}` } : {})
});

// Uploads a file (from a SvelteKit form action) to the backend and returns
// { fileUrl, fileName }. Do NOT set Content-Type here — fetch sets the multipart
// boundary automatically.
export async function uploadFile(file, token) {
  const fd = new FormData();
  fd.append("file", file);
  const res = await fetch(`${API_BASE}/files`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: fd
  });
  if (!res.ok) {
    // Backend-Meldung (z.B. "Dateityp nicht erlaubt …") durchreichen, statt sie zu schlucken.
    const msg = await res.text().catch(() => "");
    throw new Error(msg && msg.length < 300 ? msg : "Datei-Upload fehlgeschlagen");
  }
  return res.json();
}
