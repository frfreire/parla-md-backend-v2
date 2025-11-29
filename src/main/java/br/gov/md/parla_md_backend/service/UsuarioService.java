package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UsuarioService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public UsuarioService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<UsuarioDTO> getAllUsers() {
        String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);
        HttpHeaders headers = new HttpHeaders();
        // Adicione aqui a lógica para obter e incluir o token de acesso
        HttpEntity<String> entity = new HttpEntity<>(headers);

        UsuarioDTO[] users = restTemplate.exchange(url, HttpMethod.GET, entity, UsuarioDTO[].class).getBody();
        return Arrays.asList(users);
    }

    public UsuarioDTO createUser(UsuarioDTO usuarioDTO) {
        String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);
        HttpHeaders headers = new HttpHeaders();
        // Adicione aqui a lógica para obter e incluir o token de acesso
        HttpEntity<UsuarioDTO> entity = new HttpEntity<>(usuarioDTO, headers);

        return restTemplate.postForObject(url, entity, UsuarioDTO.class);
    }

    public UsuarioDTO updateUser(String userId, UsuarioDTO usuarioDTO) {
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);
        HttpHeaders headers = new HttpHeaders();
        // TODO Criar a lógica para obter e incluir o token de acesso
        HttpEntity<UsuarioDTO> entity = new HttpEntity<>(usuarioDTO, headers);

        restTemplate.put(url, entity);
        return usuarioDTO;
    }

    public void deleteUser(String userId) {
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);
        HttpHeaders headers = new HttpHeaders();
        // TODO Criar a lógica para obter e incluir o token de acesso
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
