# Modelo Entidad-Relación

Cumple el requisito (f): Empresa, Productos, Categorías, Clientes y Órdenes, con las reglas:
- Un Producto puede pertenecer a **múltiples Categorías** (N–M).
- Un Cliente puede tener **múltiples Órdenes** (1–N).
- Las Órdenes pueden tener **múltiples Productos** (N–M, vía `orden_item`).

```mermaid
erDiagram
    EMPRESA ||--o{ PRODUCTO : tiene
    EMPRESA ||--o{ INVENTARIO : registra
    PRODUCTO ||--o{ INVENTARIO : "stock por empresa"
    PRODUCTO }o--o{ CATEGORIA : clasifica
    CLIENTE ||--o{ ORDEN : realiza
    ORDEN ||--o{ ORDEN_ITEM : contiene
    PRODUCTO ||--o{ ORDEN_ITEM : "aparece en"
    USUARIO {
        bigint id PK
        string email UK
        string password_hash
        string rol
    }
    EMPRESA {
        string nit PK
        string nombre
        string direccion
        string telefono
    }
    CATEGORIA {
        bigint id PK
        string nombre UK
    }
    PRODUCTO {
        bigint id PK
        string codigo UK
        string nombre
        text caracteristicas
        numeric precio_base
        string moneda_base
        string empresa_nit FK
    }
    PRODUCTO_CATEGORIA {
        bigint producto_id FK
        bigint categoria_id FK
    }
    CLIENTE {
        bigint id PK
        string nombre
        string email UK
        string telefono
    }
    ORDEN {
        bigint id PK
        bigint cliente_id FK
        timestamp fecha
        numeric total
    }
    ORDEN_ITEM {
        bigint id PK
        bigint orden_id FK
        bigint producto_id FK
        int cantidad
        numeric precio_unitario
    }
    INVENTARIO {
        bigint id PK
        string empresa_nit FK
        bigint producto_id FK
        int cantidad
    }
```

El DDL completo está en `backend/src/main/resources/db/migration/V1__schema.sql` (aplicado por Flyway).
