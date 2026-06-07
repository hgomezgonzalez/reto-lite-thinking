# Reto Técnico — Lite Thinking

Aplicación web full-stack que gestiona **Empresas, Productos (precio en varias monedas), Inventario, Categorías, Clientes y Órdenes**, con autenticación por roles (Administrador / Externo), generación de PDF del inventario y envío por correo.

> **Autor:** Hugo Ferney Gómez González · ✉️ hgomezgonzalez@gmail.com · 📱 +57 3168343318

## 🌐 Aplicación desplegada (Heroku) + Repositorio

| Recurso | URL |
|---------|-----|
| **Repositorio (GitHub)** | https://github.com/hgomezgonzalez/reto-lite-thinking |
| **Frontend (app)** | https://reto-lite-web-662d3247a972.herokuapp.com |
| **Backend (API)** | https://reto-lite-api-4f089b9f56e9.herokuapp.com |
| **Swagger UI** | https://reto-lite-api-4f089b9f56e9.herokuapp.com/swagger-ui.html |

> **CI/CD:** cada push a `main` en GitHub dispara el workflow `.github/workflows/deploy.yml`,
> que despliega automáticamente el backend a `reto-lite-api` y el frontend a `reto-lite-web`
> (GitHub Actions → Heroku).

**Credenciales:** Administrador `admin@litethinking.com` / `Admin123*` · Externo `externo@litethinking.com` / `Externo123*`

> Backend y frontend son apps Heroku independientes. El backend usa la base de datos PostgreSQL
> compartida con un **esquema propio `reto_lite`**.

---

## 1. Stack tecnológico

| Capa        | Tecnología |
|-------------|------------|
| Backend     | Java 17, Spring Boot 3.3, Spring Security (JWT), Hibernate/JPA, Flyway, OpenPDF, springdoc-openapi |
| Frontend    | Vue 3 + **Quasar Framework**, Pinia, Vue Router, Axios, Vite |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker Compose (Postgres + MailHog + backend + frontend), despliegue en Heroku |

Arquitectura del backend: **Clean Architecture** (domain → application → infrastructure → presentation) con principios **SOLID**, patrón **Ports & Adapters**, DTOs/records, repositorios Spring Data y manejo global de errores.

---

## 2. Cumplimiento de los requisitos del reto

| Requisito | Implementación |
|-----------|----------------|
| a) Vista Empresa (NIT PK, nombre, dirección, teléfono) | `EmpresasPage.vue` + `EmpresaController` / `Empresa` (NIT como `@Id`) |
| b) Vista Productos (código, nombre, características, **precio en varias monedas**, empresa) | `ProductosPage.vue` + `ProductoController`; conversión COP/USD/EUR vía `CurrencyConverter` |
| c) Vista Login (correo + contraseña) | `LoginPage.vue` + `AuthController` (JWT) |
| d) Vista Inventario: **descargar PDF** + **enviar PDF por API REST a un correo** | `InventarioPage.vue` + `GET /api/inventario/pdf` y `POST /api/inventario/pdf/email` (OpenPDF + JavaMailSender) |
| e) Roles Administrador / Externo | Spring Security por rol; admin = CRUD empresa + registrar productos/inventario; externo = solo lectura |
| f) Modelo ER (Empresa, Productos, Categorías, Clientes, Órdenes) | Producto **N–M** Categoría · Cliente **1–N** Orden · Orden **N–M** Producto (`orden_item`) |
| g) Contraseña encriptada | **BCrypt** (`BCryptPasswordEncoder`), hash almacenado en BD |

---

## 3. Credenciales de prueba

| Rol | Correo | Contraseña |
|-----|--------|-----------|
| Administrador | `admin@litethinking.com` | `Admin123*` |
| Externo | `externo@litethinking.com` | `Externo123*` |

Estos usuarios se crean automáticamente al arrancar (`DataInitializer`) con la contraseña ya encriptada.

---

## 4. Ejecución local

### Opción A — Docker Compose (recomendada)

```bash
docker compose up --build
```

- Frontend: http://localhost:8081
- Backend / Swagger: http://localhost:8080/swagger-ui.html
- MailHog (bandeja de correos): http://localhost:8025

