package br.gov.md.parla_md_backend.domain.dto.old;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTO que representa as informações completas de um usuário autenticado no sistema Parla-MD.
 * 
 * <p>Este DTO contém todas as informações relevantes do usuário extraídas do token JWT
 * do Keycloak, incluindo dados pessoais, roles, permissões e metadados de sessão.</p>
 * 
 * <p>As informações são obtidas dos claims do JWT e enriquecidas com dados
 * adicionais do Keycloak Admin API quando necessário.</p>
 * 
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Informações completas do usuário autenticado")
public class UsuarioInfoDTO {
    
    @Schema(description = "Identificador único do usuário no Keycloak", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    private String id;
    
    @Schema(description = "Nome de usuário (username)", example = "joao.silva")
    @JsonProperty("username")
    private String username;
    
    @Schema(description = "Endereço de email do usuário", example = "joao.silva@md.gov.br")
    @JsonProperty("email")
    private String email;
    
    @Schema(description = "Primeiro nome do usuário", example = "João")
    @JsonProperty("primeiro_nome")
    private String primeiroNome;
    
    @Schema(description = "Último nome do usuário", example = "Silva")
    @JsonProperty("ultimo_nome")
    private String ultimoNome;
    
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    @JsonProperty("nome_completo")
    private String nomeCompleto;
    
    @Schema(description = "Conjunto de roles atribuídas ao usuário", 
            example = "[\"ANALISTA\", \"VIEWER\"]")
    @JsonProperty("roles")
    private Set<String> roles;
    
    @Schema(description = "Conjunto de permissões específicas do usuário", 
            example = "[\"READ_PROJETOS\", \"WRITE_PROJETOS\"]")
    @JsonProperty("permissoes")
    private Set<String> permissoes;
    
    @Schema(description = "Indica se o usuário está ativo no sistema", example = "true")
    @JsonProperty("ativo")
    private Boolean ativo;
    
    @Schema(description = "Data e hora do último login do usuário")
    @JsonProperty("ultimo_login")
    private LocalDateTime ultimoLogin;
    
    @Schema(description = "Data e hora de criação da conta do usuário")
    @JsonProperty("criado_em")
    private LocalDateTime criadoEm;
    
    @Schema(description = "URL do avatar/foto do usuário", 
            example = "https://gravatar.com/avatar/...")
    @JsonProperty("avatar")
    private String avatar;
    
    @Schema(description = "Organização ou órgão ao qual o usuário pertence", 
            example = "Assembleia Legislativa do Estado de Minas Gerais")
    @JsonProperty("organizacao")
    private String organizacao;
    
    @Schema(description = "Cargo ou função do usuário", example = "Analista Legislativo")
    @JsonProperty("cargo")
    private String cargo;
    
    @Schema(description = "Departamento ou setor do usuário", example = "Assessoria Técnica")
    @JsonProperty("departamento")
    private String departamento;
    
    @Schema(description = "Telefone de contato do usuário", example = "(31) 2533-5000")
    @JsonProperty("telefone")
    private String telefone;
    
    @Schema(description = "Indica se o email foi verificado", example = "true")
    @JsonProperty("email_verificado")
    private Boolean emailVerificado;
    
    @Schema(description = "Fuso horário preferido do usuário", example = "America/Sao_Paulo")
    @JsonProperty("fuso_horario")
    private String fusoHorario;
    
    @Schema(description = "Idioma preferido do usuário", example = "pt-BR")
    @JsonProperty("idioma")
    private String idioma;
    
    @Schema(description = "Metadados adicionais e atributos customizados do usuário")
    @JsonProperty("metadados")
    private Map<String, Object> metadados;
    
    public boolean possuiRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    public boolean possuiQualquerRole(Set<String> rolesParaVerificar) {
        if (roles == null || rolesParaVerificar == null) {
            return false;
        }
        return roles.stream().anyMatch(rolesParaVerificar::contains);
    }
    
    public boolean possuiPermissao(String permissao) {
        return permissoes != null && permissoes.contains(permissao);
    }
    
    public boolean possuiQualquerPermissao(Set<String> permissoesParaVerificar) {
        if (permissoes == null || permissoesParaVerificar == null) {
            return false;
        }
        return permissoes.stream().anyMatch(permissoesParaVerificar::contains);
    }
    
    public boolean isAdministrador() {
        return possuiRole("ADMIN");
    }
    
    public boolean isUsuarioAtivo() {
        return ativo != null && ativo;
    }
    
    public String getNomeParaExibicao() {
        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {
            return nomeCompleto;
        }
        return username;
    }
    
    public String construirNomeCompleto() {
        if (primeiroNome == null && ultimoNome == null) {
            return null;
        }
        
        StringBuilder nomeBuilder = new StringBuilder();
        if (primeiroNome != null) {
            nomeBuilder.append(primeiroNome);
        }
        if (ultimoNome != null) {
            if (nomeBuilder.length() > 0) {
                nomeBuilder.append(" ");
            }
            nomeBuilder.append(ultimoNome);
        }
        
        return nomeBuilder.toString();
    }
}