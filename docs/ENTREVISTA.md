# Guía para la entrevista — explicación de cada componente

Este documento explica **cómo está construida la solución y por qué**, para que puedas
sustentar cualquier decisión técnica. Está ordenado de lo general a lo específico.

---

## 1. Visión general de la arquitectura

La aplicación tiene dos piezas desplegables por separado:

- **Backend** (API REST en Spring Boot) → expone `/api/**`, habla con PostgreSQL.
- **Frontend** (SPA en Quasar/Vue) → consume la API con Axios y JWT.

El backend sigue **Clean Architecture** en 4 capas con la regla de dependencia
apuntando siempre hacia adentro (las capas externas conocen a las internas, nunca al revés):

```
presentation  (controladores REST, manejo de errores)
      │  depende de
application   (casos de uso/servicios, DTOs, puertos, mappers)
      │  depende de
domain        (entidades del negocio: Empresa, Producto, Orden...)
      ▲
infrastructure (adaptadores: JPA, JWT, SMTP, PDF, API de monedas)  -- implementa los puertos de application
```

**Por qué:** separar responsabilidades hace el código testeable (puedo probar un servicio
sin levantar la base de datos), mantenible (cambiar de proveedor de correo no toca la lógica)
y alineado con SOLID.

---

## 2. Cómo se ven los principios SOLID en el código

- **S (Responsabilidad única):** cada clase hace una cosa. `JwtTokenProvider` solo emite/valida
  tokens; `OpenPdfAdapter` solo arma el PDF; `EmpresaService` solo orquesta reglas de empresa.
- **O (Abierto/Cerrado):** puedo añadir un nuevo proveedor de correo o de tasas creando un nuevo
  adaptador que implemente el puerto, sin modificar los servicios.
- **L (Sustitución de Liskov):** cualquier implementación de `CurrencyConverter` o `EmailSender`
  es intercambiable porque respetan el contrato de la interfaz.
- **I (Segregación de interfaces):** los puertos son pequeños y específicos
  (`CurrencyConverter`, `EmailSender`, `PdfGenerator`) en vez de una interfaz "gigante".
- **D (Inversión de dependencias):** `application` depende de **interfaces** (puertos), no de
  clases concretas de `infrastructure`. Spring inyecta la implementación en tiempo de ejecución.

---

## 3. Backend componente por componente

### 3.1 `domain/model` — las entidades
Son las clases JPA que mapean a las tablas. Lo importante para la entrevista:

- **`Empresa`**: el **NIT es la llave primaria natural** (`@Id` sobre un `String`), tal como pide
  el reto. Tiene `@OneToMany` hacia `Producto`.
- **`Producto`**: guarda `precioBase` + `monedaBase` (enum). **No** guardo el precio en cada
  moneda: guardo uno y convierto al vuelo (evita datos desincronizados). Relación `@ManyToOne`
  con `Empresa` y `@ManyToMany` con `Categoria` (tabla intermedia `producto_categoria`).
- **`Categoria`**: lado inverso del N–M con producto.
- **`Cliente`**: `@OneToMany` hacia `Orden` (1–N).
- **`Orden`** y **`OrdenItem`**: la relación N–M entre Orden y Producto se modela con una
  **entidad intermedia** `OrdenItem` porque la relación **tiene atributos propios** (cantidad y
  precio unitario). Es la forma correcta de un N–M "enriquecido".
- **`Inventario`**: par único (empresa, producto) + cantidad → "productos por empresa".
- **`Usuario`**: email único, `passwordHash` (BCrypt) y `rol` (enum).

> Uso `Lombok` (`@Getter/@Setter`) solo para reducir el boilerplate de getters/setters.

### 3.2 `infrastructure/persistence` — repositorios
Interfaces que extienden `JpaRepository`. Spring Data **genera la implementación** en tiempo de
ejecución. Métodos derivados por nombre como `findByEmpresaNit(...)` o `existsByCodigo(...)`
generan el SQL automáticamente. Patrón **Repository**.

### 3.3 `application` — la lógica de negocio
- **DTOs (`record`)**: objetos inmutables para entrada/salida. **Nunca expongo las entidades JPA**
  directamente en la API (evita exponer estructura interna y problemas de serialización LAZY).
- **Mappers**: clases utilitarias que convierten entidad ↔ DTO de forma explícita y fácil de leer.
- **Servicios** (`@Service`, `@Transactional`): los casos de uso.
  - `AuthService`: valida credenciales con el `AuthenticationManager` (que compara el hash BCrypt)
    y emite el JWT.
  - `EmpresaService`, `ProductoService`, `InventarioService`, `CategoriaService`.
  - `ProductoService.create()` además registra el stock inicial en inventario en un solo paso.
  - `InventarioService` arma la lista, genera el PDF (delegando en el puerto) y dispara el correo.
- **Puertos** (`port/`): interfaces `CurrencyConverter`, `EmailSender`, `PdfGenerator`. Aquí está
  la **inversión de dependencias**.
- **Excepciones**: `ResourceNotFoundException` (404) y `BusinessException` (409).

