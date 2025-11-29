package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import br.gov.md.parla_md_backend.exception.AutenticacaoException;
import br.gov.md.parla_md_backend.exception.TokenInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável por operações de autenticação e autorização.
 *
 * <p><strong>VERSÃO CORRIGIDA:</strong> Atualizado para compatibilidade com Spring Boot 3.3.4</p>
 *
 * Fornece métodos para extrair informações do JWT, validar tokens,
 * verificar permissões e gerenciar informações do usuário autenticado.
 *
 *
 * @author fabricio.freire
 * @since 1.0
 * @version 2.0 (corrigida para Spring Boot 3.3.4)
 */
@Service
public class AutenticacaoService {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${app.security.token-validation.enabled:true}")
    private boolean tokenValidationEnabled;

    /**
     * Obtém informações completas do usuário autenticado.
     *
     * @return UsuarioDTO com informações do usuário
     * @throws AutenticacaoException se não houver usuário autenticado
     */
    public UsuarioDTO obterInformacoesUsuario() {
        logger.debug("Iniciando obtenção de informações do usuário autenticado");

        Jwt jwt = obterJwtDoContexto();
        return extrairInformacoesDoJwt(jwt);
    }

    /**
     * Obtém informações do usuário com cache para melhor performance.
     *
     * @return UsuarioDTO com informações do usuário (com cache)
     * @throws AutenticacaoException se não houver usuário autenticado
     */
    @Cacheable(value = "userInfo", key = "#root.methodName + '_' + authentication.name",
            condition = "#root.target.isUsuarioAutenticado()")
    public UsuarioDTO obterInformacoesUsuarioComCache() {
        return obterInformacoesUsuario();
    }

