package com.litethinking.reto.application.dto;

import com.litethinking.reto.domain.model.Moneda;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Datos de salida de un producto. El campo "precios" expone el precio
 * convertido a varias monedas (requisito b: "precio en varias monedas").
 */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String caracteristicas,
        BigDecimal precioBase,
        Moneda monedaBase,
        String empresaNit,
        String empresaNombre,
        List<CategoriaResponse> categorias,
        Map<String, BigDecimal> precios) {
}
