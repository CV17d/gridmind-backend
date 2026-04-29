# Guía de Despliegue de GridMind

Esta guía te ayudará a desplegar la aplicación GridMind:
1. **Base de Datos:** PostgreSQL en **Supabase**.
2. **Backend:** Spring Boot en **Railway**.
3. **Frontend:** React + Vite en **Vercel**.
4. **Dominio Personalizado:** Configuración del dominio.

---

## 1. Configurar la Base de Datos en Supabase
1. Ve a [Supabase](https://supabase.com/) e inicia sesión o crea una cuenta.
2. Haz clic en **"New Project"**.
   - Asigna un nombre (ej. `gridmind-db`).
   - Define una contraseña fuerte para la base de datos y **guárdala** (la necesitarás más adelante).
   - Selecciona la región más cercana a tus usuarios.
3. Una vez creado el proyecto, ve al menú lateral izquierdo: **Settings -> Database**.
4. En la sección **Connection string**, selecciona **URI** y copia la URL. Se verá similar a esto:
   `postgresql://postgres.xxxxxx:[YOUR-PASSWORD]@aws-0-us-west-1.pooler.supabase.com:6543/postgres`
5. **Asegúrate de reemplazar `[YOUR-PASSWORD]`** por la contraseña que creaste en el paso 2.

> [!TIP]
> Supabase usa poolers por defecto (puerto 6543). Esto es ideal para conexiones web.

---

## 2. Desplegar el Backend en Railway

He preparado el código modificando tu `application.yaml` para que acepte variables de entorno. Ahora, Railway podrá inyectar la URL de Supabase sin que el código esté estático.

1. Si aún no lo has hecho, sube tu proyecto completo a **GitHub**.
2. Ve a [Railway.app](https://railway.app/) y crea un nuevo proyecto.
3. Selecciona **"Deploy from GitHub repo"** y elige tu repositorio de `GridMind`.
4. Si tu repositorio es un monorepo (contiene backend y frontend juntos):
   - Una vez importado el repositorio, ve a los **Settings** del servicio en Railway.
   - En **Service Settings -> Root Directory**, escribe `/backend` (o la ruta a la carpeta de tu backend).
   - Railway detectará automáticamente el archivo `Dockerfile` y construirá la aplicación.
5. Ve a la pestaña **Variables** en Railway y agrega las siguientes:
   * `SPRING_DATASOURCE_URL`: (Ej. `jdbc:postgresql://aws-0-us-west...:6543/postgres`) **Importante:** Añade `jdbc:` al inicio de la URL de Supabase que copiaste.
   * `SPRING_DATASOURCE_USERNAME`: (Suele ser `postgres` u otro usuario provisto por Supabase).
   * `SPRING_DATASOURCE_PASSWORD`: (La contraseña de Supabase).
   * `FRONTEND_URL`: (La URL que usarás en Vercel, si aún no la tienes, pon `*` temporalmente o actualízala después para el CORS).
   * `GEMINI_API_KEY`: Tu API Key de Gemini.
   * `RESEND_API_KEY`: Tu API Key de Resend (si la usas).
6. Una vez configurado, ve a **Settings -> Networking** y haz clic en **"Generate Domain"** para obtener tu URL pública del backend (ej. `gridmind-production.up.railway.app`). ¡Copia esta URL!

---

## 3. Desplegar el Frontend en Vercel

He creado un archivo `vercel.json` en la carpeta `frontend` para garantizar que la navegación de React Router funcione correctamente y no arroje errores 404 al recargar la página.

1. Ve a [Vercel](https://vercel.com/) e importa tu repositorio de GitHub.
2. En la configuración del proyecto (Import Project):
   - En **Root Directory**, selecciona la carpeta `frontend`.
   - Vercel detectará automáticamente que es un proyecto **Vite**.
3. En la sección **Environment Variables**, añade:
   - Nombre: `VITE_API_URL`
   - Valor: La URL pública que obtuviste en Railway (ej. `https://gridmind-production.up.railway.app`).
4. Haz clic en **Deploy**.
5. Tras unos minutos, Vercel te dará la URL pública de tu frontend (ej. `gridmind.vercel.app`).

> [!IMPORTANT]
> Recuerda ir a Railway y actualizar la variable `FRONTEND_URL` con la URL final de Vercel (ej. `https://gridmind.vercel.app`) para que el backend acepte las peticiones (CORS). Tras cambiar la variable en Railway, el backend se reiniciará automáticamente.

---

## 4. Configurar un Dominio Personalizado

Para usar tu propio dominio (ej. `midominio.com`):

### Para el Frontend (Vercel):
Si compraste un dominio en Namecheap, GoDaddy, Cloudflare, etc.:
1. Ve a tu proyecto en Vercel y entra a **Settings -> Domains**.
2. Escribe tu dominio (ej. `app.midominio.com` o `midominio.com`) y haz clic en **Add**.
3. Vercel te mostrará unos registros **CNAME** o **A**.
4. Ve al panel de control de tu proveedor de dominio y añade esos registros DNS. En unos minutos, el dominio apuntará a Vercel.

### Para el Backend (Railway) - (Opcional pero recomendado):
Si quieres que tu API esté en tu dominio (ej. `api.midominio.com`):
1. En Railway, ve a **Settings -> Networking -> Custom Domains**.
2. Añade tu subdominio (`api.midominio.com`).
3. Railway te proporcionará un registro **CNAME** para que lo agregues en tu proveedor de dominio.
4. Recuerda actualizar `VITE_API_URL` en Vercel a esta nueva URL (`https://api.midominio.com`).

¡Y listo! Tu aplicación estará funcionando en la nube con su base de datos separada.
