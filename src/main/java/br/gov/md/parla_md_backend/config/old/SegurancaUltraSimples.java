package br.gov.md.parla_md_backend.config.old;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração de segurança ULTRA SIMPLES para desenvolvimento.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "app.security.oauth2.enabled", havingValue = "false", matchIfMissing = false)
public class SegurancaUltraSimples {

    /**
     * Configuração de segurança sem nenhuma dependência externa.
     */
    @Bean
    public SecurityFilterChain securityFilterChainUltraSimples(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSourceSimples()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // TODOS os endpoints liberados para desenvolvimento
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * Configuração de CORS hardcoded - sem dependências externas.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSourceSimples() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // URLs hardcoded - sem propriedades externas
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:3000", 
            "https://parlamd.gov.br",
            "*"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
