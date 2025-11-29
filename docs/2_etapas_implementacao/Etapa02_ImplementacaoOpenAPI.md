## 9. Arquivo: /docs/2_etapas_de_implementacao/Etapa02_ImplementacaoOpenAPI.md

```markdown
# Etapa 02: Implementação OpenAPI

## Objetivo
Implementar documentação OpenAPI/Swagger para a API, permitindo exploração interativa e documentação detalhada dos endpoints.

## Componentes Afetados
- pom.xml (nova dependência)
- Nova classe: ConfiguracaoOpenAPI.java

## Tarefas a Realizar
1. Adicionar dependência do Springdoc OpenAPI
2. Criar classe de configuração do OpenAPI
3. Configurar informações básicas da API
4. Configurar esquema de segurança JWT
5. Documentar endpoints principais

## Código a Implementar

### Atualização do pom.xml
```xml
<!-- Adicionar dependência -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```
```java
package br.gov.md.parla_md_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenAPI {

    @Bean
    public OpenAPI openAPIPersonalizada() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Acompanhamento Legislativo")
                        .version("1.0")
                        .description("API para acompanhamento e análise de proposições legislativas")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("autenticacaoJWT"))
                .components(new Components()
                        .addSecuritySchemes("autenticacaoJWT",
                                new SecurityScheme()
                                        .name("autenticacaoJWT")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

### Exemplo de Documentação de Controlador
```java
@RestController
@RequestMapping("/api/publico/dados-legislativos")
@Tag(name = "Dados Legislativos", description = "Operações relacionadas a dados legislativos")
public class ControladorDadosLegislativos {

    @GetMapping("/proposicoes/{id}")
    @Operation(
            summary = "Buscar proposição por ID",
            description = "Retorna os detalhes de uma proposição específica com base no ID fornecido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposição encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proposicao.class))),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    public ResponseEntity<Proposicao> buscarProposicaoPorId(
            @Parameter(description = "ID da proposição", required = true) @PathVariable String id) {
        // Implementação
    }
}
```
## Testes

- Verificar se a interface Swagger UI está acessível em /swagger-ui.html
- Verificar se a especificação OpenAPI está disponível em /v3/api-docs
- Verificar se os endpoints documentados aparecem corretamente na UI
- Testar se é possível usar a UI para fazer chamadas de teste

## Dependências

* Etapa 01: Configuração de Segurança (para permitir acesso à documentação)

## Status

 [ ] Análise Concluída
 [ ] Implementação Iniciada
 [ ] Testes Realizados
 [ ] Revisão Concluída
 [ ] Implementação Concluída