### 3.4 `infrastructure` — los adaptadores
- **`security/`**:
  - `JwtTokenProvider`: firma HS256 con clave de configuración; `generateToken`/`isValid`/`getEmail`.
  - `JwtAuthenticationFilter`: intercepta cada request, lee `Authorization: Bearer ...`, valida y
    pone la autenticación en el `SecurityContext`.
  - `CustomUserDetailsService`: carga el usuario de la BD y expone el rol como `ROLE_ADMIN`/`ROLE_EXTERNAL`.
  - `SecurityConfig`: define `BCryptPasswordEncoder`, sesión **stateless**, CORS y las **reglas de
    autorización** (lectura para ambos roles, escritura solo ADMIN).
- **`currency/ExchangeRateApiAdapter`**: implementa `CurrencyConverter`. Llama a una **API REST**
  pública de tasas y, si falla, usa **tasas de respaldo** estáticas. Cachea 1 hora. Patrón
  **Strategy/Adapter** + tolerancia a fallos.
- **`email/SmtpEmailAdapter`**: implementa `EmailSender` con `JavaMailSender` (mismo enfoque que el
  proyecto rohu-expenses: config por entorno, MailHog en local, Gmail App Password en prod).
- **`pdf/OpenPdfAdapter`**: implementa `PdfGenerator` con OpenPDF; arma una tabla corporativa y un
  pie de página con los datos de contacto.
- **`config/`**: `DataInitializer` (crea los usuarios admin/externo con contraseña encriptada) y
  `OpenApiConfig` (Swagger con esquema Bearer).

### 3.5 `presentation` — los controladores
Controladores REST finos: validan el `@RequestBody` (Bean Validation) y delegan en el servicio.
`GlobalExceptionHandler` (`@RestControllerAdvice`) traduce las excepciones a códigos HTTP
consistentes (404, 409, 401, 403, 400, 500) con un cuerpo de error uniforme.

### 3.6 Migraciones (Flyway)
- `V1__schema.sql`: crea todas las tablas y relaciones.
- `V2__seed_demo.sql`: datos de demostración.
- **Por qué Flyway:** versiona el esquema, es reproducible y separa el control del esquema de
  Hibernate (`ddl-auto=none`), que es la práctica recomendada en producción.
- **Esquema parametrizable** (`DB_SCHEMA`, por defecto `reto_lite`): permite convivir con otras
  apps en la misma base compartida de Heroku.

---

## 4. Frontend componente por componente

- **`main.js`**: arranca Vue, registra Pinia, el router y Quasar (plugins Notify/Dialog/Loading).
- **`quasar-variables.sass`**: paleta corporativa (azul `#0D47A1`) → imagen elegante y consistente.
- **`services/api.js`**: instancia de Axios con `baseURL=/api` y un **interceptor** que adjunta el
  JWT en cada petición.
- **`stores/auth.js`** (Pinia): guarda token + usuario, los persiste en `localStorage` y expone
  `isAdmin`/`isAuthenticated`. Es la fuente de verdad de la sesión.
- **`router/index.js`**: define las rutas y un **guard global** que: exige login, bloquea rutas de
  admin para externos y redirige al dashboard si ya estás logueado.
- **`layouts/MainLayout.vue`**: header, menú lateral según rol, botón salir y **footer con los datos
  del autor**.
- **Páginas**:
  - `LoginPage.vue`: formulario de acceso con validación (incluye las credenciales demo).
  - `EmpresasPage.vue`: tabla `q-table`; el admin ve botones de crear/editar/eliminar, el externo
    solo lectura.
  - `ProductosPage.vue`: formulario con select de empresa, multiselección de categorías, precio +
    moneda y cantidad inicial de inventario; la tabla muestra los **precios en varias monedas**.
  - `InventarioPage.vue`: filtro por empresa, **descarga del PDF** (blob) y diálogo para
    **enviar el PDF a un correo**.

---

## 5. Flujos clave (para explicar "de punta a punta")

**Login:** LoginPage → `POST /api/auth/login` → `AuthController` → `AuthService` valida con
`AuthenticationManager` (compara BCrypt) → emite JWT → el frontend lo guarda → el interceptor lo
manda en cada request → `JwtAuthenticationFilter` lo valida en el backend.

**Precio multimoneda:** al pedir productos, `ProductoService` llama a `CurrencyConverter` que
obtiene tasas (API o respaldo) y devuelve un mapa `{COP, USD, EUR}` que la tabla pinta como chips.

**PDF + correo:** InventarioPage → `GET /api/inventario/pdf` (descarga) o
`POST /api/inventario/pdf/email` → `InventarioService` arma las filas, `OpenPdfAdapter` genera el
PDF y `SmtpEmailAdapter` lo envía como adjunto.

---

## 6. Preguntas típicas y respuestas cortas

- **¿Por qué JWT y no sesión?** API stateless → escala horizontal sin estado de sesión en el servidor.
- **¿Por qué BCrypt?** Hash con salt y costo configurable, estándar para contraseñas (requisito g).
- **¿Por qué no guardas el precio en cada moneda?** Para no desincronizar datos; guardo uno y
  convierto con tasas actualizadas.
- **¿Por qué OrdenItem en vez de un N–M simple?** La relación tiene atributos (cantidad, precio).
- **¿Cómo evitas exponer las entidades?** Uso DTOs (records) en toda la frontera de la API.
- **¿Cómo pruebas sin base de datos?** Servicios con Mockito; el adaptador de monedas con una URL
  inválida que fuerza el respaldo.
- **¿Cómo escala el modelo de despliegue?** Backend y frontend independientes; la BD usa un esquema
  propio para convivir con otras apps.
