package com.litethinking.reto.application.dto;

import com.litethinking.reto.domain.model.Moneda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Datos de entrada de un producto (requisito b). Incluye la cantidad inicial
 * de inventario para que el administrador registre el producto por empresa y
 * lo guarde en la tabla inventario en un solo paso (requisito e).
 */
public record ProductoRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,
        String caracteristicas,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioBase,
        @NotNull Moneda monedaBase,
        @NotBlank String empresaNit,
        Set<Long> categoriaIds,
        @PositiveOrZero Integer cantidadInventario) {
}
