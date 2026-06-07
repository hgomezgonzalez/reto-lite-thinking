package com.litethinking.reto.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Destinatario al que se enviara el PDF del inventario (requisito d). */
public record EmailRequest(
        @NotBlank @Email String to) {
}
