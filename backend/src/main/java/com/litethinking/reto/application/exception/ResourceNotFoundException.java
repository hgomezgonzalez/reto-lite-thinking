package com.litethinking.reto.application.exception;

/** Se lanza cuando no existe el recurso solicitado (mapea a HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
