package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import br.gov.md.parla_md_backend.exception.AutenticacaoException;
import br.gov.md.parla_md_backend.exception.TokenInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutenticacaoService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${app.security.token-validation.enabled:true}")
    private boolean tokenValidationEnabled;

    public UsuarioDTO obterInformacoesUsuario() {
        log.debug("Obtendo informações do usuário autenticado");

        Jwt jwt = obterJwtDoContexto();
        return extrairInformacoesDoJwt(jwt);
    }

    @Cacheable(value = "userInfo", key = "#root.methodName + '_' + authentication.name",
            condition = "#root.target.isUsuarioAutenticado()")
    public UsuarioDTO obterInformacoesUsuarioComCache() {
        return obterInformacoesUsuario();
    }

    public boolean isUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null &&
                    authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser") &&
                    (authentication instanceof PreAuthenticatedAuthenticationToken ||
                            authentication.getCredentials() instanceof Jwt);
        } catch (Exception e) {
            log.debug("Erro ao verificar autenticação: {}", e.getMessage());
            return false;
        }
    }

    public String obterIdUsuario() {
        Jwt jwt = obterJwtDoContexto();
        return jwt.getClaimAsString("sub");
    }

    public String obterUsernameUsuario() {
        Jwt jwt = obterJwtDoContexto();
        return jwt.getClaimAsString("preferred_username");
    }

    public List<String> obterRolesUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AutenticacaoException("Usuário não autenticado");
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .collect(Collectors.toList());
    }

    public boolean verificarRole(String role) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return userRoles.contains(role.toUpperCase());
        } catch (Exception e) {
            log.debug("Erro ao verificar role '{}': {}", role, e.getMessage());
            return false;
        }
    }

    public boolean verificarQualquerRole(String... roles) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return Arrays.stream(roles)
                    .map(String::toUpperCase)
                    .anyMatch(userRoles::contains);
        } catch (Exception e) {
            log.debug("Erro ao verificar roles: {}", e.getMessage());
            return false;
        }
    }

    public boolean verificarTodasAsRoles(String... roles) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return Arrays.stream(roles)
                    .map(String::toUpperCase)
                    .allMatch(userRoles::contains);
        } catch (Exception e) {
            log.debug("Erro ao verificar roles: {}", e.getMessage());
            return false;
        }
    }

    public boolean validarToken(String token) {
        if (!tokenValidationEnabled) {
            log.debug("Validação de token desabilitada");
            return true;
        }

        try {
            if (token == null || token.trim().isEmpty()) {
                log.debug("Token nulo ou vazio");
                return false;
            }

            String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (tokenLimpo.split("\\.").length != 3) {
                log.debug("Token não possui formato JWT válido");
                return false;
            }

            log.debug("Token passou na validação básica");
            return true;

        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            throw new TokenInvalidoException("Token inválido", e);
        }
    }

    private Jwt obterJwtDoContexto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AutenticacaoException("Usuário não autenticado");
        }

        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken preAuthToken = (PreAuthenticatedAuthenticationToken) authentication;
            if (preAuthToken.getCredentials() instanceof Jwt) {
                return (Jwt) preAuthToken.getCredentials();
            }
        }

        if (authentication.getCredentials() instanceof Jwt) {
            return (Jwt) authentication.getCredentials();
        }

        throw new AutenticacaoException("Token JWT não encontrado no contexto de segurança");
    }

    @SuppressWarnings("unchecked")
    private UsuarioDTO extrairInformacoesDoJwt(Jwt jwt) {
        try {
            String id = obterClaimSeguro(jwt, "sub");
            String username = obterClaimSeguro(jwt, "preferred_username");
            String email = obterClaimSeguro(jwt, "email");
            String primeiroNome = obterClaimSeguro(jwt, "given_name");
            String ultimoNome = obterClaimSeguro(jwt, "family_name");

            String nomeCompleto = construirNomeCompleto(primeiroNome, ultimoNome);

            List<String> roles = extrairRolesDoJwt(jwt);

            return new UsuarioDTO(
                    id,
                    nomeCompleto,
                    email,
                    null,
                    null,
                    null,
                    null,
                    null,
                    roles,
                    true,
                    null,
                    null,
                    null
            );

        } catch (Exception e) {
            log.error("Erro ao extrair informações do JWT: {}", e.getMessage(), e);
            throw new AutenticacaoException("Erro ao processar informações do token", e);
        }
    }

    private String construirNomeCompleto(String primeiroNome, String ultimoNome) {
        if (primeiroNome != null && ultimoNome != null) {
            return primeiroNome + " " + ultimoNome;
        } else if (primeiroNome != null) {
            return primeiroNome;
        } else if (ultimoNome != null) {
            return ultimoNome;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extrairRolesDoJwt(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    return roles.stream()
                            .filter(Objects::nonNull)
                            .filter(role -> !role.trim().isEmpty())
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao extrair roles do JWT: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    private String obterClaimSeguro(Jwt jwt, String claimName) {
        try {
            return jwt.getClaimAsString(claimName);
        } catch (Exception e) {
            log.debug("Erro ao obter claim '{}': {}", claimName, e.getMessage());
            return null;
        }
    }

    public boolean isAdministrador() {
        return verificarRole("ADMIN");
    }

    public boolean isAnalista() {
        return verificarRole("ANALISTA");
    }

    public boolean isGestor() {
        return verificarRole("GESTOR");
    }

    public Map<String, Object> obterInformacoesResumidas() {
        try {
            UsuarioDTO usuario = obterInformacoesUsuario();
            Map<String, Object> info = new HashMap<>();

            info.put("id", usuario.id());
            info.put("username", usuario.nome());
            info.put("email", usuario.email());
            info.put("nome_completo", usuario.nome());
            info.put("roles", usuario.roles() != null ? usuario.roles() : new ArrayList<>());
            info.put("ativo", usuario.ativo());

            return info;
        } catch (Exception e) {
            log.error("Erro ao obter informações resumidas: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}