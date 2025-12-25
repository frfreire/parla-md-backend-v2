package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.old.AutenticacaoConfigDTO;
import br.gov.md.parla_md_backend.domain.dto.old.UsuarioInfoDTO;
import br.gov.md.parla_md_backend.exception.AutenticacaoException;
import br.gov.md.parla_md_backend.exception.UsuarioNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável por gerenciar a autenticação e autorização na API do sistema Parla-MD.
 *
 * <p><strong>VERSÃO CORRIGIDA:</strong> Esta versão corrige problemas de compatibilidade
 * com Spring Boot 3.3.4 e adiciona tratamento robusto de erros.</p>
 *
 * <p>Nome alterado para AutenticacaoApiService para evitar conflito com o AutenticacaoService existente.</p>
 *
 * <p>Principais melhorias implementadas:</p>
 * <ul>
 *   <li>Compatibilidade total com Spring Boot 3.3.4</li>
 *   <li>Tratamento robusto de diferentes tipos de tokens</li>
 *   <li>Validação de claims e dados do JWT</li>
 *   <li>Logging detalhado para debug</li>
 *   <li>Fallbacks para diferentes configurações</li>
 * </ul>
 *
 * <p>Este serviço fornece funcionalidades para:</p>
 * <ul>
 *   <li>Obter informações do usuário autenticado</li>
 *   <li>Validar tokens JWT</li>
 *   <li>Verificar permissões e roles</li>
 *   <li>Construir URLs de logout</li>
 *   <li>Extrair claims dos tokens</li>
 *   <li>Fornecer configurações públicas do Keycloak</li>
 * </ul>
 *
 * @author fabricio.freire
 * @version 2.0
 * @since 2024-12-16
 */
