package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.config.SecurityConfig;
import br.gov.md.parla_md_backend.domain.dto.AutenticacaoConfigDTO;
import br.gov.md.parla_md_backend.domain.dto.PermissaoDTO;
import br.gov.md.parla_md_backend.domain.dto.TokenValidationDTO;
import br.gov.md.parla_md_backend.domain.dto.UsuarioInfoDTO;
import br.gov.md.parla_md_backend.service.AutenticacaoApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutenticacaoController.class)
@DisplayName("Testes do AutenticacaoController")
@Import(SecurityConfig.class)
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutenticacaoApiService autenticacaoApiService;

    private AutenticacaoConfigDTO configDTO;
    private UsuarioInfoDTO usuarioInfoDTO;

    @BeforeEach
    void setUp() {
        // Preparar AutenticacaoConfigDTO
        configDTO = new AutenticacaoConfigDTO();
        configDTO.setKeycloakUrl("http://localhost:8080");
        configDTO.setRealm("parla-md-realm");
        configDTO.setClientId("parla-md-frontend");
        configDTO.setLoginUrl("http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/auth");
        configDTO.setLogoutUrl("http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/logout");
        configDTO.setTokenUrl("http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/token");
        configDTO.setUserinfoUrl("http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/userinfo");

        // Preparar UsuarioInfoDTO
        usuarioInfoDTO = new UsuarioInfoDTO();
        usuarioInfoDTO.setId("123e4567-e89b-12d3-a456-426614174000");
        usuarioInfoDTO.setUsername("joao.silva");
        usuarioInfoDTO.setEmail("joao.silva@md.gov.br");
        usuarioInfoDTO.setPrimeiroNome("João");
        usuarioInfoDTO.setUltimoNome("Silva");
        usuarioInfoDTO.setNomeCompleto("João Silva");
        usuarioInfoDTO.setRoles(Set.of("ADMIN", "GESTOR"));
        usuarioInfoDTO.setPermissoes(Set.of("READ_PROCESSOS", "WRITE_PROCESSOS"));
        usuarioInfoDTO.setAtivo(true);
        usuarioInfoDTO.setEmailVerificado(true);
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/config")
    class TestesConfiguracao {

        @Test
        @DisplayName("Deve retornar configurações públicas do Keycloak")
        void deveRetornarConfiguracoesPublicasDoKeycloak() throws Exception {
            AutenticacaoConfigDTO config = AutenticacaoConfigDTO.builder()
                    .keycloakUrl("http://localhost:8080")
                    .realm("parla-md-realm")
                    .clientId("parla-md-frontend")
                    // Campos necessários para que isConfiguracaoValida() retorne TRUE:
                    .loginUrl("http://localhost:8080/realms/auth")
                    .logoutUrl("http://localhost:8080/realms/logout")
                    .tokenUrl("http://localhost:8080/realms/token")
                    .userinfoUrl("http://localhost:8080/realms/userinfo")
                    .build();

            when(autenticacaoApiService.obterConfiguracaoPublica()).thenReturn(config);

            mockMvc.perform(get("/api/auth/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.configuracaoValida").value(true)) // Agora retornará true
                    .andExpect(jsonPath("$.keycloak_url").value("http://localhost:8080"))
                    .andExpect(jsonPath("$.realm").value("parla-md-realm"))
                    .andExpect(jsonPath("$.client_id").value("parla-md-frontend"));
        }

        @Test
        @DisplayName("Deve permitir acesso sem autenticação")
        void devePermitirAcessoSemAutenticacao() throws Exception {
            when(autenticacaoApiService.obterConfiguracaoPublica()).thenReturn(configDTO);

            mockMvc.perform(get("/api/auth/config"))
                    .andExpect(status().isOk());

            verify(autenticacaoApiService).obterConfiguracaoPublica();
        }

        @Test
        @DisplayName("Deve retornar 500 quando ocorrer erro ao obter configuração")
        void deveRetornar500QuandoOcorrerErroAoObterConfiguracao() throws Exception {
            when(autenticacaoApiService.obterConfiguracaoPublica())
                    .thenThrow(new RuntimeException("Erro ao conectar com Keycloak"));

            mockMvc.perform(get("/api/auth/config"))
                    .andExpect(status().isInternalServerError());

            verify(autenticacaoApiService).obterConfiguracaoPublica();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/health")
    class TestesSaude {

        @Test
        @DisplayName("Deve retornar status UP quando sistema está saudável")
        void deveRetornarStatusUpQuandoSistemaSaudavel() throws Exception {
            when(autenticacaoApiService.verificarSaudeAutenticacao()).thenReturn(true);

            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.keycloak").value("CONNECTED"))
                    .andExpect(jsonPath("$.service").value("autenticacao"));

            verify(autenticacaoApiService).verificarSaudeAutenticacao();
        }

        @Test
        @DisplayName("Deve retornar status DOWN quando sistema não está saudável")
        void deveRetornarStatusDownQuandoSistemaNaoSaudavel() throws Exception {
            when(autenticacaoApiService.verificarSaudeAutenticacao()).thenReturn(false);

            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("DOWN"))
                    .andExpect(jsonPath("$.keycloak").value("DISCONNECTED"));

            verify(autenticacaoApiService).verificarSaudeAutenticacao();
        }

        @Test
        @DisplayName("Deve retornar 503 quando ocorrer exceção no health check")
        void deveRetornar503QuandoOcorrerExcecaoNoHealthCheck() throws Exception {
            when(autenticacaoApiService.verificarSaudeAutenticacao())
                    .thenThrow(new RuntimeException("Erro de conexão"));

            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("DOWN"))
                    .andExpect(jsonPath("$.error").exists());

            verify(autenticacaoApiService).verificarSaudeAutenticacao();
        }

        @Test
        @DisplayName("Deve permitir acesso sem autenticação")
        void devePermitirAcessoSemAutenticacao() throws Exception {
            when(autenticacaoApiService.verificarSaudeAutenticacao()).thenReturn(true);

            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isOk());

            verify(autenticacaoApiService).verificarSaudeAutenticacao();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/user-info")
    class TestesUserInfo {

        @Test
        @DisplayName("Deve retornar informações do usuário autenticado")
        @WithMockUser(username = "joao.silva", roles = {"ADMIN", "GESTOR"})
        void deveRetornarInformacoesDoUsuarioAutenticado() throws Exception {
            when(autenticacaoApiService.obterInformacoesUsuarioAtual()).thenReturn(usuarioInfoDTO);

            mockMvc.perform(get("/api/auth/user-info"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("123e4567-e89b-12d3-a456-426614174000"))
                    .andExpect(jsonPath("$.username").value("joao.silva"))
                    .andExpect(jsonPath("$.email").value("joao.silva@md.gov.br"))
                    .andExpect(jsonPath("$.nome_completo").value("João Silva"))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.roles").isArray());

            verify(autenticacaoApiService).obterInformacoesUsuarioAtual();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(get("/api/auth/user-info"))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).obterInformacoesUsuarioAtual();
        }

        @Test
        @DisplayName("Deve retornar 500 quando ocorrer erro ao obter informações")
        @WithMockUser
        void deveRetornar500QuandoOcorrerErroAoObterInformacoes() throws Exception {
            when(autenticacaoApiService.obterInformacoesUsuarioAtual())
                    .thenThrow(new RuntimeException("Erro ao processar token"));

            mockMvc.perform(get("/api/auth/user-info"))
                    .andExpect(status().isInternalServerError());

            verify(autenticacaoApiService).obterInformacoesUsuarioAtual();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/permissions")
    class TestesPermissoes {

        @Test
        @DisplayName("Deve retornar permissões do usuário autenticado")
        @WithMockUser
        void deveRetornarPermissoesDoUsuarioAutenticado() throws Exception {
            Set<String> permissoes = Set.of("READ_PROCESSOS", "WRITE_PROCESSOS", "DELETE_PROCESSOS");
            when(autenticacaoApiService.extrairPermissoesDoUsuario()).thenReturn(permissoes);

            mockMvc.perform(get("/api/auth/permissions"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            verify(autenticacaoApiService).extrairPermissoesDoUsuario();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(get("/api/auth/permissions"))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).extrairPermissoesDoUsuario();
        }

        @Test
        @DisplayName("Deve retornar conjunto vazio quando usuário não tem permissões")
        @WithMockUser
        void deveRetornarConjuntoVazioQuandoUsuarioNaoTemPermissoes() throws Exception {
            when(autenticacaoApiService.extrairPermissoesDoUsuario()).thenReturn(Set.of());

            mockMvc.perform(get("/api/auth/permissions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(autenticacaoApiService).extrairPermissoesDoUsuario();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint POST /api/auth/check-permission")
    class TestesVerificarPermissao {

        @Test
        @DisplayName("Deve retornar true quando usuário possui a permissão")
        @WithMockUser
        void deveRetornarTrueQuandoUsuarioPossuiPermissao() throws Exception {
            PermissaoDTO permissaoDTO = new PermissaoDTO();
            permissaoDTO.setPermissao("READ_PROCESSOS");

            when(autenticacaoApiService.verificarPermissao("READ_PROCESSOS")).thenReturn(true);

            mockMvc.perform(post("/api/auth/check-permission")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(permissaoDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(autenticacaoApiService).verificarPermissao("READ_PROCESSOS");
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não possui a permissão")
        @WithMockUser
        void deveRetornarFalseQuandoUsuarioNaoPossuiPermissao() throws Exception {
            PermissaoDTO permissaoDTO = new PermissaoDTO();
            permissaoDTO.setPermissao("DELETE_ALL");

            when(autenticacaoApiService.verificarPermissao("DELETE_ALL")).thenReturn(false);

            mockMvc.perform(post("/api/auth/check-permission")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(permissaoDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

            verify(autenticacaoApiService).verificarPermissao("DELETE_ALL");
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            PermissaoDTO permissaoDTO = new PermissaoDTO();
            permissaoDTO.setPermissao("READ_PROCESSOS");

            mockMvc.perform(post("/api/auth/check-permission")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(permissaoDTO)))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).verificarPermissao(anyString());
        }

        @Test
        @DisplayName("Deve retornar 400 quando dados inválidos")
        @WithMockUser
        void deveRetornar400QuandoDadosInvalidos() throws Exception {
            mockMvc.perform(post("/api/auth/check-permission")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(autenticacaoApiService, never()).verificarPermissao(anyString());
        }
    }

    @Nested
    @DisplayName("Testes do endpoint POST /api/auth/validate-token")
    class TestesValidarToken {

        @Test
        @DisplayName("Deve retornar true quando token é válido")
        @WithMockUser
        void deveRetornarTrueQuandoTokenValido() throws Exception {
            TokenValidationDTO tokenDTO = new TokenValidationDTO();
            tokenDTO.setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");

            when(autenticacaoApiService.validarToken(anyString())).thenReturn(true);

            mockMvc.perform(post("/api/auth/validate-token")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(autenticacaoApiService).validarToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar false quando token é inválido")
        @WithMockUser
        void deveRetornarFalseQuandoTokenInvalido() throws Exception {
            TokenValidationDTO tokenDTO = new TokenValidationDTO();
            tokenDTO.setToken("token-invalido");

            when(autenticacaoApiService.validarToken(anyString())).thenReturn(false);

            mockMvc.perform(post("/api/auth/validate-token")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

            verify(autenticacaoApiService).validarToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            TokenValidationDTO tokenDTO = new TokenValidationDTO();
            tokenDTO.setToken("token");

            mockMvc.perform(post("/api/auth/validate-token")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenDTO)))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).validarToken(anyString());
        }
    }

    @Nested
    @DisplayName("Testes do endpoint POST /api/auth/logout")
    class TestesLogout {

        @Test
        @DisplayName("Deve retornar URL de logout sem redirect URI")
        @WithMockUser
        void deveRetornarUrlDeLogoutSemRedirectUri() throws Exception {
            String logoutUrl = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/logout";
            when(autenticacaoApiService.obterUrlLogout()).thenReturn(logoutUrl);

            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.logout_url").value(logoutUrl))
                    .andExpect(jsonPath("$.message").exists());

            verify(autenticacaoApiService).obterUrlLogout();
            verify(autenticacaoApiService, never()).obterUrlLogoutCompleto(anyString());
        }

        @Test
        @DisplayName("Deve retornar URL de logout com redirect URI")
        @WithMockUser
        void deveRetornarUrlDeLogoutComRedirectUri() throws Exception {
            String redirectUri = "http://localhost:3000/login";
            String logoutUrl = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/logout?redirect_uri=" + redirectUri;
            when(autenticacaoApiService.obterUrlLogoutCompleto(redirectUri)).thenReturn(logoutUrl);

            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf())
                            .param("redirectUri", redirectUri))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.logout_url").value(logoutUrl));

            verify(autenticacaoApiService).obterUrlLogoutCompleto(redirectUri);
            verify(autenticacaoApiService, never()).obterUrlLogout();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).obterUrlLogout();
        }

        @Test
        @DisplayName("Deve retornar 500 quando ocorrer erro ao gerar URL")
        @WithMockUser
        void deveRetornar500QuandoOcorrerErroAoGerarUrl() throws Exception {
            when(autenticacaoApiService.obterUrlLogout())
                    .thenThrow(new RuntimeException("Erro ao gerar URL"));

            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").exists());

            verify(autenticacaoApiService).obterUrlLogout();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/roles")
    class TestesRoles {

        @Test
        @DisplayName("Deve retornar roles do usuário autenticado")
        @WithMockUser
        void deveRetornarRolesDoUsuarioAutenticado() throws Exception {
            Set<String> roles = Set.of("ADMIN", "GESTOR", "ANALISTA");
            when(autenticacaoApiService.extrairRolesDoUsuario()).thenReturn(roles);

            mockMvc.perform(get("/api/auth/roles"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            verify(autenticacaoApiService).extrairRolesDoUsuario();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(get("/api/auth/roles"))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).extrairRolesDoUsuario();
        }

        @Test
        @DisplayName("Deve retornar conjunto vazio quando usuário não tem roles")
        @WithMockUser
        void deveRetornarConjuntoVazioQuandoUsuarioNaoTemRoles() throws Exception {
            when(autenticacaoApiService.extrairRolesDoUsuario()).thenReturn(Set.of());

            mockMvc.perform(get("/api/auth/roles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(autenticacaoApiService).extrairRolesDoUsuario();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/profile")
    class TestesPerfil {

        @Test
        @DisplayName("Deve retornar perfil completo do usuário")
        @WithMockUser
        void deveRetornarPerfilCompletoDoUsuario() throws Exception {
            when(autenticacaoApiService.obterInformacoesUsuarioAtual()).thenReturn(usuarioInfoDTO);

            mockMvc.perform(get("/api/auth/profile"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value("123e4567-e89b-12d3-a456-426614174000"))
                    .andExpect(jsonPath("$.username").value("joao.silva"))
                    .andExpect(jsonPath("$.email").value("joao.silva@md.gov.br"));

            verify(autenticacaoApiService).obterInformacoesUsuarioAtual();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(get("/api/auth/profile"))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).obterInformacoesUsuarioAtual();
        }
    }

    @Nested
    @DisplayName("Testes do endpoint GET /api/auth/token-claims")
    class TestesTokenClaims {

        @Test
        @DisplayName("Deve retornar claims do token JWT")
        @WithMockUser
        void deveRetornarClaimsDoTokenJwt() throws Exception {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "123e4567-e89b-12d3-a456-426614174000");
            claims.put("email", "joao.silva@md.gov.br");
            claims.put("preferred_username", "joao.silva");
            claims.put("realm_access", Map.of("roles", Set.of("ADMIN", "GESTOR")));

            when(autenticacaoApiService.extrairClaimsDoToken()).thenReturn(claims);

            mockMvc.perform(get("/api/auth/token-claims"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sub").value("123e4567-e89b-12d3-a456-426614174000"))
                    .andExpect(jsonPath("$.email").value("joao.silva@md.gov.br"))
                    .andExpect(jsonPath("$.preferred_username").value("joao.silva"));

            verify(autenticacaoApiService).extrairClaimsDoToken();
        }

        @Test
        @DisplayName("Deve retornar 401 quando usuário não autenticado")
        void deveRetornar401QuandoUsuarioNaoAutenticado() throws Exception {
            mockMvc.perform(get("/api/auth/token-claims"))
                    .andExpect(status().isUnauthorized());

            verify(autenticacaoApiService, never()).extrairClaimsDoToken();
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando não houver claims")
        @WithMockUser
        void deveRetornarMapaVazioQuandoNaoHouverClaims() throws Exception {
            when(autenticacaoApiService.extrairClaimsDoToken()).thenReturn(new HashMap<>());

            mockMvc.perform(get("/api/auth/token-claims"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());

            verify(autenticacaoApiService).extrairClaimsDoToken();
        }
    }
}