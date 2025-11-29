package br.gov.md.parla_md_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenAPIConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpenApiConfig openApiConfig;

    @Test
    @DisplayName("Deve criar configuração OpenAPI com informações corretas")
    void testCustomOpenAPIConfiguration() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Verifica informações básicas
        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("API Parla-MD", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertTrue(info.getDescription().contains("sistema de acompanhamento legislativo"));
        
        // Verifica contato
        assertNotNull(info.getContact());
        assertEquals("<nome-responsavel>", info.getContact().getName());
        assertEquals("email-suporte@md.gov.br", info.getContact().getEmail());
        assertEquals("https://parla-md.gov.br", info.getContact().getUrl());
        
        // Verifica licença
        assertNotNull(info.getLicense());
        assertEquals("Apache 2.0", info.getLicense().getName());
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0.html", info.getLicense().getUrl());
        
        // Verifica servers
        assertNotNull(openAPI.getServers());
        assertEquals(2, openAPI.getServers().size());
        Server devServer = openAPI.getServers().get(0);
        assertEquals("http://localhost:8081", devServer.getUrl());
        assertEquals("Servidor de Desenvolvimento", devServer.getDescription());
        
        // Verifica tags
        assertNotNull(openAPI.getTags());
        assertTrue(openAPI.getTags().stream()
            .map(Tag::getName)
            .anyMatch(name -> name.equals("Proposições")));
        
        // Verifica configuração de segurança
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }

    @Test
    @DisplayName("Deve permitir acesso à interface Swagger UI")
    void testSwaggerUIEndpoint() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve permitir acesso à documentação OpenAPI")
    void testOpenAPIEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tags devem estar presentes e corretamente configuradas")
    void testOpenAPITags() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        var tags = openAPI.getTags();
        assertNotNull(tags);
        
        // Verifica todas as tags esperadas
        assertTrue(tags.stream().anyMatch(tag -> "Proposições".equals(tag.getName())));
        assertTrue(tags.stream().anyMatch(tag -> "Matérias".equals(tag.getName())));
        assertTrue(tags.stream().anyMatch(tag -> "Parlamentares".equals(tag.getName())));
        assertTrue(tags.stream().anyMatch(tag -> "Votações".equals(tag.getName())));
        assertTrue(tags.stream().anyMatch(tag -> "Análises".equals(tag.getName())));
        assertTrue(tags.stream().anyMatch(tag -> "Monitoramento".equals(tag.getName())));
        
        // Verifica descrições das tags
        assertTrue(tags.stream()
            .filter(tag -> "Proposições".equals(tag.getName()))
            .findFirst()
            .map(Tag::getDescription)
            .orElse("")
            .contains("proposições legislativas"));
    }

    @Test
    @DisplayName("Configuração de segurança deve estar corretamente definida")
    void testSecurityConfiguration() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Verifica SecurityScheme
        var securitySchemes = openAPI.getComponents().getSecuritySchemes();
        assertNotNull(securitySchemes);
        
        SecurityScheme bearerAuth = securitySchemes.get("bearerAuth");
        assertNotNull(bearerAuth);
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
        assertTrue(bearerAuth.getDescription().contains("token de autenticação"));
        
        // Verifica SecurityRequirement
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
    }
}
