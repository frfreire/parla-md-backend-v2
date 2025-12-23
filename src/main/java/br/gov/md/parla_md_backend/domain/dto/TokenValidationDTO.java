package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * DTO para validação de tokens JWT no sistema Parla-MD.
 * 
 * <p>Este DTO é usado tanto para enviar tokens para validação quanto
 * para receber informações sobre a validade e conteúdo dos tokens.</p>
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
@Schema(description = "DTO para validação de tokens JWT")
public class TokenValidationDTO {
    
    @Schema(description = "Token JWT a ser validado", 
            example = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJr...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("token")
    @NotBlank(message = "Token é obrigatório")
    private String token;
    
    @Schema(description = "Indica se o token é válido", 
            example = "true")
    @JsonProperty("valido")
    private Boolean valido;
    
    @Schema(description = "Motivo da invalidação do token", 
            example = "Token expirado")
    @JsonProperty("motivo_invalidacao")
    private String motivoInvalidacao;
    
    @Schema(description = "Data e hora de expiração do token")
    @JsonProperty("expira_em")
    private LocalDateTime expiraEm;
    
    @Schema(description = "Data e hora de emissão do token")
    @JsonProperty("emitido_em")
    private LocalDateTime emitidoEm;
    
    @Schema(description = "Subject (usuário) do token", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("subject")
    private String subject;
    
    @Schema(description = "Issuer (emissor) do token", 
            example = "http://localhost:8080/realms/parla-md-realm")
    @JsonProperty("issuer")
    private String issuer;
    
    @Schema(description = "Audience (audiência) do token", 
            example = "[\"parla-md-backend\", \"account\"]")
    @JsonProperty("audience")
    private Set<String> audience;
    
    @Schema(description = "Scopes presentes no token", 
            example = "[\"openid\", \"profile\", \"email\"]")
    @JsonProperty("scopes")
    private Set<String> scopes;
    
    @Schema(description = "Roles extraídas do token", 
            example = "[\"ANALISTA\", \"VIEWER\"]")
    @JsonProperty("roles")
    private Set<String> roles;
    
    @Schema(description = "Tipo do token", 
            example = "Bearer")
    @JsonProperty("tipo_token")
    private String tipoToken;
    
    @Schema(description = "ID da sessão (session ID)", 
            example = "7f8d4e2a-9b1c-4f5e-8d2a-1b3c4d5e6f7g")
    @JsonProperty("session_id")
    private String sessionId;
    
    @Schema(description = "Claims adicionais do token")
    @JsonProperty("claims_adicionais")
    private Map<String, Object> claimsAdicionais;
    
    @Schema(description = "Tempo restante até expiração em segundos", 
            example = "3600")
    @JsonProperty("tempo_restante_segundos")
    private Long tempoRestanteSegundos;
    
    @Schema(description = "Indica se o token expira em breve (menos de 5 minutos)", 
            example = "false")
    @JsonProperty("expira_em_breve")
    private Boolean expiraEmBreve;
    
    public static TokenValidationDTO tokenInvalido(String token, String motivoInvalidacao) {
        return TokenValidationDTO.builder()
                .token(token)
                .valido(false)
                .motivoInvalidacao(motivoInvalidacao)
                .build();
    }
    
    public static TokenValidationDTO tokenValido(String token) {
        return TokenValidationDTO.builder()
                .token(token)
                .valido(true)
                .build();
    }
    
    public boolean isTokenExpirado() {
        if (expiraEm == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiraEm);
    }
    
    public boolean isTokenExpiraEmBreve() {
        if (expiraEm == null) {
            return false;
        }
        return LocalDateTime.now().plusMinutes(5).isAfter(expiraEm);
    }
    
    public long calcularTempoRestanteSegundos() {
        if (expiraEm == null) {
            return 0L;
        }
        
        LocalDateTime agora = LocalDateTime.now();
        if (agora.isAfter(expiraEm)) {
            return 0L;
        }
        
        return java.time.Duration.between(agora, expiraEm).getSeconds();
    }
    
    public boolean possuiRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    public boolean possuiScope(String scope) {
        return scopes != null && scopes.contains(scope);
    }
}