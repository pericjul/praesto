# Praesto auf Exoscale (Schweiz) deployen

Ziel: App **und** Datenbank laufen bei Exoscale (Schweizer Anbieter, Zürich/Genf).
Architektur: 1 VM mit Docker → Container `app` (nginx + Backend + Frontend) hinter
`caddy` (automatisches HTTPS). Datenbank = Exoscale Managed PostgreSQL.

---

## 1. Datenbank (Exoscale Managed PostgreSQL)
1. Exoscale-Portal → **Databases → Add → PostgreSQL**, Zone **ch-dk-2 (Zürich)**, Version 16, kleiner Plan.
2. Nach „Running": Connection-URI kopieren (`postgres://avnadmin:...@host.db.exo.io:21699/defaultdb?sslmode=require`).
3. **IP-Filter** der DB: später die öffentliche IP der App-VM (Schritt 2) eintragen; zum Testen deine eigene IP.

## 2. App-Server (Exoscale Compute VM)
1. **Compute → Add Instance**: Ubuntu 24.04, Zone wie DB, kleine Größe (z.B. „Small", 2 vCPU/4 GB), deinen SSH-Key hinterlegen.
2. **Security Group / Firewall**: Ports **22 (SSH)**, **80** und **443** offen.
3. Per SSH einloggen, Docker installieren:
   ```bash
   curl -fsSL https://get.docker.com | sh
   ```
4. Projektordner anlegen und Deploy-Dateien hochladen:
   ```bash
   sudo mkdir -p /opt/praesto && sudo chown $USER /opt/praesto
   ```
   Lege dort ab (aus diesem `deploy/`-Ordner): `docker-compose.yml`, `Caddyfile`,
   und `.env` (aus `.env.example` kopieren, echte Werte eintragen – v.a. `DB_URL` der Exoscale-DB).

## 3. DNS
- Beim Domain-Anbieter von **praesto.ch** einen **A-Record** auf die **öffentliche IP der VM** setzen (auch `www`).
- Caddy holt dann automatisch das HTTPS-Zertifikat (Let's Encrypt).

## 4. Image-Zugriff (ghcr)
Das App-Image liegt unter `ghcr.io/pericjul/praesto`. Entweder:
- **Package auf „public" stellen** (GitHub → Repo → Packages → praesto → Settings → Visibility: Public) – dann kein Login nötig, oder
- auf der VM einmalig `docker login ghcr.io` mit einem GitHub-PAT (Scope `read:packages`).

## 5. Erststart
```bash
cd /opt/praesto
docker compose pull
docker compose up -d
docker compose logs -f app   # beim ersten Start legt Hibernate die Tabellen an + seedet
```
→ https://praesto.ch sollte laufen (HTTPS automatisch).

## 6. Automatisches Deploy (GitHub Actions)
Der Workflow `.github/workflows/deploy-exoscale.yml` baut bei jedem Push auf `main`
das Image und startet es auf der VM neu. Dafür in GitHub **Repository Secrets** setzen:
- `EXOSCALE_HOST` = öffentliche IP der VM
- `EXOSCALE_USER` = SSH-User (z.B. `ubuntu`)
- `EXOSCALE_SSH_KEY` = privater SSH-Key (passend zum auf der VM hinterlegten public key)

Der alte Azure-Deploy ist deaktiviert (nur noch manuell).

## Updates / Betrieb
- Update: einfach auf `main` pushen → Action deployt automatisch. Oder manuell auf der VM:
  `cd /opt/praesto && docker compose pull && docker compose up -d`
- Logs: `docker compose logs -f`
- Die Datei `.env` (Geheimnisse) bleibt **nur auf der VM**, niemals in git.
