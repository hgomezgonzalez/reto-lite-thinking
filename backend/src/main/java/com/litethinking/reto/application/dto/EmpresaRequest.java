package com.litethinking.reto.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos de entrada de una empresa (requisito a). */
public record EmpresaRequest(
        @NotBlank @Size(max = 30) String nit,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 200) String direccion,
        @Size(max = 50) String telefono) {
}
