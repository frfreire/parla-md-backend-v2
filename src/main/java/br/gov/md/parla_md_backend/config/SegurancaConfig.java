package br.gov.md.parla_md_backend.config;

import br.gov.md.parla_md_backend.security.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança do sistema Parla-MD com autenticação JWT via Keycloak.
 *
 * <p>Esta configuração implementa autenticação OAuth2 com Keycloak seguindo as especificações:</p>
 * <ul>
 *   <li>Autenticação OAuth2 com tokens JWT</li>
 *   <li>Autorização baseada em roles (RBAC)</li>
 *   <li>Validação de tokens JWT do Keycloak</li>
 *   <li>Configuração CORS para frontend</li>
 *   <li>Endpoints públicos e protegidos bem definidos</li>
 * </ul>
 *
 * <p><strong>URLs Públicas:</strong></p>
 * <ul>
 *   <li>/api/publico/** - Endpoints públicos da API</li>
 *   <li>/api/auth/config - Configuração de autenticação</li>
 *   <li>/api/auth/health - Status de saúde da autenticação</li>
 *   <li>/v3/api-docs/** - Documentação OpenAPI</li>
 *   <li>/swagger-ui/** - Interface Swagger</li>
 * </ul>
 *
 * <p><strong>URLs Protegidas:</strong></p>
 * <ul>
 *   <li>/api/auth/** - Endpoints de autenticação (exceto config e health)</li>
 *   <li>Todos os outros endpoints requerem autenticação</li>
 * </ul>
 *
 * <p><strong>Roles Esperadas:</strong> ADMIN, ANALISTA, GESTOR, EXTERNO, VIEWER</p>
 *
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(name = "app.security.oauth2.enabled", havingValue = "true", matchIfMissing = false)
public class SegurancaConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private List<String> allowedOrigins;

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SegurancaConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /**
     * Configura a cadeia de filtros de segurança.
     *
     * <p>Define endpoints públicos e protegidos, configuração OAuth2 e políticas de sessão.</p>
     *
     * @param http builder para configuração HTTP
     * @return SecurityFilterChain configurada
     * @throws Exception em caso de erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Configuração CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Desabilitar CSRF para APIs stateless
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configuração de sessão stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configuração de autorização de endpoints
            .authorizeHttpRequests(auth -> auth
                // ========== ENDPOINTS PÚBLICOS ==========
                
                // Endpoints públicos da API
                .requestMatchers("/api/publico/**").permitAll()
                
                // Configuração e health da autenticação (públicos)
                .requestMatchers("/api/auth/config").permitAll()
                .requestMatchers("/api/auth/health").permitAll()
                
                // Documentação da API (pública)
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Actuator endpoints (públicos básicos)
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // ========== ENDPOINTS PROTEGIDOS ==========
                
                // Endpoints de autenticação (protegidos, exceto config e health)
                .requestMatchers("/api/auth/**").authenticated()
                
                // Endpoints administrativos (apenas ADMIN)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints de gestão (ADMIN e GESTOR)
                .requestMatchers("/api/gestor/**").hasAnyRole("ADMIN", "GESTOR")
                
                // Endpoints de análise (ADMIN, GESTOR e ANALISTA)
                .requestMatchers("/api/analista/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA")
                
                // Endpoints de dados legislativos (todos os roles autenticados)
                .requestMatchers("/api/dados-legislativos/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA", "EXTERNO", "VIEWER")
                
                // Endpoints de proposições (todos os roles autenticados)
                .requestMatchers("/api/proposicoes/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA", "EXTERNO", "VIEWER")
                
                // Endpoints de tramitação (todos os roles autenticados)
                .requestMatchers("/api/tramitacao/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA", "EXTERNO", "VIEWER")
                
                // Endpoints de pareceres (ADMIN, GESTOR e ANALISTA)
                .requestMatchers("/api/pareceres/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA")
                
                // Endpoints de posicionamentos (ADMIN, GESTOR e ANALISTA)
                .requestMatchers("/api/posicionamentos/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA")
                
                // Endpoints de triagem (ADMIN, GESTOR e ANALISTA)
                .requestMatchers("/api/triagem/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA")
                
                // Endpoints de IA Generativa (ADMIN, GESTOR e ANALISTA)
                .requestMatchers("/api/ia-generativa/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA")
                
                // Endpoints de usuário (todos os roles autenticados)
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GESTOR", "ANALISTA", "EXTERNO", "VIEWER")
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            
            // Configuração OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            
            .build();
    }

    /**
     * Configura o decoder JWT para validação de tokens.
     *
     * <p>Utiliza o Nimbus JWT Decoder para validar tokens JWT emitidos pelo Keycloak.</p>
     *
     * @return JwtDecoder configurado
     * @throws IllegalStateException se a URI do JWK Set não estiver configurada
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri == null || jwkSetUri.trim().isEmpty()) {
            throw new IllegalStateException(
                    "URI do JWK Set não configurada. Configure 'spring.security.oauth2.resourceserver.jwt.jwk-set-uri'");
        }

        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                .build();
    }

    /**
     * Configura o CORS (Cross-Origin Resource Sharing).
     *
     * <p>Permite requisições do frontend e outras origens autorizadas.</p>
     *
     * @return CorsConfigurationSource configurada
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origens permitidas (frontend)
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials",
            "X-Total-Count"
        ));
        
        // Permitir cookies/credenciais
        configuration.setAllowCredentials(true);
        
        // Cache de CORS
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
