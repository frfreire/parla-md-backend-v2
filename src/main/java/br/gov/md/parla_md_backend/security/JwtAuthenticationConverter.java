package br.gov.md.parla_md_backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversor customizado para extrair roles e authorities dos tokens JWT do Keycloak.
 *
 * <p>Este componente é responsável por converter tokens JWT emitidos pelo Keycloak
 * em objetos de autenticação do Spring Security, extraindo as roles do claim
 * 'realm_access.roles' e convertendo-as em authorities com o prefixo 'ROLE_'.</p>
 *
 * <p>Roles esperadas do Keycloak: ADMIN, ANALISTA, GESTOR, EXTERNO, VIEWER</p>
 *
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationConverter.class);

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String SCOPE_CLAIM = "scope";
    private static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * Converte um token JWT em um objeto AbstractAuthenticationToken do Spring Security.
     *
     * @param jwt o token JWT a ser convertido
     * @return AbstractAuthenticationToken com as authorities extraídas do token
     * @throws IllegalArgumentException se o JWT for nulo ou inválido
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (jwt == null) {
            logger.warn("Tentativa de conversão com JWT nulo");
            throw new IllegalArgumentException("JWT não pode ser nulo");
        }

        try {
            logger.debug("Iniciando conversão do JWT para Authentication. Subject: {}", jwt.getSubject());

            Collection<GrantedAuthority> authorities = extrairAuthoritiesDoToken(jwt);

            logger.info("JWT convertido com sucesso. Subject: {}, Authorities: {}",
                    jwt.getSubject(),
                    authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()));

            return new PreAuthenticatedAuthenticationToken(jwt.getSubject(), jwt, authorities);

        } catch (Exception e) {
            logger.error("Erro ao converter JWT para Authentication. Subject: {}, Erro: {}", 
                    jwt.getSubject(), e.getMessage(), e);
            throw new IllegalArgumentException("Falha na conversão do JWT", e);
        }
    }

    /**
     * Extrai authorities do token JWT do Keycloak.
     * 
     * <p>Prioriza roles do realm_access.roles, mas também verifica resource_access
     * e scope como fallback para máxima compatibilidade.</p>
     *
     * @param jwt o token JWT
     * @return Collection de GrantedAuthority
     */
    private Collection<GrantedAuthority> extrairAuthoritiesDoToken(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        try {
            // Prioridade 1: Extrair roles do realm_access.roles (padrão Keycloak)
            authorities.addAll(extrairRolesDoRealmAccess(jwt));

            // Prioridade 2: Extrair roles do resource_access como fallback
            if (authorities.isEmpty()) {
                authorities.addAll(extrairRolesDoResourceAccess(jwt));
                logger.debug("Usou resource_access como fallback para roles");
            }

            // Prioridade 3: Extrair do scope como último recurso
            if (authorities.isEmpty()) {
                authorities.addAll(extrairRolesDoScope(jwt));
                logger.debug("Usou scope como fallback para roles");
            }

            // Garantir que sempre há pelo menos uma role padrão
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + "VIEWER"));
                logger.warn("Nenhuma role encontrada no JWT. Atribuindo role padrão VIEWER. Subject: {}", 
                        jwt.getSubject());
            }

        } catch (Exception e) {
            logger.error("Erro crítico ao extrair authorities do JWT. Subject: {}, Erro: {}", 
                    jwt.getSubject(), e.getMessage(), e);
            // Em caso de erro, atribuir role mínima para não falhar completamente
            authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + "VIEWER"));
        }

        return authorities;
    }

    /**
     * Extrai roles do claim realm_access.roles (método principal do Keycloak).
     *
     * @param jwt o token JWT
     * @return Collection de GrantedAuthority extraídas do realm_access
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extrairRolesDoRealmAccess(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
            if (realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)) {
                Object rolesObj = realmAccess.get(ROLES_CLAIM);

                if (rolesObj instanceof Collection) {
                    Collection<String> roles = (Collection<String>) rolesObj;
                    
                    for (String role : roles) {
                        if (StringUtils.hasText(role)) {
                            String normalizedRole = normalizarRole(role);
                            authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + normalizedRole));
                        }
                    }

                    logger.debug("Roles extraídas do realm_access: {} -> Authorities: {}", 
                            roles, 
                            authorities.stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao extrair roles do realm_access: {}", e.getMessage());
        }

        return authorities;
    }

    /**
     * Extrai roles do claim resource_access como fallback.
     * 
     * <p>Percorre todos os clientes em resource_access e extrai suas roles.</p>
     *
     * @param jwt o token JWT
     * @return Collection de GrantedAuthority extraídas do resource_access
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extrairRolesDoResourceAccess(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        try {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
            if (resourceAccess != null) {
                
                for (Map.Entry<String, Object> clientEntry : resourceAccess.entrySet()) {
                    String clientId = clientEntry.getKey();
                    Object clientObj = clientEntry.getValue();

                    if (clientObj instanceof Map) {
                        Map<String, Object> client = (Map<String, Object>) clientObj;
                        
                        if (client.containsKey(ROLES_CLAIM)) {
                            Object rolesObj = client.get(ROLES_CLAIM);
                            
                            if (rolesObj instanceof Collection) {
                                Collection<String> roles = (Collection<String>) rolesObj;
                                
                                for (String role : roles) {
                                    if (StringUtils.hasText(role)) {
                                        String normalizedRole = normalizarRole(role);
                                        authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + normalizedRole));
                                    }
                                }
                                
                                logger.debug("Roles extraídas do resource_access[{}]: {}", clientId, roles);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao extrair roles do resource_access: {}", e.getMessage());
        }

        return authorities;
    }

    /**
     * Extrai roles do claim scope como último recurso.
     * 
     * <p>Converte scopes em roles se não encontrar roles nos outros claims.</p>
     *
     * @param jwt o token JWT
     * @return Collection de GrantedAuthority extraídas do scope
     */
    private Collection<GrantedAuthority> extrairRolesDoScope(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        try {
            String scope = jwt.getClaimAsString(SCOPE_CLAIM);
            if (StringUtils.hasText(scope)) {
                String[] scopes = scope.split("\\s+");
                
                for (String scopeItem : scopes) {
                    if (StringUtils.hasText(scopeItem)) {
                        // Converter scopes em roles (ex: "admin" -> "ROLE_ADMIN")
                        String normalizedRole = normalizarRole(scopeItem);
                        authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + normalizedRole));
                    }
                }
                
                logger.debug("Roles extraídas do scope: {} -> {}", scope, 
                        authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            logger.warn("Erro ao extrair roles do scope: {}", e.getMessage());
        }

        return authorities;
    }

    /**
     * Normaliza o nome da role para seguir o padrão esperado.
     * 
     * <p>Converte para maiúsculo e garante que seja uma das roles esperadas.</p>
     *
     * @param role a role original
     * @return role normalizada
     */
    private String normalizarRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "VIEWER";
        }

        String upperRole = role.trim().toUpperCase();

        // Validar se é uma das roles esperadas
        Set<String> rolesValidas = Set.of("ADMIN", "ANALISTA", "GESTOR", "EXTERNO", "VIEWER");
        
        if (rolesValidas.contains(upperRole)) {
            return upperRole;
        }

        // Tentar mapear roles similares
        switch (upperRole) {
            case "ADMINISTRATOR":
            case "ADMINISTRADOR":
                return "ADMIN";
            case "ANALYST":
                return "ANALISTA";
            case "MANAGER":
            case "GERENTE":
                return "GESTOR";
            case "EXTERNAL":
                return "EXTERNO";
            case "USER":
            case "USUARIO":
            default:
                logger.debug("Role '{}' não reconhecida. Mapeando para VIEWER", role);
                return "VIEWER";
        }
    }
}
