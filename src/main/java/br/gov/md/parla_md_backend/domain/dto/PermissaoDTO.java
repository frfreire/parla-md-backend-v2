package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Set;

/**
 * DTO para verificação de permissões específicas no sistema Parla-MD.
 * 
 * <p>Este DTO é usado para enviar solicitações de verificação de permissões
 * e receber respostas sobre se um usuário possui determinadas permissões
 * ou roles para acessar recursos específicos.</p>
 * 
 * @author Sistema Parla-MD
 * @version 1.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO para verificação de permissões")
public class PermissaoDTO {
    
    @Schema(description = "Nome da permissão a ser verificada", 
            example = "READ_PROJETOS",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("permissao")
    @NotBlank(message = "Permissão é obrigatória")
    private String permissao;
    
    @Schema(description = "Recurso específico ao qual a permissão se aplica", 
            example = "projeto:123")
    @JsonProperty("recurso")
    private String recurso;
    
    @Schema(description = "Ação que está sendo solicitada", 
            example = "READ")
    @JsonProperty("acao")
    private String acao;
    
    @Schema(description = "Contexto adicional para verificação da permissão", 
            example = "departamento:assessoria-tecnica")
    @JsonProperty("contexto")
    private String contexto;
    
    @Schema(description = "Conjunto de roles alternativas que também concedem acesso", 
            example = "[\"ADMIN\", \"GESTOR\"]")
    @JsonProperty("roles_alternativas")
    private Set<String> rolesAlternativas;
    
    @Schema(description = "Conjunto de permissões que devem estar presentes em conjunto", 
            example = "[\"READ_PROJETOS\", \"READ_TRAMITACAO\"]")
    @JsonProperty("permissoes_obrigatorias")
    private Set<String> permissoesObrigatorias;
    
    @Schema(description = "Indica se a verificação deve ser estrita (todas as permissões) ou flexível (qualquer uma)", 
            example = "false")
    @JsonProperty("verificacao_estrita")
    private Boolean verificacaoEstrita;
    
    @Schema(description = "Resultado da verificação de permissão", 
            example = "true")
    @JsonProperty("permitido")
    private Boolean permitido;
    
    @Schema(description = "Motivo da negação da permissão", 
            example = "Usuário não possui role ANALISTA")
    @JsonProperty("motivo_negacao")
    private String motivoNegacao;
    
    @Schema(description = "Detalhes adicionais sobre a verificação")
    @JsonProperty("detalhes_verificacao")
    private Map<String, Object> detalhesVerificacao;
    
    public static PermissaoDTO verificarPermissao(String permissao) {
        return PermissaoDTO.builder()
                .permissao(permissao)
                .verificacaoEstrita(false)
                .build();
    }
    
    public static PermissaoDTO verificarPermissaoRecurso(String permissao, String recurso) {
        return PermissaoDTO.builder()
                .permissao(permissao)
                .recurso(recurso)
                .verificacaoEstrita(false)
                .build();
    }
    
    public static PermissaoDTO verificarAcaoRecurso(String acao, String recurso) {
        return PermissaoDTO.builder()
                .acao(acao)
                .recurso(recurso)
                .permissao(acao + "_" + recurso.toUpperCase())
                .verificacaoEstrita(false)
                .build();
    }
    
    public static PermissaoDTO verificarComRolesAlternativas(String permissao, Set<String> rolesAlternativas) {
        return PermissaoDTO.builder()
                .permissao(permissao)
                .rolesAlternativas(rolesAlternativas)
                .verificacaoEstrita(false)
                .build();
    }
    
    public static PermissaoDTO permissaoPermitida(String permissao) {
        return PermissaoDTO.builder()
                .permissao(permissao)
                .permitido(true)
                .build();
    }
    
    public static PermissaoDTO permissaoNegada(String permissao, String motivoNegacao) {
        return PermissaoDTO.builder()
                .permissao(permissao)
                .permitido(false)
                .motivoNegacao(motivoNegacao)
                .build();
    }
    
    public boolean isPermitida() {
        return permitido != null && permitido;
    }
    
    public boolean isNegada() {
        return permitido != null && !permitido;
    }
    
    public boolean isVerificacaoEstrita() {
        return verificacaoEstrita != null && verificacaoEstrita;
    }
    
    public String construirChavePermissao() {
        if (acao != null && recurso != null) {
            return acao.toUpperCase() + "_" + recurso.toUpperCase();
        } else if (permissao != null) {
            return permissao.toUpperCase();
        }
        return null;
    }
}