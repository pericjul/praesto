# Security-Hardening vor dem Pilot (Azure)

Diese Schritte ergänzen die Code-Fixes (Datei-Download-Autorisierung, Upload-Whitelist,
nginx-Security-Header). Sie sind **vor dem Pilot mit echten Schülerdaten** zwingend.

## H-1 — Dateien dürfen nicht verloren gehen (persistenter Speicher)
Das Container-Dateisystem von Azure App Service ist **flüchtig** → hochgeladene Dossier-
Dateien und generierte Lebensläufe/Briefe wären nach jedem Neustart weg.

**Sofort-Fix (persistenter /home-Mount):** in der Web App → **Settings → Environment variables**:
| Variable | Wert |
|---|---|
| `WEBSITES_ENABLE_APP_SERVICE_STORAGE` | `true` |
| `PRAESTO_UPLOADS_DIR` | `/home/data/uploads` |

→ `/home` ist auf App Service persistent; die App schreibt Uploads dann dorthin.

**Bessere Lösung (später):** Azure **Blob Storage** (Storage Account + Container), Dateien
dort ablegen statt im Dateisystem. Robuster bei Mehr-Instanz-Betrieb.

## H-4 — App mit eingeschränktem DB-User betreiben (Least Privilege)
Nicht mit dem Server-Admin (`praestoadmin`) produktiv laufen. Dedizierten App-User anlegen.

**Reihenfolge (wichtig wegen Hibernate-Schema):**
1. **Erster Deploy** mit `DB_USERNAME=praestoadmin` → Hibernate legt die Tabellen an.
2. Danach per DBeaver (als `praestoadmin`, DB `praesto`) ausführen:
   ```sql
   CREATE USER praesto_app WITH PASSWORD '<starkes-passwort>';
   GRANT CONNECT ON DATABASE praesto TO praesto_app;
   GRANT USAGE ON SCHEMA public TO praesto_app;
   GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO praesto_app;
   GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO praesto_app;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public
     GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO praesto_app;
   ```
3. Web App: `DB_USERNAME=praesto_app`, `DB_PASSWORD=<obiges>`, **`DB_DDL=validate`** setzen
   (App-User hat absichtlich kein DDL-Recht). Bei späteren Schemaänderungen Migration via
   `praestoadmin` einspielen → siehe H-2 (Flyway).

## M-3 — HTTPS erzwingen
Web App → **Settings → Configuration** (bzw. „TLS/SSL") → **„HTTPS Only" = Ein**.
(HSTS-Header setzt bereits die nginx-Config im Container.)

## H-2 — Schema-Migrationen (bald danach)
`DB_DDL=update` in Prod ist riskant. Empfohlen: **Flyway** einführen (versionierte
`V1__init.sql` …), in Prod `DB_DDL=validate`. Bis dahin: nach dem ersten stabilen Schema
auf `validate` umstellen.

## Erledigt im Code (dieser Stand)
- ✅ Datei-Download nur für Besitzer/Schul-Personal/Super-Admin (FileAccessService).
- ✅ Upload-Whitelist (PDF/Word/Bilder/Video), Pfad-Sanitisierung, 20 MB-Limit.
- ✅ nginx-Security-Header (X-Frame-Options, nosniff, Referrer-Policy, HSTS, frame-ancestors).
