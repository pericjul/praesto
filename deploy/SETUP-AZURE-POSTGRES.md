# Praesto auf Azure deployen (mit echter PostgreSQL) – von Null

Muster wie früher (Azure Web App + MongoDB Atlas), nur ist die DB jetzt PostgreSQL.
Drei Bausteine: **A) Datenbank**, **B) App-Server**, **C) Verbindung**.

> Hinweis Datenstandort: Azure-Regionen können in der EU/USA liegen. Solange nur
> Demo-Daten drin sind, ist das ok. Für echte Schülerdaten später auf einen
> Schweizer Server umziehen (siehe `SETUP-EXOSCALE.md`).
> Tipp: als Region möglichst **„Switzerland North"** wählen, dann steht alles in der CH.

---

## A) Datenbank – Azure Database for PostgreSQL
1. Azure-Portal → **Create a resource** → „Azure Database for PostgreSQL" →
   **Flexible server** → *Create*.
2. Ausfüllen:
   - **Resource group**: neu anlegen, z.B. `praesto-rg`.
   - **Server name**: z.B. `praesto-db` (wird zu `praesto-db.postgres.database.azure.com`).
   - **Region**: `Switzerland North` (wenn verfügbar) – sonst eine EU-Region.
   - **PostgreSQL version**: 16.
   - **Workload type**: „Development" (= günstigster, Burstable B1ms).
   - **Authentication**: „PostgreSQL authentication only".
   - **Admin username**: z.B. `praestoadmin` – merken.
   - **Password**: starkes Passwort setzen – merken (NICHT in den Chat).
3. **Networking**-Tab:
   - „Allow public access from any Azure service within Azure to this server"
     **aktivieren** (damit die Web App rankommt).
   - „Add current client IP address" **aktivieren** (damit du selbst per DBeaver testen kannst).
4. **Create** → 5–10 Min warten.
5. Nach dem Anlegen: Server → **Databases** → eine DB anlegen, z.B. `praesto`
   (oder die vorhandene `postgres` benutzen).

→ Du hast jetzt: Host `praesto-db.postgres.database.azure.com`, Port `5432`,
   DB-Name `praesto`, User `praestoadmin`, dein Passwort.

Daraus wird die `DB_URL`:
```
jdbc:postgresql://praesto-db.postgres.database.azure.com:5432/praesto?sslmode=require
```
(`sslmode=require` ist bei Azure Pflicht.)

## B) App-Server – Web App for Containers
1. Azure-Portal → **Create a resource** → **Web App**.
2. Ausfüllen:
   - **Resource group**: `praesto-rg` (dieselbe).
   - **Name**: z.B. `praesto` → URL wird `https://praesto.azurewebsites.net`.
   - **Publish**: **Container**.
   - **Operating System**: **Linux**.
   - **Region**: gleiche wie die DB.
   - **Pricing plan**: kleiner Plan (z.B. B1) – evtl. von Studenten-Guthaben gedeckt.
3. Tab **Container**:
   - **Image Source**: „Other container registries".
   - **Registry server URL**: `https://ghcr.io`
   - **Image and tag**: `pericjul/praesto:latest`
   - Wenn das ghcr-Package privat ist: Username = dein GitHub-Name,
     Passwort = ein GitHub-PAT mit Scope `read:packages`.
     (Einfacher: Package in GitHub auf **public** stellen → kein Login nötig.)
4. **Create**.

## C) Verbindung – Umgebungsvariablen + Firewall
Web App → **Settings → Environment variables** → diese anlegen:

| Name | Wert |
|------|------|
| `DB_URL` | `jdbc:postgresql://praesto-db.postgres.database.azure.com:5432/praesto?sslmode=require` |
| `DB_USERNAME` | `praestoadmin` |
| `DB_PASSWORD` | *(dein DB-Passwort)* |
| `DB_DDL` | `update` |
| `JWT_SECRET` | *(frischer Zufallswert: `openssl rand -base64 32`)* |
| `SUPER_ADMIN_EMAIL` | `admin@praesto.ch` |
| `SUPER_ADMIN_PASSWORD` | *(Passwort deiner Wahl)* |
| `OPEN_AI_KEY` | `local-dev` *(optional echter Key für den KI-Coach)* |
| `NODE_ENV` | `production` |
| `WEBSITES_PORT` | `80` |

→ **Save** (App startet neu). Beim ersten Start legt Hibernate die Tabellen an und
seedet Badges + Super-Admin + Demo-Daten.

## Deploy / Updates (GitHub Actions)
Workflow `.github/workflows/azure-container-webapp.yml` baut das Image und
veröffentlicht es. Einmalig nötig:
- GitHub → Repo → **Settings → Secrets and variables → Actions** →
  `AZURE_WEBAPP_PUBLISH_PROFILE` = Inhalt des Publish-Profils
  (Azure → Web App → **Get publish profile** herunterladen).
- Falls dein Web-App-Name nicht `praesto` ist: im Workflow `AZURE_WEBAPP_NAME` anpassen.

Auslösen: GitHub → **Actions** → den Azure-Workflow → **Run workflow**.

## Domain (praesto.ch) bei Hostpoint
Web App → **Custom domains** → Azure zeigt einen CNAME + TXT. Diese bei
**Hostpoint → DNS** eintragen (z.B. `demo` → `praesto.azurewebsites.net`).
Danach in Azure **Managed Certificate** für kostenloses HTTPS hinzufügen.
Tipp: `demo.praesto.ch` nehmen, `praesto.ch` für den späteren CH-Server freihalten.

## Test
`https://praesto.azurewebsites.net` öffnen → Login mit Super-Admin oder den
Demo-Logins. Mit DBeaver kannst du dich (dank Firewall-Regel) auch direkt auf die
Azure-DB verbinden und die Tabellen ansehen.
