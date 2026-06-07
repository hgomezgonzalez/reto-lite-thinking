package com.litethinking.reto.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    // Clave base64 de 256 bits para pruebas.
    private static final String SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdW5pdC10ZXN0cy0yNTYtYml0cyEh";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 3600000L);
    }

    @Test
    void generaTokenYRecuperaElEmail() {
        String token = provider.generateToken("admin@litethinking.com", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(provider.isValid(token)).isTrue();
        assertThat(provider.getEmail(token)).isEqualTo("admin@litethinking.com");
    }

    @Test
    void rechazaTokenManipulado() {
        String token = provider.generateToken("user@litethinking.com", "EXTERNAL");

        assertThat(provider.isValid(token + "x")).isFalse();
    }
}
