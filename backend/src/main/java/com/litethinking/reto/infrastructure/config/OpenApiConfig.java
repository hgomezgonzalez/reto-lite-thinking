package com.litethinking.reto.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuracion de la documentacion OpenAPI / Swagger UI con auth Bearer. */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI retoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reto Tecnico Lite Thinking - API")
                        .version("1.0.0")
                        .description("API REST del reto tecnico. Autor: Hugo Ferney Gomez Gonzalez.")
                        .contact(new Contact()
                                .name("Hugo Ferney Gomez Gonzalez")
                                .email("hgomezgonzalez@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
