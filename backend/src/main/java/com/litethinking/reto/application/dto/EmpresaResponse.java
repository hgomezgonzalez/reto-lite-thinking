package com.litethinking.reto.application.dto;

/** Datos de salida de una empresa. */
public record EmpresaResponse(
        String nit,
        String nombre,
        String direccion,
        String telefono) {
}
