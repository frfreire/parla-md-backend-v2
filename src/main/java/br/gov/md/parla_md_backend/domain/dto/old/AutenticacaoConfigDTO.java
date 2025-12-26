package br.gov.md.parla_md_backend.domain.dto.old;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO que contém as configurações públicas de autenticação do Keycloak.
 * 
 * <p>Este DTO fornece informações necessárias para que aplicações frontend
 * possam configurar corretamente a integração com o Keycloak, incluindo
 * URLs de autenticação, realm, client ID e outras configurações públicas.</p>
 * 
 * <p>Estas informações são seguras para exposição pública e não contêm
 * dados sensíveis como secrets ou chaves privadas.</p>
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
@Schema(description = "Configurações públicas de autenticação do Keycloak")
public class AutenticacaoConfigDTO {
    
    @Schema(description = "URL base do servidor Keycloak", 
            example = "http://localhost:8080")
    @JsonProperty("keycloak_url")
    private String keycloakUrl;
    
    @Schema(description = "Nome do realm do Keycloak", 
            example = "parla-md-realm")
    @JsonProperty("realm")
    private String realm;
    
    @Schema(description = "ID do cliente público do Keycloak", 
            example = "parla-md-frontend")
    @JsonProperty("client_id")
    private String clientId;
    
    @Schema(description = "URL completa para login", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/auth")
    @JsonProperty("login_url")
    private String loginUrl;
    
    @Schema(description = "URL completa para logout", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/logout")
    @JsonProperty("logout_url")
    private String logoutUrl;
    
    @Schema(description = "URL para obter tokens", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/token")
    @JsonProperty("token_url")
    private String tokenUrl;
    
    @Schema(description = "URL para informações do usuário", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/userinfo")
    @JsonProperty("userinfo_url")
    private String userinfoUrl;
    
    @Schema(description = "URL do well-known configuration", 
            example = "http://localhost:8080/realms/parla-md-realm/.well-known/openid-configuration")
    @JsonProperty("wellknown_url")
    private String wellknownUrl;
    
    @Schema(description = "URL para verificação de tokens", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/token/introspect")
    @JsonProperty("introspect_url")
    private String introspectUrl;
    
    @Schema(description = "URL do JWKS (JSON Web Key Set)", 
            example = "http://localhost:8080/realms/parla-md-realm/protocol/openid-connect/certs")
    @JsonProperty("jwks_url")
    private String jwksUrl;
    
    @Schema(description = "Scopes OAuth2 suportados", 
            example = "[\"openid\", \"profile\", \"email\", \"roles\"]")
    @JsonProperty("scopes")
    private List<String> scopes;
    
    @Schema(description = "Tipos de response suportados", 
            example = "[\"code\", \"token\", \"id_token\"]")
    @JsonProperty("response_types")
    private List<String> responseTypes;
    
    @Schema(description = "Métodos de autenticação suportados", 
            example = "[\"client_secret_post\", \"client_secret_basic\"]")
    @JsonProperty("auth_methods")
    private List<String> authMethods;
    
    @Schema(description = "Algoritmos de assinatura suportados", 
            example = "[\"RS256\", \"ES256\"]")
    @JsonProperty("signing_algorithms")
    private List<String> signingAlgorithms;
    
    @Schema(description = "Tempo de vida padrão do token em segundos", 
            example = "3600")
    @JsonProperty("token_lifetime")
    private Integer tokenLifetime;
    
    @Schema(description = "Tempo de vida do refresh token em segundos", 
            example = "7200")
    @JsonProperty("refresh_token_lifetime")
    private Integer refreshTokenLifetime;
    
    @Schema(description = "Indica se PKCE é obrigatório", 
            example = "true")
    @JsonProperty("pkce_required")
    private Boolean pkceRequired;
    
    @Schema(description = "Configurações de CORS permitidas")
    @JsonProperty("cors_config")
    private CorsConfigDTO corsConfig;
    
    @Schema(description = "Informações da versão do Keycloak", 
            example = "23.0.1")
    @JsonProperty("keycloak_version")
    private String keycloakVersion;
    
    @Schema(description = "Configurações adicionais específicas do ambiente")
    @JsonProperty("configuracoes_extras")
    private Map<String, Object> configuracoesExtras;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CorsConfigDTO {
        
        @Schema(description = "Origens permitidas para CORS", 
                example = "[\"http://localhost:3000\", \"https://parla-md.gov.br\"]")
        @JsonProperty("allowed_origins")
        private List<String> allowedOrigins;
        
        @Schema(description = "Métodos HTTP permitidos", 
                example = "[\"GET\", \"POST\", \"PUT\", \"DELETE\", \"OPTIONS\"]")
        @JsonProperty("allowed_methods")
        private List<String> allowedMethods;
        
        @Schema(description = "Headers permitidos", 
                example = "[\"Authorization\", \"Content-Type\", \"X-Requested-With\"]")
        @JsonProperty("allowed_headers")
        private List<String> allowedHeaders;
        
        @Schema(description = "Indica se credenciais são permitidas", 
                example = "true")
        @JsonProperty("allow_credentials")
        private Boolean allowCredentials;
        
        @Schema(description = "Tempo de cache para preflight requests em segundos", 
                example = "3600")
        @JsonProperty("max_age")
        private Integer maxAge;
    }
    
    public String construirUrlLogin(String redirectUri, String state) {
        StringBuilder urlBuilder = new StringBuilder(loginUrl);
        urlBuilder.append("?client_id=").append(clientId);
        urlBuilder.append("&response_type=code");
        urlBuilder.append("&scope=openid profile email");
        
        if (redirectUri != null) {
            urlBuilder.append("&redirect_uri=").append(redirectUri);
        }
        
        if (state != null) {
            urlBuilder.append("&state=").append(state);
        }
        
        return urlBuilder.toString();
    }
    
    public String construirUrlLogout(String redirectUri, String idTokenHint) {
        StringBuilder urlBuilder = new StringBuilder(logoutUrl);
        urlBuilder.append("?client_id=").append(clientId);
        
        if (redirectUri != null) {
            urlBuilder.append("&post_logout_redirect_uri=").append(redirectUri);
        }
        
        if (idTokenHint != null) {
            urlBuilder.append("&id_token_hint=").append(idTokenHint);
        }
        
        return urlBuilder.toString();
    }
    
    public boolean isConfiguracaoValida() {
        return keycloakUrl != null && 
               realm != null && 
               clientId != null && 
               loginUrl != null && 
               logoutUrl != null;
    }
}