package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import br.gov.md.parla_md_backend.exception.AutenticacaoException;
import br.gov.md.parla_md_backend.exception.TokenInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AutenticacaoService")
class AutenticacaoServiceTest {

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        // Configurar propriedades
        ReflectionTestUtils.setField(autenticacaoService, "keycloakAuthServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(autenticacaoService, "keycloakRealm", "parla-md-realm");
        ReflectionTestUtils.setField(autenticacaoService, "tokenValidationEnabled", true);
    }

    private Jwt criarJwtMock() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "123e4567-e89b-12d3-a456-426614174000");
        claims.put("preferred_username", "joao.silva");
        claims.put("email", "joao.silva@md.gov.br");
        claims.put("given_name", "João");
        claims.put("family_name", "Silva");

        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("ADMIN", "GESTOR", "ANALISTA"));
        claims.put("realm_access", realmAccess);

        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
    }

    @Nested
    @DisplayName("Testes de obterInformacoesUsuario")
    class TestesObterInformacoesUsuario {

        @Test
        @DisplayName("Deve obter informações completas do usuário autenticado")
        void deveObterInformacoesCompletasDoUsuarioAutenticado() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            UsuarioDTO resultado = autenticacaoService.obterInformacoesUsuario();

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
            assertThat(resultado.nome()).isEqualTo("João Silva");
            assertThat(resultado.email()).isEqualTo("joao.silva@md.gov.br");
            assertThat(resultado.roles()).containsExactlyInAnyOrder("ADMIN", "GESTOR", "ANALISTA");
            assertThat(resultado.ativo()).isTrue();

            verify(securityContext).getAuthentication();
        }

        @Test
        @DisplayName("Deve construir nome completo quando tiver primeiro e último nome")
        void deveConstruirNomeCompletoQuandoTiverPrimeiroEUltimoNome() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            UsuarioDTO resultado = autenticacaoService.obterInformacoesUsuario();

            assertThat(resultado.nome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve usar apenas primeiro nome quando não tiver último nome")
        void deveUsarApenasPrimeiroNomeQuandoNaoTiverUltimoNome() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "123");
            claims.put("given_name", "João");
            claims.put("email", "joao@test.com");

            Jwt jwtSemUltimoNome = new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    claims
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwtSemUltimoNome);

            UsuarioDTO resultado = autenticacaoService.obterInformacoesUsuario();

            assertThat(resultado.nome()).isEqualTo("João");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não autenticado")
        void deveLancarExcecaoQuandoUsuarioNaoAutenticado() {
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThatThrownBy(() -> autenticacaoService.obterInformacoesUsuario())
                    .isInstanceOf(AutenticacaoException.class)
                    .hasMessage("Usuário não autenticado");

            verify(securityContext).getAuthentication();
        }

        @Test
        @DisplayName("Deve lançar exceção quando JWT não encontrado no contexto")
        void deveLancarExcecaoQuandoJwtNaoEncontrado() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(null);

            assertThatThrownBy(() -> autenticacaoService.obterInformacoesUsuario())
                    .isInstanceOf(AutenticacaoException.class)
                    .hasMessage("Token JWT não encontrado no contexto de segurança");
        }

        @Test
        @DisplayName("Deve retornar lista vazia de roles quando realm_access não existe")
        void deveRetornarListaVaziaDeRolesQuandoRealmAccessNaoExiste() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "123");
            claims.put("email", "test@test.com");

            Jwt jwtSemRoles = new Jwt(
                    "token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    claims
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwtSemRoles);

            UsuarioDTO resultado = autenticacaoService.obterInformacoesUsuario();

            assertThat(resultado.roles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de isUsuarioAutenticado")
    class TestesIsUsuarioAutenticado {

        @Test
        @DisplayName("Deve retornar true quando usuário autenticado com JWT")
        void deveRetornarTrueQuandoUsuarioAutenticadoComJwt() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("joao.silva");
            when(authentication.getCredentials()).thenReturn(jwt);

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true quando usuário autenticado com PreAuthenticatedAuthenticationToken")
        void deveRetornarTrueQuandoUsuarioAutenticadoComPreAuth() {
            jwt = criarJwtMock();
            PreAuthenticatedAuthenticationToken preAuthToken =
                    new PreAuthenticatedAuthenticationToken("principal", jwt);
            preAuthToken.setAuthenticated(true);

            when(securityContext.getAuthentication()).thenReturn(preAuthToken);

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando authentication é null")
        void deveRetornarFalseQuandoAuthenticationNull() {
            when(securityContext.getAuthentication()).thenReturn(null);

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não autenticado")
        void deveRetornarFalseQuandoUsuarioNaoAutenticado() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário é anonymousUser")
        void deveRetornarFalseQuandoUsuarioAnonymous() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("anonymousUser");

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando ocorrer exceção")
        void deveRetornarFalseQuandoOcorrerExcecao() {
            when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Erro"));

            boolean resultado = autenticacaoService.isUsuarioAutenticado();

            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de obterIdUsuario")
    class TestesObterIdUsuario {

        @Test
        @DisplayName("Deve obter ID do usuário do JWT")
        void deveObterIdDoUsuarioDoJwt() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            String resultado = autenticacaoService.obterIdUsuario();

            assertThat(resultado).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há autenticação")
        void deveLancarExcecaoQuandoNaoHaAutenticacao() {
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThatThrownBy(() -> autenticacaoService.obterIdUsuario())
                    .isInstanceOf(AutenticacaoException.class);
        }
    }

    @Nested
    @DisplayName("Testes de obterUsernameUsuario")
    class TestesObterUsernameUsuario {

        @Test
        @DisplayName("Deve obter username do usuário do JWT")
        void deveObterUsernameDoUsuarioDoJwt() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            String resultado = autenticacaoService.obterUsernameUsuario();

            assertThat(resultado).isEqualTo("joao.silva");
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há autenticação")
        void deveLancarExcecaoQuandoNaoHaAutenticacao() {
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThatThrownBy(() -> autenticacaoService.obterUsernameUsuario())
                    .isInstanceOf(AutenticacaoException.class);
        }
    }

    @Nested
    @DisplayName("Testes de obterRolesUsuario")
    class TestesObterRolesUsuario {

        @Test
        @DisplayName("Deve obter roles do usuário sem prefixo ROLE_")
        void deveObterRolesDoUsuarioSemPrefixo() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_GESTOR"),
                    new SimpleGrantedAuthority("ROLE_ANALISTA")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            List<String> resultado = autenticacaoService.obterRolesUsuario();

            assertThat(resultado).containsExactlyInAnyOrder("ADMIN", "GESTOR", "ANALISTA");
        }

        @Test
        @DisplayName("Deve lançar exceção quando authentication é null")
        void deveLancarExcecaoQuandoAuthenticationNull() {
            when(securityContext.getAuthentication()).thenReturn(null);

            assertThatThrownBy(() -> autenticacaoService.obterRolesUsuario())
                    .isInstanceOf(AutenticacaoException.class)
                    .hasMessage("Usuário não autenticado");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há authorities")
        void deveRetornarListaVaziaQuandoNaoHaAuthorities() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

            List<String> resultado = autenticacaoService.obterRolesUsuario();

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de verificarRole")
    class TestesVerificarRole {

        @Test
        @DisplayName("Deve retornar true quando usuário possui a role")
        void deveRetornarTrueQuandoUsuarioPossuiRole() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_GESTOR")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarRole("ADMIN");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não possui a role")
        void deveRetornarFalseQuandoUsuarioNaoPossuiRole() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ANALISTA")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarRole("ADMIN");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve ser case-insensitive ao verificar role")
        void deveSerCaseInsensitiveAoVerificarRole() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarRole("admin");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando ocorrer exceção")
        void deveRetornarFalseQuandoOcorrerExcecao() {
            when(securityContext.getAuthentication()).thenThrow(new RuntimeException());

            boolean resultado = autenticacaoService.verificarRole("ADMIN");

            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de verificarQualquerRole")
    class TestesVerificarQualquerRole {

        @Test
        @DisplayName("Deve retornar true quando usuário possui pelo menos uma das roles")
        void deveRetornarTrueQuandoUsuarioPossuiPeloMenosUmaRole() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ANALISTA")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarQualquerRole("ADMIN", "GESTOR", "ANALISTA");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não possui nenhuma das roles")
        void deveRetornarFalseQuandoUsuarioNaoPossuiNenhumaRole() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_VIEWER")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarQualquerRole("ADMIN", "GESTOR");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando ocorrer exceção")
        void deveRetornarFalseQuandoOcorrerExcecao() {
            when(securityContext.getAuthentication()).thenThrow(new RuntimeException());

            boolean resultado = autenticacaoService.verificarQualquerRole("ADMIN");

            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de verificarTodasAsRoles")
    class TestesVerificarTodasAsRoles {

        @Test
        @DisplayName("Deve retornar true quando usuário possui todas as roles")
        void deveRetornarTrueQuandoUsuarioPossuiTodasRoles() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_GESTOR"),
                    new SimpleGrantedAuthority("ROLE_ANALISTA")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarTodasAsRoles("ADMIN", "GESTOR");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não possui todas as roles")
        void deveRetornarFalseQuandoUsuarioNaoPossuiTodasRoles() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.verificarTodasAsRoles("ADMIN", "GESTOR");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando ocorrer exceção")
        void deveRetornarFalseQuandoOcorrerExcecao() {
            when(securityContext.getAuthentication()).thenThrow(new RuntimeException());

            boolean resultado = autenticacaoService.verificarTodasAsRoles("ADMIN");

            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de validarToken")
    class TestesValidarToken {

        @Test
        @DisplayName("Deve retornar true quando token é válido")
        void deveRetornarTrueQuandoTokenValido() {
            String tokenValido = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";

            boolean resultado = autenticacaoService.validarToken(tokenValido);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true quando token tem prefixo Bearer")
        void deveRetornarTrueQuandoTokenTemPrefixoBearer() {
            String tokenComBearer = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";

            boolean resultado = autenticacaoService.validarToken(tokenComBearer);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando token é null")
        void deveRetornarFalseQuandoTokenNull() {
            boolean resultado = autenticacaoService.validarToken(null);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando token é vazio")
        void deveRetornarFalseQuandoTokenVazio() {
            boolean resultado = autenticacaoService.validarToken("");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando token não tem formato JWT")
        void deveRetornarFalseQuandoTokenNaoTemFormatoJwt() {
            boolean resultado = autenticacaoService.validarToken("token-invalido");

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true quando validação está desabilitada")
        void deveRetornarTrueQuandoValidacaoDesabilitada() {
            ReflectionTestUtils.setField(autenticacaoService, "tokenValidationEnabled", false);

            boolean resultado = autenticacaoService.validarToken("qualquer-coisa");

            assertThat(resultado).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de métodos auxiliares de role")
    class TestesMetodosAuxiliaresRole {

        @Test
        @DisplayName("isAdministrador deve retornar true quando usuário é ADMIN")
        void isAdministradorDeveRetornarTrueQuandoUsuarioAdmin() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.isAdministrador();

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("isAnalista deve retornar true quando usuário é ANALISTA")
        void isAnalistaDeveRetornarTrueQuandoUsuarioAnalista() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ANALISTA")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.isAnalista();

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("isGestor deve retornar true quando usuário é GESTOR")
        void isGestorDeveRetornarTrueQuandoUsuarioGestor() {
            List<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_GESTOR")
            );

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getAuthorities()).thenReturn((Collection) authorities);

            boolean resultado = autenticacaoService.isGestor();

            assertThat(resultado).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de obterInformacoesResumidas")
    class TestesObterInformacoesResumidas {

        @Test
        @DisplayName("Deve obter informações resumidas do usuário")
        void deveObterInformacoesResumidasDoUsuario() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            Map<String, Object> resultado = autenticacaoService.obterInformacoesResumidas();

            assertThat(resultado).isNotNull();
            assertThat(resultado).containsKeys("id", "username", "email", "nome_completo", "roles", "ativo");
            assertThat(resultado.get("id")).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
            assertThat(resultado.get("email")).isEqualTo("joao.silva@md.gov.br");
            assertThat(resultado.get("ativo")).isEqualTo(true);
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando ocorrer erro")
        void deveRetornarMapaVazioQuandoOcorrerErro() {
            when(securityContext.getAuthentication()).thenReturn(null);

            Map<String, Object> resultado = autenticacaoService.obterInformacoesResumidas();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve incluir roles no resultado")
        void deveIncluirRolesNoResultado() {
            jwt = criarJwtMock();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getCredentials()).thenReturn(jwt);

            Map<String, Object> resultado = autenticacaoService.obterInformacoesResumidas();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) resultado.get("roles");
            assertThat(roles).containsExactlyInAnyOrder("ADMIN", "GESTOR", "ANALISTA");
        }
    }
}