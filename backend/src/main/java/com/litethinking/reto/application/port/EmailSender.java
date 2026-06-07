package com.litethinking.reto.application.port;

/**
 * Puerto de envio de correo con adjunto. La implementacion concreta (SMTP,
 * API REST de un proveedor, etc.) vive en la capa de infraestructura.
 */
public interface EmailSender {

    void sendWithAttachment(String to,
                            String subject,
                            String body,
                            byte[] attachment,
                            String attachmentName);
}