### Opción B — Manual (sin Docker)

**Requisitos:** Java 17+, Maven, Node 18+, PostgreSQL.

1. Crear la base de datos y exportar las variables (ver `.env.example`):

```bash
createdb reto_db
export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/reto_db
export JDBC_DATABASE_USERNAME=tu_usuario
export JDBC_DATABASE_PASSWORD=tu_password
export DB_SCHEMA=reto_lite
```

2. Backend:

```bash
cd backend
mvn spring-boot:run        # o: mvn package && java -jar target/reto-backend.jar
```

3. Frontend:

```bash
cd frontend
npm install
npm run dev                 # http://localhost:9000 (proxy /api -> :8080)
```

> Para el envío de correo en local sin Docker puedes levantar un SMTP de pruebas:
> `python3 -m smtpd -n -c DebuggingServer localhost:1025`

---

## 5. Configuración (variables de entorno)

Ver **`.env.example`**. Las más relevantes:

- `JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, `JDBC_DATABASE_PASSWORD`, `DB_SCHEMA`
- `JWT_SECRET` (base64, ≥256 bits), `JWT_EXPIRATION_MS`
- `SMTP_HOST/PORT/USER/PASSWORD/AUTH/TLS/FROM` (Gmail App Password en producción)
- `CORS_ALLOWED_ORIGINS`
- `CURRENCY_BASE/TARGETS/API_URL`

---

## 6. Despliegue en Heroku

Dos aplicaciones separadas; el backend usa la **base de datos PostgreSQL compartida** con un **esquema propio `reto_lite`**.

### Backend (`reto-lite-api`)

```bash
heroku create reto-lite-api
# Reutiliza la BD compartida (attach) o crea un addon:
heroku config:set DB_SCHEMA=reto_lite JWT_SECRET=... SMTP_HOST=smtp.gmail.com SMTP_PORT=587 \
  SMTP_AUTH=true SMTP_TLS=true SMTP_USER=... SMTP_PASSWORD=... \
  CORS_ALLOWED_ORIGINS=https://reto-lite-web.herokuapp.com -a reto-lite-api
git subtree push --prefix backend heroku main   # o despliegue por contenedor
```

`backend/system.properties` fija Java 17 y `backend/Procfile` define el proceso web.

### Frontend (`reto-lite-web`)

```bash
heroku create reto-lite-web
heroku config:set VITE_API_URL=https://reto-lite-api.herokuapp.com/api -a reto-lite-web
git subtree push --prefix frontend heroku main
```

`frontend/server.js` (Express) sirve el `dist/` y `heroku-postbuild` ejecuta el build de Vite.

---

## 7. Documentación de API

Swagger UI: `http://localhost:8080/swagger-ui.html` (botón **Authorize** → `Bearer <token>`).

| Método | Endpoint | Rol |
|--------|----------|-----|
| POST | `/api/auth/login` | público |
| GET | `/api/empresas` | ADMIN, EXTERNAL |
| POST/PUT/DELETE | `/api/empresas` | ADMIN |
| GET | `/api/productos` | ADMIN, EXTERNAL |
| POST/PUT/DELETE | `/api/productos` | ADMIN |
| GET | `/api/inventario` | ADMIN, EXTERNAL |
| POST | `/api/inventario` | ADMIN |
| GET | `/api/inventario/pdf` | ADMIN, EXTERNAL |
| POST | `/api/inventario/pdf/email` | ADMIN, EXTERNAL |

---

## 8. Pruebas

```bash
cd backend && mvn test
```

Incluye pruebas de `JwtTokenProvider`, `EmpresaService` (Mockito) y `ExchangeRateApiAdapter` (tasas de respaldo).

---

## 9. Estructura del repositorio

```
.
├── backend/     Spring Boot (Clean Architecture)
├── frontend/    Quasar / Vue 3
├── db/          Diagrama ER y SQL de referencia
├── docs/        ENTREVISTA.md (explicación componente por componente)
├── docker-compose.yml
└── .env.example
```

---

## 10. Para la entrevista

El archivo **`docs/ENTREVISTA.md`** explica cada componente, las decisiones de arquitectura y los patrones aplicados, pensado para sustentar la solución.
