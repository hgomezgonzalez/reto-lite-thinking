package com.litethinking.reto.infrastructure.email;

import com.litethinking.reto.application.exception.BusinessException;
import com.litethinking.reto.application.port.EmailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Adaptador de envio de correo por SMTP (implementa el puerto EmailSender).
 * Reutiliza el mismo enfoque que el proyecto rohu-expenses: configuracion por
 * variables de entorno, MailHog en local y Gmail (App Password) en produccion.
 */
@Component
public class SmtpEmailAdapter implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailAdapter(JavaMailSender mailSender,
                            @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendWithAttachment(String to, String subject, String body,
                                   byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo enviar el correo: " + ex.getMessage());
        }
    }
}
