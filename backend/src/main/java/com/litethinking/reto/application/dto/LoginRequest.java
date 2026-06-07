package com.litethinking.reto.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciales de inicio de sesion (requisito c). */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
