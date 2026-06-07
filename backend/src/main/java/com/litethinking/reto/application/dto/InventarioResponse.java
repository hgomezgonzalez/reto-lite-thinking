package com.litethinking.reto.application.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Fila de inventario: producto por empresa con su cantidad y precios. */
public record InventarioResponse(
        Long id,
        String empresaNit,
        String empresaNombre,
        Long productoId,
        String productoCodigo,
        String productoNombre,
        Integer cantidad,
        BigDecimal precioBase,
        String monedaBase,
        Map<String, BigDecimal> precios) {
}
