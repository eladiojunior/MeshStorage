package br.com.devd2.meshstorageserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Bean
    public OpenApiCustomizer auditHeadersCustomizer() {

        /* ----- X‑User‑Name ------------------------------------------------ */
        Parameter userHeader = new Parameter()
                .in("header")
                .name("X-User-Name")
                .description("Identidade lógica do usuário logado")
                .schema(new StringSchema())
                .example("eladio.junior")
                .required(false);

        /* ----- X‑Access‑Channel ------------------------------------------ */
        Parameter channelHeader = new Parameter()
                .in("header")
                .name("X-Access-Channel")
                .description("Canal da requisição (Site, Mobile, Chat…)")
                .schema(new StringSchema()
                        ._enum(List.of("Site", "Mobile", "Chat")))
                .example("Mobile")
                .required(false);

        /* ----- Customizer que injeta os headers nos paths desejados ------- */
        return (OpenAPI openApi) -> openApi.getPaths().forEach((path, item) -> {
            if (path.startsWith("/api/v1/file/download") ||
                    path.startsWith("/api/v1/file/link")) {
                addHeader(item, userHeader);
                addHeader(item, channelHeader);
            }
        });
    }

    /* helper */
    private static void addHeader(PathItem item, Parameter param) {
        item.readOperations().forEach(op -> op.addParametersItem(param));
    }

}