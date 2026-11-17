package com.petvitaliz.config;

import com.petvitaliz.security.TokenType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        Components components = new Components();
        for (TokenType type : TokenType.values()) {
            components.addSecuritySchemes(type.getCookieName(), new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .name(type.getCookieName()));
        }

        return new OpenAPI()
                .info(new Info()
                        .title("PetVitaliz API")
                        .version("1.0.0")
                        .description("Documentação da API"))
                .servers(List.of(new Server().url("/")))
                .components(components);
    }
}
