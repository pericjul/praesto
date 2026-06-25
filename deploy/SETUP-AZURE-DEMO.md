# Praesto – öffentliche Demo auf Azure (USA, nur Demo-Daten!)

Zweck: ein öffentlicher Link, den du Schulen zum Ausprobieren schicken kannst,
**bevor** eine Schule zusagt. Läuft mit einer **In-Container-Datenbank (H2)** –
keine externe DB, **0 CHF DB-Kosten**. Die Demo-Daten werden beim Start automatisch
erzeugt und setzen sich jede Nacht (und bei jedem Neustart) zurück.

> ⚠️ WICHTIG: Hier dürfen **nur Demo-/Fake-Daten** rein. Keine echte Schule darf
> echte Schülerdaten eingeben (Server steht in den USA). Sobald eine Schule wirklich
> startet → vorher auf einen Schweizer Server umziehen (siehe `SETUP-EXOSCALE.md`),
> Daten erst dort eingeben.

---

## 1. App Settings (Umgebungsvariablen) im Azure Web App setzen
Azure-Portal → dein Web App `praesto` → **Settings → Environment variables**
(früher „Configuration → Application settings") → diese Einträge anlegen:

| Name | Wert |
|------|------|
| `DB_URL` | `jdbc:h2:mem:praesto;DB_CLOSE_DELAY=-1;MODE=PostgreSQL` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | *(leer lassen)* |
| `DB_DDL` | `update` |
| `JWT_SECRET` | *(frischer Zufallswert, siehe unten – NICHT der aus dem Repo)* |
| `SUPER_ADMIN_EMAIL` | `admin@praesto.ch` |
| `SUPER_ADMIN_PASSWORD` | *(ein Passwort deiner Wahl, nur für die Demo)* |
| `AZURE_OPENAI_KEY` | *(optional: Key deiner Azure-OpenAI-Ressource, dann antwortet der KI-Coach wirklich)* |
| `AZURE_OPENAI_ENDPOINT` | `https://<dein-openai>.openai.azure.com/` |
| `AZURE_OPENAI_DEPLOYMENT` | `gpt-4o-mini` |
| `NODE_ENV` | `production` |
| `WEBSITES_PORT` | `80` |

Danach **Save** / **Apply** (Web App startet neu).

JWT-Secret frisch erzeugen (Terminal):
```bash
openssl rand -base64 32
```

## 2. Deploy auslösen (GitHub Actions)
Der Workflow `.github/workflows/azure-container-webapp.yml` baut das Docker-Image
und veröffentlicht es im Web App.

Voraussetzung (einmalig): in GitHub → Repo → **Settings → Secrets and variables → Actions**
muss `AZURE_WEBAPP_PUBLISH_PROFILE` gesetzt sein (Publish-Profil aus dem Azure-Portal:
Web App → **Get publish profile** herunterladen, Inhalt als Secret einfügen).

Auslösen: GitHub → **Actions** → „Build and deploy a container to an Azure Web App"
→ **Run workflow** (Branch wählen) → starten.

## 3. Testen
Nach dem Deploy: `https://<dein-app-name>.azurewebsites.net` öffnen.
Login als Super-Admin mit `SUPER_ADMIN_EMAIL` / `SUPER_ADMIN_PASSWORD`,
oder die Demo-Logins von der Login-Seite nutzen.

## 4. Eigene Domain (praesto.ch) bei Hostpoint darauf zeigen lassen
Die Domain bleibt bei Hostpoint, nur ein DNS-Eintrag zeigt auf Azure:

1. Azure-Portal → Web App → **Custom domains** → **Add custom domain** →
   Azure zeigt dir die nötigen Einträge (einen **CNAME** und einen **TXT** zur
   Verifizierung).
2. Bei **Hostpoint** → Control Panel → Domain `praesto.ch` → **DNS** → diese
   Einträge anlegen (z.B. `demo` als CNAME auf `<app>.azurewebsites.net`,
   plus den TXT-Verifizierungseintrag).
3. Zurück in Azure auf **Validate/Add**, danach **Managed Certificate** für HTTPS
   hinzufügen (kostenlos).

→ Ergebnis: z.B. `https://demo.praesto.ch` zeigt auf die Azure-Demo.

> Tipp: Für die Demo eine **Subdomain** wie `demo.praesto.ch` nehmen. Dann bleibt
> `praesto.ch` selbst frei für später (Schweizer Produktiv-Server).