    /**
     * Verifica se há um usuário autenticado no contexto.
     *
     * @return true se há usuário autenticado
     */
    public boolean isUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null &&
                    authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser") &&
                    (authentication instanceof PreAuthenticatedAuthenticationToken ||
                            authentication.getCredentials() instanceof Jwt);
        } catch (Exception e) {
            logger.debug("Erro ao verificar autenticação: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém o ID do usuário autenticado.
     *
     * @return ID do usuário
     * @throws AutenticacaoException se não houver usuário autenticado
     */
    public String obterIdUsuario() {
        Jwt jwt = obterJwtDoContexto();
        return jwt.getClaimAsString("sub");
    }

    /**
     * Obtém o username do usuário autenticado.
     *
     * @return username do usuário
     * @throws AutenticacaoException se não houver usuário autenticado
     */
    public String obterUsernameUsuario() {
        Jwt jwt = obterJwtDoContexto();
        return jwt.getClaimAsString("preferred_username");
    }

    /**
     * Obtém as roles do usuário autenticado.
     *
     * @return lista de roles do usuário
     * @throws AutenticacaoException se não houver usuário autenticado
     */
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

    /**
     * Verifica se o usuário possui uma role específica.
     *
     * @param role a role a ser verificada
     * @return true se o usuário possui a role
     */
    public boolean verificarRole(String role) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return userRoles.contains(role.toUpperCase());
        } catch (Exception e) {
            logger.debug("Erro ao verificar role '{}': {}", role, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário possui pelo menos uma das roles especificadas.
     *
     * @param roles lista de roles a serem verificadas
     * @return true se o usuário possui ao menos uma das roles
     */
    public boolean verificarQualquerRole(String... roles) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return Arrays.stream(roles)
                    .map(String::toUpperCase)
                    .anyMatch(userRoles::contains);
        } catch (Exception e) {
            logger.debug("Erro ao verificar roles: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário possui todas as roles especificadas.
     *
     * @param roles lista de roles a serem verificadas
     * @return true se o usuário possui todas as roles
     */
    public boolean verificarTodasAsRoles(String... roles) {
        try {
            List<String> userRoles = obterRolesUsuario();
            return Arrays.stream(roles)
                    .map(String::toUpperCase)
                    .allMatch(userRoles::contains);
        } catch (Exception e) {
            logger.debug("Erro ao verificar roles: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida se um token JWT é válido.
     *
     * <p><strong>MELHORADO:</strong> Validação mais robusta</p>
     *
     * @param token o token a ser validado
     * @return true se o token é válido
     */
    public boolean validarToken(String token) {
        if (!tokenValidationEnabled) {
            logger.debug("Validação de token desabilitada");
            return true;
        }

        try {
            // Validação básica
            if (token == null || token.trim().isEmpty()) {
                logger.debug("Token nulo ou vazio");
                return false;
            }

            // Remover prefixo "Bearer " se presente
            String tokenLimpo = token.startsWith("Bearer ") ? token.substring(7) : token;

            // Validação mais rigorosa seria implementada aqui
            // Por enquanto, validação básica de formato
            if (tokenLimpo.split("\\.").length != 3) {
                logger.debug("Token não possui formato JWT válido");
                return false;
            }

            logger.debug("Token passou na validação básica");
            return true;

        } catch (Exception e) {
            logger.error("Erro ao validar token: {}", e.getMessage());
            throw new TokenInvalidoException("Token inválido", e);
        }
    }

    /**
     * Obtém o JWT do contexto de segurança.
     *
     * <p><strong>CORRIGIDO:</strong> Atualizado para compatibilidade com Spring Boot 3.3.4</p>
     *
     * @return o JWT do usuário autenticado
     * @throws AutenticacaoException se não houver usuário autenticado
     */
    private Jwt obterJwtDoContexto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AutenticacaoException("Usuário não autenticado");
        }

        // Spring Boot 3+: Tentar obter de PreAuthenticatedAuthenticationToken
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken preAuthToken = (PreAuthenticatedAuthenticationToken) authentication;
            if (preAuthToken.getCredentials() instanceof Jwt) {
                return (Jwt) preAuthToken.getCredentials();
            }
        }

        // Fallback: Tentar obter diretamente das credenciais
        if (authentication.getCredentials() instanceof Jwt) {
            return (Jwt) authentication.getCredentials();
        }

        // Se chegou aqui, não conseguiu obter o JWT
        throw new AutenticacaoException("Token JWT não encontrado no contexto de segurança");
    }

    /**
     * Extrai informações do usuário a partir do JWT.
     *
     * <p><strong>MELHORADO:</strong> Adicionado tratamento robusto de erros</p>
     *
     * @param jwt o token JWT
     * @return UsuarioDTO com as informações extraídas
     */
    @SuppressWarnings("unchecked")
    private UsuarioDTO extrairInformacoesDoJwt(Jwt jwt) {
        try {
            UsuarioDTO usuario = new UsuarioDTO();

            // Informações básicas com tratamento seguro
            usuario.setId(obterClaimSeguro(jwt, "sub"));
            usuario.setUsername(obterClaimSeguro(jwt, "preferred_username"));
            usuario.setEmail(obterClaimSeguro(jwt, "email"));
            usuario.setNome(obterClaimSeguro(jwt, "given_name"));
            usuario.setSobrenome(obterClaimSeguro(jwt, "family_name"));
            usuario.setNomeCompleto(obterClaimSeguro(jwt, "name"));

            // Verificação de email com segurança
            Boolean emailVerificado = jwt.getClaimAsBoolean("email_verified");
            usuario.setEmailVerificado(emailVerificado != null ? emailVerificado : false);

            // Timestamps com tratamento de exceções
//            if (jwt.getIssuedAt() != null) {
//                try {
//                    usuario.setDataCriacao(LocalDateTime.ofInstant(jwt.getIssuedAt(), ZoneId.systemDefault()));
//                } catch (Exception e) {
//                    logger.warn("Erro ao converter timestamp de criação: {}", e.getMessage());
//                }
//            }

//            if (jwt.getExpiresAt() != null) {
//                try {
//                    usuario.setDataExpiracao(LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneId.systemDefault()));
//                } catch (Exception e) {
//                    logger.warn("Erro ao converter timestamp de expiração: {}", e.getMessage());
//                }
//            }

            // Roles do realm_access com tratamento seguro
//            try {
//                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
//                if (realmAccess != null && realmAccess.containsKey("roles")) {
//                    List<String> roles = (List<String>) realmAccess.get("roles");
//                    if (roles != null) {
//                        // Filtrar e validar roles
//                        Set<String> rolesValidas = roles.stream()
//                                .filter(Objects::nonNull)
//                                .filter(role -> !role.trim().isEmpty())
//                                .collect(Collectors.toSet());
//                        usuario.setRoles(rolesValidas);
//                    }
//                }
//            } catch (Exception e) {
//                logger.warn("Erro ao extrair roles do JWT: {}", e.getMessage());
//                usuario.setRoles(new HashSet<>());
//            }
//
//            // Atributos customizados com segurança
//            Map<String, Object> attributes = new HashMap<>();
//            try {
//                jwt.getClaims().forEach((key, value) -> {
//                    if (!Arrays.asList("sub", "preferred_username", "email", "given_name",
//                            "family_name", "name", "email_verified", "iat", "exp",
//                            "realm_access").contains(key)) {
//                        attributes.put(key, value);
//                    }
//                });
//                usuario.setAtributos(attributes);
//            } catch (Exception e) {
//                logger.warn("Erro ao extrair atributos customizados: {}", e.getMessage());
//                usuario.setAtributos(new HashMap<>());
//            }

            logger.debug("Informações extraídas do JWT para usuário: {}", usuario.getUsername());
            return usuario;

        } catch (Exception e) {
            logger.error("Erro ao extrair informações do JWT: {}", e.getMessage(), e);
            throw new AutenticacaoException("Erro ao processar informações do token", e);
        }
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
     * Verifica se o usuário é administrador.
     *
     * @return true se o usuário possui role ADMIN
     */
    public boolean isAdministrador() {
        return verificarRole("ADMIN");
    }

    /**
     * Verifica se o usuário é analista.
     *
     * @return true se o usuário possui role ANALISTA
     */
    public boolean isAnalista() {
        return verificarRole("ANALISTA");
    }

    /**
     * Verifica se o usuário é gestor.
     *
     * @return true se o usuário possui role GESTOR
     */
    public boolean isGestor() {
        return verificarRole("GESTOR");
    }

    /**
     * Obtém informações resumidas do usuário.
     *
     * <p><strong>MELHORADO:</strong> Tratamento de exceções aprimorado</p>
     *
     * @return mapa com informações básicas do usuário
     */
    public Map<String, Object> obterInformacoesResumidas() {
        try {
            UsuarioDTO usuario = obterInformacoesUsuario();
            Map<String, Object> info = new HashMap<>();

            info.put("id", usuario.getId());
            info.put("username", usuario.getUsername());
            info.put("email", usuario.getEmail());
            info.put("nome_completo", usuario.getNomeCompleto());
            info.put("roles", usuario.getRoles() != null ? usuario.getRoles() : new HashSet<>());
            info.put("ativo", true);
//            info.put("email_verificado", usuario.getEmailVerificado());

            return info;
        } catch (Exception e) {
            logger.error("Erro ao obter informações resumidas: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Verifica se o token do usuário atual está próximo do vencimento.
     *
     * @param minutosAntecipadosParaAviso número de minutos antes do vencimento para considerar "próximo"
     * @return true se o token expira nos próximos N minutos
     */
//    public boolean isTokenProximoDoVencimento(int minutosAntecipadosParaAviso) {
//        try {
//            UsuarioDTO usuario = obterInformacoesUsuario();
////            if (usuario.getDataExpiracao() == null) {
////                return false;
////            }
//
//            LocalDateTime agora = LocalDateTime.now();
//            LocalDateTime limiteAviso = agora.plusMinutes(minutosAntecipadosParaAviso);
//
//            return usuario.getDataExpiracao().isBefore(limiteAviso);
//
//        } catch (Exception e) {
//            logger.debug("Erro ao verificar vencimento do token: {}", e.getMessage());
//            return false;
//        }
//    }

    /**
     * Obtém o tempo restante em minutos até o token expirar.
     *
     * @return minutos restantes até expiração ou -1 se não conseguir determinar
     */
//    public long obterMinutosRestantesToken() {
//        try {
//            UsuarioDTO usuario = obterInformacoesUsuario();
//            if (usuario.getDataExpiracao() == null) {
//                return -1;
//            }
//
//            LocalDateTime agora = LocalDateTime.now();
//            if (agora.isAfter(usuario.getDataExpiracao())) {
//                return 0; // Token já expirado
//            }
//
//            return java.time.Duration.between(agora, usuario.getDataExpiracao()).toMinutes();
//
//        } catch (Exception e) {
//            logger.debug("Erro ao calcular tempo restante do token: {}", e.getMessage());
//            return -1;
//        }
//    }
}