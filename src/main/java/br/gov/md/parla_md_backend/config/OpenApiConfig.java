package br.gov.md.parla_md_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.swagger-ui.path}")
    private String swaggerPath;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String SCHEME_NAME = "bearerAuth";
    private static final String SCHEME = "bearer";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Parla-MD")
                        .version("1.0")
                        .description("API para o sistema de acompanhamento legislativo Parla-MD. " +
                                   "Este sistema permite o monitoramento e análise de proposições " +
                                   "legislativas usando IA para predição e análise de impacto.")
                        .contact(new Contact()
                                .name("DETIC")
                                .email("email-suporte@md.gov.br")
                                .url("https://parla-md.gov.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.parla-md.gov.br")
                                .description("Servidor de Produção")))
                .tags(List.of(
                        new Tag().name("Proposições").description("Operações relacionadas a proposições legislativas"),
                        new Tag().name("Matérias").description("Operações relacionadas a matérias legislativas"),
                        new Tag().name("Parlamentares").description("Operações relacionadas a parlamentares"),
                        new Tag().name("Votações").description("Operações relacionadas a votações"),
                        new Tag().name("Análises").description("Operações relacionadas a análises de IA"),
                        new Tag().name("Monitoramento").description("Endpoints de monitoramento e saúde da aplicação")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(SCHEME)
                                .bearerFormat("JWT")
                                .description("JWT token de autenticação. Utilize o endpoint de login para obter o token.")));
    }
}
