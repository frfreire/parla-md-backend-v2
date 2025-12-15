package br.gov.md.parla_md_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança com OAuth2/JWT via Keycloak
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SegurancaConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/publico/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Endpoints de Processos
                        .requestMatchers(HttpMethod.POST, "/api/processos").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/processos/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")
                        .requestMatchers(HttpMethod.PUT, "/api/processos/*/status").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/processos/*/posicao-final").hasRole("ADMIN")

                        // Endpoints de Tramitação
                        .requestMatchers(HttpMethod.POST, "/api/tramitacoes").hasAnyRole("ADMIN", "ANALISTA", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/tramitacoes/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")
                        .requestMatchers(HttpMethod.PUT, "/api/tramitacoes/*/receber").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Endpoints de Parecer
                        .requestMatchers(HttpMethod.POST, "/api/pareceres").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/pareceres/*/emitir").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/pareceres/*/aprovar").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/api/pareceres/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Endpoints de Posicionamento
                        .requestMatchers(HttpMethod.POST, "/api/posicionamentos").hasAnyRole("ADMIN", "GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/posicionamentos/*/registrar").hasAnyRole("ADMIN", "EXTERNO")
                        .requestMatchers(HttpMethod.GET, "/api/posicionamentos/**").hasAnyRole("ADMIN", "ANALISTA", "GESTOR", "EXTERNO")

                        // Endpoints de Setores e Órgãos
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
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}