@Service
public class AutenticacaoApiService {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoApiService.class);

    private final JwtDecoder jwtDecoder;
    private final RestTemplate restTemplate;

    // Configurações do Keycloak injetadas via application.yml (compatível com existente)
    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:parlamd}")
    private String keycloakRealm;

    @Value("${keycloak.resource:parlamd-frontend}")
    private String keycloakClientId;

    @Value("${app.keycloak.token-lifetime:3600}")
    private Integer tokenLifetime;

    @Value("${app.keycloak.refresh-token-lifetime:7200}")
    private Integer refreshTokenLifetime;

    public AutenticacaoApiService(JwtDecoder jwtDecoder,
                                  RestTemplate restTemplate) {
        this.jwtDecoder = Objects.requireNonNull(jwtDecoder, "JwtDecoder não pode ser nulo");
        this.restTemplate = Objects.requireNonNull(restTemplate, "RestTemplate não pode ser nulo");

        logger.info("AutenticacaoApiService inicializado com sucesso");
    }

    /**
     * Obtém as informações completas do usuário atualmente autenticado.
     *
     * @return UsuarioInfoDTO com as informações do usuário
     * @throws AutenticacaoException se não há usuário autenticado
     * @throws UsuarioNaoEncontradoException se o usuário não é encontrado
     */
    @Cacheable(value = "usuario-info", key = "#root.target.obterSubjectUsuarioAtual()")
    public UsuarioInfoDTO obterInformacoesUsuarioAtual() throws AutenticacaoException {
        try {
            logger.debug("Iniciando obtenção de informações do usuário autenticado");

            Jwt jwt = obterJwtDoContextoSeguro();

            logger.debug("Extraindo informações do usuário do token JWT. Subject: {}", jwt.getSubject());

            UsuarioInfoDTO usuarioInfo = construirUsuarioInfoDoJwt(jwt);

            logger.info("Informações do usuário obtidas com sucesso. Username: {}, Roles: {}",
                    usuarioInfo.getUsername(), usuarioInfo.getRoles());

            return usuarioInfo;

        } catch (Exception e) {
            logger.error("Erro ao obter informações do usuário atual: {}", e.getMessage(), e);

            if (e instanceof AutenticacaoException || e instanceof UsuarioNaoEncontradoException) {
                throw e;
            }

            throw new AutenticacaoException("Falha ao obter informações do usuário", e);
        }
    }

    /**
     * Valida um token JWT específico.
     *
     * @param token o token a ser validado
     * @return true se o token é válido, false caso contrário
     */
    public Boolean validarToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            // Remover prefixo "Bearer " se presente
            String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;

            Jwt jwt = jwtDecoder.decode(tokenLimpo);

            // Verificar se o token não está expirado
            Instant agora = Instant.now();
            if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(agora)) {
                logger.debug("Token expirado. Expirou em: {}", jwt.getExpiresAt());
                return false;
            }

            // Verificar se o token não é válido antes da data atual
            if (jwt.getNotBefore() != null && jwt.getNotBefore().isAfter(agora)) {
                logger.debug("Token ainda não é válido. Válido a partir de: {}", jwt.getNotBefore());
                return false;
            }

            logger.debug("Token validado com sucesso. Subject: {}", jwt.getSubject());
            return true;

        } catch (JwtException e) {
            logger.debug("Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Erro inesperado ao validar token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se o usuário atual possui uma permissão específica.
     *
     * @param permissao a permissão a ser verificada
     * @return true se o usuário possui a permissão
     */
    public Boolean verificarPermissao(String permissao) {
        try {
            UsuarioInfoDTO usuario = obterInformacoesUsuarioAtual();
            boolean possuiPermissao = usuario.possuiPermissao(permissao);

            logger.debug("Verificação de permissão '{}' para usuário '{}': {}",
                    permissao, usuario.getUsername(), possuiPermissao);

            return possuiPermissao;

        } catch (Exception e) {
            logger.debug("Erro ao verificar permissão '{}': {}", permissao, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário atual possui uma role específica.
     *
     * @param role a role a ser verificada
     * @return true se o usuário possui a role
     */
    public Boolean verificarRole(String role) {
        try {
            UsuarioInfoDTO usuario = obterInformacoesUsuarioAtual();
            boolean possuiRole = usuario.possuiRole(role);

            logger.debug("Verificação de role '{}' para usuário '{}': {}",
                    role, usuario.getUsername(), possuiRole);

            return possuiRole;

        } catch (Exception e) {
            logger.debug("Erro ao verificar role '{}': {}", role, e.getMessage());
            return false;
        }
    }

    /**
     * Obtém a URL de logout simples.
     *
     * @return URL de logout
     */
    public String obterUrlLogout() {
        return String.format("%s/realms/%s/protocol/openid-connect/logout?client_id=%s",
                keycloakServerUrl, keycloakRealm, keycloakClientId);
    }

    /**
     * Obtém a URL de logout com redirecionamento.
     *
     * @param redirectUri URI para redirecionamento após logout
     * @return URL completa de logout
     */
    public String obterUrlLogoutCompleto(String redirectUri) {
        StringBuilder url = new StringBuilder(obterUrlLogout());

        if (StringUtils.hasText(redirectUri)) {
            url.append("&post_logout_redirect_uri=").append(redirectUri);
        }

        return url.toString();
    }

    /**
     * Extrai todos os claims do token JWT do usuário atual.
     *
     * @return mapa com os claims do token
     */
    public Map<String, Object> extrairClaimsDoToken() {
        try {
            Jwt jwt = obterJwtDoContextoSeguro();
            return new HashMap<>(jwt.getClaims());

        } catch (Exception e) {
            logger.debug("Erro ao extrair claims do token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Extrai as roles do usuário atual.
     *
     * @return conjunto de roles
     */
    public Set<String> extrairRolesDoUsuario() {
        try {
            UsuarioInfoDTO usuario = obterInformacoesUsuarioAtual();
            return usuario.getRoles() != null ? usuario.getRoles() : new HashSet<>();

        } catch (Exception e) {
            logger.debug("Erro ao extrair roles do usuário: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Extrai as permissões do usuário atual.
     *
     * @return conjunto de permissões
     */
    public Set<String> extrairPermissoesDoUsuario() {
        try {
            UsuarioInfoDTO usuario = obterInformacoesUsuarioAtual();
            return usuario.getPermissoes() != null ? usuario.getPermissoes() : new HashSet<>();

        } catch (Exception e) {
            logger.debug("Erro ao extrair permissões do usuário: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Obtém as configurações públicas do Keycloak para o frontend.
     *
     * @return AutenticacaoConfigDTO com as configurações
     */
    @Cacheable(value = "keycloak-config")
    public AutenticacaoConfigDTO obterConfiguracaoPublica() {
        logger.debug("Construindo configuração pública do Keycloak");

        return AutenticacaoConfigDTO.builder()
                .keycloakUrl(keycloakServerUrl)
                .realm(keycloakRealm)
                .clientId(keycloakClientId)
                .loginUrl(String.format("%s/realms/%s/protocol/openid-connect/auth",
                        keycloakServerUrl, keycloakRealm))
                .logoutUrl(String.format("%s/realms/%s/protocol/openid-connect/logout",
                        keycloakServerUrl, keycloakRealm))
                .tokenUrl(String.format("%s/realms/%s/protocol/openid-connect/token",
                        keycloakServerUrl, keycloakRealm))
                .userinfoUrl(String.format("%s/realms/%s/protocol/openid-connect/userinfo",
                        keycloakServerUrl, keycloakRealm))
                .wellknownUrl(String.format("%s/realms/%s/.well-known/openid-configuration",
                        keycloakServerUrl, keycloakRealm))
                .jwksUrl(String.format("%s/realms/%s/protocol/openid-connect/certs",
                        keycloakServerUrl, keycloakRealm))
                .tokenLifetime(tokenLifetime)
                .refreshTokenLifetime(refreshTokenLifetime)
                .scopes(Arrays.asList("openid", "profile", "email", "roles"))
                .responseTypes(Arrays.asList("code"))
                .pkceRequired(true)
                .build();
    }

    /**
     * Verifica a saúde do sistema de autenticação.
     *
     * @return true se o sistema está saudável
     */
    public Boolean verificarSaudeAutenticacao() {
        try {
            // Verificar se consegue acessar o well-known endpoint do Keycloak
            String wellKnownUrl = String.format("%s/realms/%s/.well-known/openid-configuration",
                    keycloakServerUrl, keycloakRealm);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(wellKnownUrl, Map.class);
            logger.debug("Health check do Keycloak bem-sucedido");
            return true;

        } catch (Exception e) {
            logger.warn("Health check do Keycloak falhou: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém o subject (ID) do usuário atual.
     *
     * @return subject do usuário ou null se não autenticado
     */
    public String obterSubjectUsuarioAtual() {
        try {
            Jwt jwt = obterJwtDoContextoSeguro();
            return jwt.getSubject();
        } catch (Exception e) {
            logger.debug("Erro ao obter subject do usuário: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se há um usuário autenticado no contexto atual.
     *
     * @return true se há usuário autenticado
     */
    public Boolean isUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null &&
                    authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser");
        } catch (Exception e) {
            logger.debug("Erro ao verificar autenticação: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém o JWT do contexto de segurança de forma segura.
     * Compatível com diferentes tipos de authentication tokens.
     *
     * @return JWT do usuário autenticado
     * @throws AutenticacaoException se não há usuário autenticado
     */
    private Jwt obterJwtDoContextoSeguro() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AutenticacaoException("Usuário não autenticado - contexto nulo");
        }

        // Tentar obter JWT de PreAuthenticatedAuthenticationToken (Spring Boot 3+)
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken preAuthToken = (PreAuthenticatedAuthenticationToken) authentication;
            if (preAuthToken.getCredentials() instanceof Jwt) {
                return (Jwt) preAuthToken.getCredentials();
            }
        }

        // Fallback: tentar obter de outros tipos de token (compatibilidade)
        if (authentication.getCredentials() instanceof Jwt) {
            return (Jwt) authentication.getCredentials();
        }

        // Se chegou aqui, não conseguiu obter o JWT
        throw new AutenticacaoException("Token JWT não encontrado no contexto de segurança");
    }

    /**
     * Constrói o UsuarioInfoDTO a partir dos claims do JWT.
     *
     * @param jwt o token JWT
     * @return UsuarioInfoDTO construído
     */
    @SuppressWarnings("unchecked")
    private UsuarioInfoDTO construirUsuarioInfoDoJwt(Jwt jwt) {
        if (jwt == null) {
            throw new AutenticacaoException("JWT não pode ser nulo");
        }

        Map<String, Object> claims = jwt.getClaims();

        // Extrair informações básicas com validação
        String username = obterClaimSeguro(jwt, "preferred_username");
        String email = obterClaimSeguro(jwt, "email");
        String primeiroNome = obterClaimSeguro(jwt, "given_name");
        String ultimoNome = obterClaimSeguro(jwt, "family_name");
        String nomeCompleto = obterClaimSeguro(jwt, "name");
        Boolean emailVerificado = jwt.getClaimAsBoolean("email_verified");

        // Extrair roles do realm_access
        Set<String> roles = extrairRolesDoJwt(jwt);

        // Construir permissões baseadas nas roles (simplificado)
        Set<String> permissoes = construirPermissoesDasRoles(roles);

        // Extrair timestamps com validação
        LocalDateTime criadoEm = null;
        if (jwt.getIssuedAt() != null) {
            try {
                criadoEm = LocalDateTime.ofInstant(jwt.getIssuedAt(), ZoneId.systemDefault());
            } catch (Exception e) {
                logger.warn("Erro ao converter timestamp de criação: {}", e.getMessage());
            }
        }

        return UsuarioInfoDTO.builder()
                .id(jwt.getSubject())
                .username(username)
                .email(email)
                .primeiroNome(primeiroNome)
                .ultimoNome(ultimoNome)
                .nomeCompleto(nomeCompleto != null ? nomeCompleto : construirNomeCompleto(primeiroNome, ultimoNome))
                .roles(roles)
                .permissoes(permissoes)
                .ativo(true) // Assumir ativo se o token é válido
                .emailVerificado(emailVerificado)
                .criadoEm(criadoEm)
                .metadados(claims)
                .build();
    }

    /**
     * Obtém um claim de forma segura, tratando possíveis exceções.
     *
     * @param jwt o token JWT
     * @param claimName nome do claim
     * @return valor do claim ou null se não encontrado
     */
    private String obterClaimSeguro(Jwt jwt, String claimName) {
        try {
            return jwt.getClaimAsString(claimName);
        } catch (Exception e) {
            logger.debug("Erro ao obter claim '{}': {}", claimName, e.getMessage());
            return null;
        }
    }

    /**
     * Extrai roles do JWT do Keycloak com tratamento robusto de erros.
     */
    @SuppressWarnings("unchecked")
    private Set<String> extrairRolesDoJwt(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        try {
            // Tentar extrair do realm_access
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof Collection) {
                    Collection<String> rolesList = (Collection<String>) rolesObj;
                    roles.addAll(rolesList);
                }
            }

            // Filtrar apenas roles relevantes para o sistema
            Set<String> rolesValidas = roles.stream()
                    .filter(Objects::nonNull)
                    .filter(role -> !role.trim().isEmpty())
                    .filter(role -> Arrays.asList("ADMIN", "ANALISTA", "GESTOR", "EXTERNO", "VIEWER").contains(role.toUpperCase()))
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            logger.debug("Roles extraídas e validadas: {}", rolesValidas);
            return rolesValidas;

        } catch (Exception e) {
            logger.warn("Erro ao extrair roles do JWT: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Constrói permissões básicas baseadas nas roles.
     */
    private Set<String> construirPermissoesDasRoles(Set<String> roles) {
        Set<String> permissoes = new HashSet<>();

        if (roles.contains("ADMIN")) {
            permissoes.addAll(Arrays.asList("READ_ALL", "WRITE_ALL", "DELETE_ALL", "ADMIN_ALL"));
        }
        if (roles.contains("GESTOR")) {
            permissoes.addAll(Arrays.asList("READ_PROJETOS", "WRITE_PROJETOS", "READ_TRAMITACAO", "WRITE_TRAMITACAO"));
        }
        if (roles.contains("ANALISTA")) {
            permissoes.addAll(Arrays.asList("READ_PROJETOS", "WRITE_PROJETOS", "READ_TRAMITACAO"));
        }
        if (roles.contains("VIEWER")) {
            permissoes.addAll(Arrays.asList("READ_PROJETOS", "READ_TRAMITACAO"));
        }

        return permissoes;
    }

    /**
     * Constrói nome completo a partir de primeiro e último nome com validação.
     *
     * @param primeiroNome primeiro nome
     * @param ultimoNome último nome
     * @return nome completo construído ou null se ambos forem nulos/vazios
     */
    private String construirNomeCompleto(String primeiroNome, String ultimoNome) {
        // Validar e limpar inputs
        String primeiro = (primeiroNome != null) ? primeiroNome.trim() : "";
        String ultimo = (ultimoNome != null) ? ultimoNome.trim() : "";

        if (primeiro.isEmpty() && ultimo.isEmpty()) {
            return null;
        }

        StringBuilder nomeBuilder = new StringBuilder();
        if (!primeiro.isEmpty()) {
            nomeBuilder.append(primeiro);
        }
        if (!ultimo.isEmpty()) {
            if (nomeBuilder.length() > 0) {
                nomeBuilder.append(" ");
            }
            nomeBuilder.append(ultimo);
        }

        String nomeCompleto = nomeBuilder.toString().trim();
        return nomeCompleto.isEmpty() ? null : nomeCompleto;
    }
}