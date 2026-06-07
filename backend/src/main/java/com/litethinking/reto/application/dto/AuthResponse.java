package com.litethinking.reto.application.dto;

import com.litethinking.reto.domain.model.Rol;

/** Respuesta de autenticacion: token JWT y datos del usuario. */
public record AuthResponse(
        String token,
        String email,
        String nombre,
        Rol rol,
        long expiresInMs) {
}
