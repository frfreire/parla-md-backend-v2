package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para informações do usuário autenticado.
 *
 * Contém todas as informações necessárias sobre o usuário
 * extraídas do token JWT do Keycloak.
 *
 * @author fabricio.freire
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

    @NotBlank(message = "ID do usuário é obrigatório")
    private String id;

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @Email(message = "E-mail deve ter formato válido")
    private String email;

    private String nome;

    private String sobrenome;

    private String nomeCompleto;

    @NotNull(message = "Lista de roles não pode ser nula")
    private List<String> roles;

    private List<String> permissoes;

    @Builder.Default
    private Boolean isActive = true;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tokenExpiracao;

    private String setor;

    private String cargo;

    private Map<String, Object> additionalInfo;

    private String urlAvatar;

    private Map<String, Object> preferences;

    @Builder.Default
    private boolean emailVerificado = false;

    /**
     * Indica se o usuário possui permissão administrativa.
     *
     * @return true se o usuário tem role ADMIN
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }

    /**
     * Indica se o usuário possui permissão de analista.
     *
     * @return true se o usuário tem role ANALISTA
     */
    public boolean isAnalista() {
        return roles != null && roles.contains("ANALISTA");
    }

    /**
     * Indica se o usuário possui permissão de gestor.
     *
     * @return true se o usuário tem role GESTOR
     */
    public boolean isGestor() {
        return roles != null && roles.contains("GESTOR");
    }

    public boolean isExterno() {
        return roles != null && roles.contains("EXTERNO");
    }

    /**
     * Indica se o usuário possui apenas permissão de visualização.
     *
     * @return true se o usuário tem apenas role VIEWER
     */
    public boolean isViewer() {
        return roles != null && roles.contains("VIEWER") && roles.size() == 1;
    }

    /**
     * Verifica se o usuário possui uma role específica.
     *
     * @param role a role a ser verificada
     * @return true se o usuário possui a role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Verifica se o usuário possui uma permissão específica.
     *
     * @param permission a permissão a ser verificada
     * @return true se o usuário possui a permissão
     */
    public boolean hasPermission(String permission) {
        return permissoes != null && permissoes.contains(permission);
    }

    public String getDisplayName() {
        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {
            return nomeCompleto;
        }
        if (nome != null && sobrenome != null) {
            return nome + " " + sobrenome;
        }
        return username;
    }

    /**
     * Verifica se o token está próximo do vencimento.
     *
     * @param minutosThreshold minutos antes do vencimento para considerar "próximo"
     * @return true se o token expira dentro do threshold especificado
     */
    public boolean isTokenNearExpiration(int minutosThreshold) {
        if (tokenExpiracao == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minutosThreshold);
        return tokenExpiracao.isBefore(threshold);
    }
}