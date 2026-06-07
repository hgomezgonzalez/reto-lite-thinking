package com.litethinking.reto.domain.model;

/**
 * Tipos de usuario soportados por la aplicacion (requisito e del reto).
 * ADMIN    -> CRUD de empresas, registro de productos e inventario.
 * EXTERNAL -> Solo visualizacion de empresas (visitante).
 */
public enum Rol {
    ADMIN,
    EXTERNAL
}
