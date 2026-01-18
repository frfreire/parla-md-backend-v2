package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final RestClient.Builder restClientBuilder;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    // ==================== RECORDS INTERNOS ====================

    record KeycloakUserResponse(
            String id,
            String username,
            String firstName,
            String lastName,
            String email,
            boolean enabled,
            Long createdTimestamp,
            Map<String, List<String>> attributes
    ) {}

    record KeycloakRoleResponse(String name) {}

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Busca usuário por ID
     */
    public UsuarioDTO buscarPorId(String userId) {
        log.debug("Buscando usuário por ID no Keycloak: {}", userId);

        String token = getAdminToken();

        try {
            KeycloakUserResponse kUser = restClientBuilder.build()
                    .get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(KeycloakUserResponse.class);

            if (kUser == null) {
                throw new RecursoNaoEncontradoException("Usuário não encontrado");
            }

            List<String> roles = buscarRolesDoUsuario(userId, token);

            return converterParaDTO(kUser, roles);

        } catch (Exception e) {
            log.error("Erro ao buscar usuário por ID: {}", userId, e);
            throw new RecursoNaoEncontradoException("Usuário não encontrado no Keycloak");
        }
    }

    /**
     * Busca usuário por email
     */
    public UsuarioDTO buscarPorEmail(String email) {
        log.debug("Buscando usuário por email no Keycloak: {}", email);

        String token = getAdminToken();

        try {
            List<KeycloakUserResponse> users = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(serverUrl + "/admin/realms/" + realm + "/users")
                            .queryParam("email", email)
                            .queryParam("exact", true)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null || users.isEmpty()) {
                throw new RecursoNaoEncontradoException("Usuário não encontrado");
            }

            KeycloakUserResponse kUser = users.getFirst();
            List<String> roles = buscarRolesDoUsuario(kUser.id(), token);

            return converterParaDTO(kUser, roles);

        } catch (Exception e) {
            log.error("Erro ao buscar usuário por email: {}", email, e);
            throw new RecursoNaoEncontradoException("Usuário não encontrado no Keycloak");
        }
    }

    public UsuarioDTO buscarPorUsername(String username) {
        log.debug("Buscando usuário por username no Keycloak: {}", username);

        String token = getAdminToken();

        try {
            List<KeycloakUserResponse> users = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(serverUrl + "/admin/realms/" + realm + "/users")
                            .queryParam("username", username)
                            .queryParam("exact", true)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null || users.isEmpty()) {
                throw new RecursoNaoEncontradoException("Usuário não encontrado");
            }

            KeycloakUserResponse kUser = users.getFirst();
            List<String> roles = buscarRolesDoUsuario(kUser.id(), token);

            return converterParaDTO(kUser, roles);

        } catch (Exception e) {
            log.error("Erro ao buscar usuário por username: {}", username, e);
            throw new RecursoNaoEncontradoException("Usuário não encontrado no Keycloak");
        }
    }

    public List<UsuarioDTO> listarTodos() {
        log.debug("Listando todos os usuários do Keycloak");

        String token = getAdminToken();

        try {
            List<KeycloakUserResponse> users = restClientBuilder.build()
                    .get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null) {
                return List.of();
            }

            return users.stream()
                    .map(user -> {
                        List<String> roles = buscarRolesDoUsuario(user.id(), token);
                        return converterParaDTO(user, roles);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao listar todos os usuários", e);
            return List.of();
        }
    }

    public List<UsuarioDTO> listarAtivos() {
        log.debug("Listando usuários ativos do Keycloak");

        return listarTodos().stream()
                .filter(UsuarioDTO::ativo)
                .toList();
    }

    public List<UsuarioDTO> buscarPorNome(String nome) {
        log.debug("Buscando usuários por nome no Keycloak: {}", nome);

        String token = getAdminToken();

        try {
            List<KeycloakUserResponse> users = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(serverUrl + "/admin/realms/" + realm + "/users")
                            .queryParam("search", nome)
                            .queryParam("max", 100)
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null) {
                return List.of();
            }

            return users.stream()
                    .map(user -> {
                        List<String> roles = buscarRolesDoUsuario(user.id(), token);
                        return converterParaDTO(user, roles);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao buscar usuários por nome: {}", nome, e);
            return List.of();
        }
    }

    public List<UsuarioDTO> listarPorRole(String roleName) {
        log.debug("Listando usuários com role no Keycloak: {}", roleName);

        String token = getAdminToken();

        try {
            List<KeycloakUserResponse> users = restClientBuilder.build()
                    .get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/roles/" + roleName + "/users")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null) {
                return List.of();
            }

            return users.stream()
                    .map(user -> {
                        List<String> roles = buscarRolesDoUsuario(user.id(), token);
                        return converterParaDTO(user, roles);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao listar usuários por role: {}", roleName, e);
            return List.of();
        }
    }

    public List<String> obterRolesUsuario(String userId) {
        log.debug("Obtendo roles do usuário no Keycloak: {}", userId);

        String token = getAdminToken();
        return buscarRolesDoUsuario(userId, token);
    }

    public boolean usuarioPossuiRole(String userId, String roleName) {
        List<String> roles = obterRolesUsuario(userId);
        return roles.contains(roleName);
    }

    public boolean usuarioEstaAtivo(String userId) {
        try {
            UsuarioDTO usuario = buscarPorId(userId);
            return usuario.ativo();
        } catch (Exception e) {
            return false;
        }
    }

    public long contarUsuarios() {
        log.debug("Contando usuários no Keycloak");

        String token = getAdminToken();

        try {
            Integer count = restClientBuilder.build()
                    .get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users/count")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Integer.class);

            return count != null ? count.longValue() : 0L;

        } catch (Exception e) {
            log.error("Erro ao contar usuários", e);
            return 0L;
        }
    }

    private List<String> buscarRolesDoUsuario(String userId, String token) {
        try {
            List<KeycloakRoleResponse> rolesResponse = restClientBuilder.build()
                    .get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (rolesResponse == null) {
                return List.of();
            }

            return rolesResponse.stream()
                    .map(KeycloakRoleResponse::name)
                    .toList();

        } catch (Exception e) {
            log.warn("Falha ao buscar roles do usuário {}", userId, e);
            return List.of();
        }
    }

    private String getAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        try {
            Map<String, Object> response = restClientBuilder.build()
                    .post()
                    .uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || !response.containsKey("access_token")) {
                throw new RuntimeException("Token não retornado pelo Keycloak");
            }

            return (String) response.get("access_token");

        } catch (Exception e) {
            log.error("Erro de autenticação com Keycloak", e);
            throw new RuntimeException("Falha na comunicação com servidor de identidade");
        }
    }

    private UsuarioDTO converterParaDTO(KeycloakUserResponse user, List<String> roles) {
        String nomeCompleto = String.format("%s %s",
                user.firstName() != null ? user.firstName() : "",
                user.lastName() != null ? user.lastName() : ""
        ).trim();

        if (nomeCompleto.isEmpty()) {
            nomeCompleto = user.username();
        }

        Map<String, List<String>> attributes = user.attributes() != null
                ? user.attributes()
                : Collections.emptyMap();

        LocalDateTime dataCriacao = null;
        if (user.createdTimestamp() != null) {
            dataCriacao = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(user.createdTimestamp()),
                    ZoneId.systemDefault()
            );
        }

        return new UsuarioDTO(
                user.id(),
                nomeCompleto,
                user.email(),
                getAttributeSafe(attributes, "cpf"),
                getAttributeSafe(attributes, "telefone"),
                getAttributeSafe(attributes, "cargo"),
                getAttributeSafe(attributes, "setorId"),
                getAttributeSafe(attributes, "setorNome"),
                roles,
                user.enabled(),
                dataCriacao,
                null, // dataAtualizacao - não disponível na API padrão
                null  // ultimoAcesso - não disponível na API padrão
        );
    }

    private String getAttributeSafe(Map<String, List<String>> attrs, String key) {
        if (attrs.containsKey(key) && !attrs.get(key).isEmpty()) {
            return attrs.get(key).get(0);
        }
        return null;
    }
}