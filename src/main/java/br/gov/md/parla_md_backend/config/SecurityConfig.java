package br.gov.md.parla_md_backend.config;

import br.gov.md.parla_md_backend.security.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança com OAuth2/JWT via Keycloak
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== ENDPOINTS PÚBLICOS - DEVE VIR PRIMEIRO =====
                        .requestMatchers("/api/publico/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ===== SWAGGER E OPENAPI - ORDEM IMPORTANTE =====
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ===== ENDPOINTS DE AUTENTICAÇÃO =====
                        .requestMatchers("/api/auth/config", "/api/auth/health").permitAll()

                        // ===== ENDPOINTS PROTEGIDOS =====
                        // Processos
                        .requestMatchers(HttpMethod.POST, "/api/processos").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/processos/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")
                        .requestMatchers(HttpMethod.PUT, "/api/processos/*/status").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/processos/*/posicao-final").hasRole("ADMIN")

                        // Tramitação
                        .requestMatchers(HttpMethod.POST, "/api/tramitacoes").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/tramitacoes/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")
                        .requestMatchers(HttpMethod.PUT, "/api/tramitacoes/*/receber").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Parecer
                        .requestMatchers(HttpMethod.POST, "/api/pareceres").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/pareceres/*/emitir").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/pareceres/*/aprovar").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/pareceres/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Posicionamento
                        .requestMatchers(HttpMethod.POST, "/api/posicionamentos").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/posicionamentos/*/registrar").hasAnyRole("ADMIN", "EXTERNO")
                        .requestMatchers(HttpMethod.GET, "/api/posicionamentos/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Setores e Órgãos
                        .requestMatchers(HttpMethod.POST, "/api/setores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/setores/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/setores/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/setores/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")

                        .requestMatchers(HttpMethod.POST, "/api/orgaos-externos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/orgaos-externos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/orgaos-externos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orgaos-externos/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")

                        // Qualquer outra requisição requer autenticação
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}