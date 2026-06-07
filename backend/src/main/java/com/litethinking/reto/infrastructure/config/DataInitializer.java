package com.litethinking.reto.infrastructure.config;

import com.litethinking.reto.domain.model.Rol;
import com.litethinking.reto.domain.model.Usuario;
import com.litethinking.reto.infrastructure.persistence.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios de demostracion (administrador y externo) si aun no existen,
 * almacenando SIEMPRE la contrasena encriptada con BCrypt (requisito g).
 *
 * Credenciales entregables:
 *   Administrador -> admin@litethinking.com   / Admin123*
 *   Externo       -> externo@litethinking.com / Externo123*
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin@litethinking.com", "Admin123*", "Administrador Lite Thinking", Rol.ADMIN);
        crearSiNoExiste("externo@litethinking.com", "Externo123*", "Usuario Externo", Rol.EXTERNAL);
    }

    private void crearSiNoExiste(String email, String passwordPlano, String nombre, Rol rol) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
        usuarioRepository.save(usuario);
        log.info("Usuario {} creado con rol {}", email, rol);
    }
}
