package com.litethinking.reto.application.exception;

/** Se lanza ante una violacion de regla de negocio (mapea a HTTP 409/400). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
