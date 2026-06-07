-- =====================================================================
-- V2: Datos de demostracion (sin usuarios; los usuarios admin/externo
-- los crea DataInitializer con la contrasena ya encriptada en BCrypt).
-- Se usan INSERT ... SELECT por llaves naturales para no depender de ids.
-- =====================================================================

-- Empresas -------------------------------------------------------------
INSERT INTO empresa (nit, nombre, direccion, telefono) VALUES
    ('900123456-1', 'Lite Thinking S.A.S', 'Calle 100 #20-30, Bogota', '+57 601 7551234'),
    ('901987654-2', 'Andina Foods', 'Carrera 7 #45-12, Medellin', '+57 604 4445566'),
    ('800111222-3', 'TecnoGlobal Ltda', 'Av. El Dorado #68-90, Bogota', '+57 601 9998877');

-- Categorias -----------------------------------------------------------
INSERT INTO categoria (nombre) VALUES
    ('Tecnologia'), ('Alimentos'), ('Hogar'), ('Oficina');

-- Productos ------------------------------------------------------------
INSERT INTO producto (codigo, nombre, caracteristicas, precio_base, moneda_base, empresa_nit) VALUES
    ('LT-001', 'Licencia ERP Lite',     'Suscripcion anual modulo contable', 4500000.00, 'COP', '900123456-1'),
    ('LT-002', 'Soporte Premium',       'Soporte 24/7 con SLA de 4 horas',   1200000.00, 'COP', '900123456-1'),
    ('AF-010', 'Cafe Premium 1kg',      'Cafe de origen tostado medio',         85000.00, 'COP', '901987654-2'),
    ('TG-100', 'Laptop Pro 14',         'Portatil 16GB RAM, 512GB SSD',       6800000.00, 'COP', '800111222-3'),
    ('TG-101', 'Monitor 27 4K',         'Panel IPS 4K, 60Hz, USB-C',          1800000.00, 'COP', '800111222-3');

-- Relacion N-M Producto <-> Categoria ---------------------------------
INSERT INTO producto_categoria (producto_id, categoria_id)
SELECT p.id, c.id FROM producto p, categoria c
WHERE (p.codigo = 'LT-001' AND c.nombre IN ('Tecnologia', 'Oficina'))
   OR (p.codigo = 'LT-002' AND c.nombre IN ('Tecnologia'))
   OR (p.codigo = 'AF-010' AND c.nombre IN ('Alimentos'))
   OR (p.codigo = 'TG-100' AND c.nombre IN ('Tecnologia', 'Oficina'))
   OR (p.codigo = 'TG-101' AND c.nombre IN ('Tecnologia', 'Hogar'));

-- Inventario (productos por empresa) ----------------------------------
INSERT INTO inventario (empresa_nit, producto_id, cantidad)
SELECT p.empresa_nit, p.id, v.cantidad
FROM producto p
JOIN (VALUES
        ('LT-001', 50),
        ('LT-002', 120),
        ('AF-010', 300),
        ('TG-100', 25),
        ('TG-101', 40)
     ) AS v(codigo, cantidad) ON v.codigo = p.codigo;

-- Clientes -------------------------------------------------------------
INSERT INTO cliente (nombre, email, telefono) VALUES
    ('Comercializadora Norte', 'compras@norte.co', '+57 300 1112233'),
    ('Distribuidora Sur',      'ventas@sur.co',    '+57 301 4455667');

-- Ordenes y sus lineas (relacion N-M Orden <-> Producto) --------------
INSERT INTO orden (cliente_id, fecha, total)
SELECT c.id, TIMESTAMP '2026-05-10 10:30:00', 6900000.00
FROM cliente c WHERE c.email = 'compras@norte.co';

INSERT INTO orden_item (orden_id, producto_id, cantidad, precio_unitario)
SELECT o.id, p.id, x.cantidad, x.precio
FROM orden o
JOIN cliente c ON c.id = o.cliente_id AND c.email = 'compras@norte.co'
JOIN (VALUES
        ('LT-001', 1, 4500000.00),
        ('LT-002', 2, 1200000.00)
     ) AS x(codigo, cantidad, precio) ON TRUE
JOIN producto p ON p.codigo = x.codigo;
