# Fase 5 — Deploy Híbrido: ngrok + Hostinger

## Arquitectura

```
┌──────────────────────────────────┐
│   Tu máquina (siempre encendida) │
│                                  │
│  Docker → Spring Boot :8080      │
│  Docker → PostgreSQL :5432       │
│                                  │
│  ngrok ──────────────────────────┼──▶ https://xxxxx.ngrok-free.app
└──────────────────────────────────┘              │
                                                  │ PUBLIC_API_URL
                                      ┌───────────▼──────────┐
                                      │  Hostinger (CDN)     │
                                      │  frontend/dist/      │
                                      │  (HTML + JS + CSS)   │
                                      └──────────────────────┘
```

El backend corre en tu máquina local, expuesto al mundo vía túnel ngrok.  
El frontend es un build estático subido a Hostinger — sin servidor, sin costes variables.

---

## Requisitos previos

- [ ] Cuenta en [ngrok.com](https://ngrok.com) (tier gratuito es suficiente)
- [ ] ngrok instalado: `brew install ngrok` (macOS)
- [ ] Docker Desktop corriendo con el stack levantado
- [ ] Hosting en Hostinger con acceso a **hPanel → File Manager** o FTP
- [ ] `lftp` instalado para FTP automático: `brew install lftp` (opcional)

---

## Paso 1 — Obtener tu dominio estático de ngrok (gratis)

El plan gratuito de ngrok incluye **1 dominio estático permanente**.  
Sin él, la URL cambia en cada reinicio y tendrías que rehacer el build cada vez.

1. Entra a [https://dashboard.ngrok.com](https://dashboard.ngrok.com)
2. En el menú lateral: **Cloud Edge → Domains**
3. Clic en **New Domain** → elige o acepta el dominio generado  
   (ej: `parrot-genuine-rarely.ngrok-free.app`)
4. Copia ese dominio — lo usarás en todos los pasos siguientes.

### Autenticar ngrok en tu máquina

```bash
# Copia el authtoken desde: https://dashboard.ngrok.com/get-started/your-authtoken
ngrok config add-authtoken <PEGA_TU_TOKEN_AQUÍ>
```

---

## Paso 2 — Configurar variables de entorno

### 2a. Backend (.env en la raíz del proyecto)

```bash
# .env
CORS_ALLOWED_ORIGINS=http://localhost:4321,https://clavei.fearg.es
```

> Para CORS usa el origen exacto, sin barra final.  
> Correcto: `https://clavei.fearg.es`  
> Incorrecto: `https://clavei.fearg.es/`
> Puedes separar varios orígenes con comas.

### 2b. Frontend (frontend/.env)

```bash
# frontend/.env
PUBLIC_API_URL=https://parrot-genuine-rarely.ngrok-free.app
```

Sustituye `parrot-genuine-rarely.ngrok-free.app` por tu dominio estático real.

---

## Paso 3 — Build del frontend

```bash
cd frontend
npm run build
```

El build queda en `frontend/dist/`. Ahí están todos los archivos estáticos listos para subir.

### Verificar el build localmente

```bash
npx serve frontend/dist -p 4000
# Abre http://localhost:4000 — debería llamar a tu backend local
```

---

## Paso 4 — Subir a Hostinger

### Opción A — File Manager (sin instalar nada)

1. Entra a [hPanel](https://hpanel.hostinger.com) → **Websites** → tu dominio → **Files → File Manager**
2. Navega a `public_html/`
3. Borra el contenido existente (si hay algo)
4. Arrastra o sube **el contenido de `frontend/dist/`** (no la carpeta `dist` en sí, sino todo lo que hay dentro)

### Opción B — FTP con lftp (más rápido, automatizable)

Busca tus credenciales FTP en hPanel → **Advanced → FTP Accounts**.

```bash
lftp -e "
  open ftp://TU_USUARIO_FTP:TU_PASSWORD_FTP@ftp.tudominio.com;
  mirror -R --delete frontend/dist/ /public_html/;
  bye
"
```

### Opción C — Script automático (`redeploy.sh`)

Ver sección **Script de re-deploy** al final de este documento.

---

## Paso 5 — Reiniciar docker-compose con la nueva configuración CORS

Después de editar `.env` (con el dominio de Hostinger en `CORS_ALLOWED_ORIGINS`), reinicia el backend:

```bash
docker compose down
docker compose up -d
```

---

## Paso 6 — Levantar el túnel ngrok

```bash
# Usa tu dominio estático para que la URL no cambie nunca
ngrok http --url=parrot-genuine-rarely.ngrok-free.app 8080
```

Deberías ver algo como:

```
Session Status    online
Account           tu@email.com (Plan: Free)
Version           3.x.x
Web Interface     http://127.0.0.1:4040
Forwarding        https://parrot-genuine-rarely.ngrok-free.app -> http://localhost:8080
```

**Deja esta terminal abierta** — el túnel muere si la cierras.

> **Tip:** Para que ngrok se arranque solo al iniciar el sistema, consulta:  
> https://ngrok.com/docs/agent/config/

---

## Paso 7 — Verificar el deploy completo

### Test del backend a través de ngrok

```bash
curl -s https://parrot-genuine-rarely.ngrok-free.app/api/v1/orders/ai-ingest \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "rawText": "2 cajas de papel A4 a 14.50 y 5 bolígrafos a 1.20",
    "clientId": "deploy-test-01",
    "clientName": "Test Deploy",
    "clientEmail": "test@deploy.com"
  }' | python3 -m json.tool
```

Respuesta esperada:
```json
{
  "orderId": "...",
  "status": "PENDING",
  "totalAmount": 35.0,
  "items": [...]
}
```

### Flujo completo

1. Abre `https://tudominio.hostinger.com` en el navegador
2. Pega un pedido en lenguaje natural en el textarea
3. Haz clic en **Procesar Pedido**
4. Verifica que aparece el resultado estructurado

---

## Resumen de variables de entorno

| Variable | Archivo | Valor |
|---|---|---|
| `GROQ_API_KEY` | `.env` (raíz) | `gsk_...` |
| `GROQ_MODEL` | `.env` (raíz) | `meta-llama/llama-4-scout-17b-16e-instruct` |
| `CORS_ALLOWED_ORIGINS` | `.env` (raíz) | `http://localhost:4321,https://clavei.fearg.es` |
| `DB_NAME` | `.env` (raíz) | `smartorder` |
| `DB_USERNAME` | `.env` (raíz) | `smartorder` |
| `DB_PASSWORD` | `.env` (raíz) | `smartorder_secret` (cámbialo en producción) |
| `PUBLIC_API_URL` | `frontend/.env` | `https://xxxxx.ngrok-free.app` |

---

## Script de re-deploy

Usa `redeploy.sh` cuando necesites volver a subir el frontend:

```bash
# Ejemplo
./redeploy.sh \
  https://parrot-genuine-rarely.ngrok-free.app \
  ftp_usuario \
  ftp_password \
  ftp.tudominio.com
```

---

## Troubleshooting

### El frontend no puede llamar al backend (CORS error)

```
Access to fetch at 'https://xxx.ngrok-free.app' has been blocked by CORS policy
```

**Causa:** `CORS_ALLOWED_ORIGINS` en `.env` no incluye el dominio de Hostinger.  
**Solución:**
```bash
# .env
CORS_ALLOWED_ORIGINS=http://localhost:4321,https://clavei.fearg.es
docker compose down && docker compose up -d
```

---

### ngrok responde con código 429 o "ngrok-skip-browser-warning"

**Causa:** ngrok Free muestra una página de advertencia para peticiones de navegador.  
**Solución:** Añade el header en `useOrderProcessor.ts` (ya resuelto — el useOrderProcessor usa `Content-Type: application/json` que evita esto para peticiones API).  
Para peticiones curl de prueba, añade: `-H "ngrok-skip-browser-warning: true"`

---

### El tunnel ngrok se cierra

**Causa:** La terminal se cerró o el ordenador entró en suspensión.  
**Solución:**
```bash
ngrok http --url=<TU-DOMINIO>.ngrok-free.app 8080
```

Para evitarlo, considera ejecutar ngrok como servicio:
```bash
# macOS (launchd)
ngrok service install --config ~/.config/ngrok/ngrok.yml
ngrok service start
```

---

### Los cambios en el frontend no se reflejan en Hostinger

El frontend es un **build estático** — los cambios en código fuente no se reflejan automáticamente.  
Debes correr `npm run build` y subir `frontend/dist/` de nuevo.  
Usa `redeploy.sh` para hacerlo en un solo comando.

---

## Flujo de trabajo diario (una vez desplegado)

```
# 1. Arrancar (cada vez que enciendas el ordenador)
docker compose up -d
ngrok http --url=<TU-DOMINIO>.ngrok-free.app 8080

# 2. Verificar que todo está arriba
curl -s https://<TU-DOMINIO>.ngrok-free.app/actuator/health   # opcional
docker compose ps

# 3. Apagar (cuando termines)
docker compose down
# (cerrar la terminal de ngrok)
```
