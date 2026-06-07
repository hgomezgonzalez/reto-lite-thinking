package com.litethinking.reto.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Alta o ajuste de stock de un producto en una empresa. */
public record InventarioRequest(
        @NotBlank String empresaNit,
        @NotNull Long productoId,
        @NotNull @PositiveOrZero Integer cantidad) {
}
