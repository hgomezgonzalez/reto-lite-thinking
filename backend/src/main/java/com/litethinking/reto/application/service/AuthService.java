package com.litethinking.reto.application.service;

import com.litethinking.reto.application.dto.AuthResponse;
import com.litethinking.reto.application.dto.LoginRequest;
import com.litethinking.reto.domain.model.Usuario;
import com.litethinking.reto.infrastructure.persistence.UsuarioRepository;
import com.litethinking.reto.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Caso de uso de autenticacion (requisitos c y g). Valida las credenciales
 * mediante el AuthenticationManager (que compara el hash BCrypt) y, si son
 * correctas, emite un token JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        String token = tokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());
        return new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol(),
                tokenProvider.getExpirationMs());
    }
}
