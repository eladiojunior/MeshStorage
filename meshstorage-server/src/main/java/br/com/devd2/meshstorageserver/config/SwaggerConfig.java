package br.com.devd2.meshstorageserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // Define o esquema de segurança (Bearer Token)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // Aplica o esquema de segurança globalmente
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Mesh Storage API")
                        .version("1.0")
                        .description("API para gestão de servidores de armazenamento e registro de arquivos físicos de forma centralizada")
                        .contact(new Contact().name("DevD2 - Soluções Tecnológicas").email("atendimento@devd2.com.br").url("https://wwww.devd2.com.br"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }

}