## 8. Arquivo: /docs/2_etapas_de_implementacao/Etapa01_ConfiguracaoSeguranca.md

```markdown
# Etapa 01: Configuração de Segurança

## Objetivo
Refatorar a configuração de segurança para usar nomenclatura em português, corrigir problemas de segurança e melhorar a configuração para suportar OpenAPI.

## Componentes Afetados
- SecurityConfig.java → ConfiguracaoSeguranca.java

## Tarefas a Realizar
1. Renomear classe para português
2. Limpar configurações comentadas
3. Adicionar suporte para endpoints OpenAPI
4. Atualizar URLs para padrão `/api/publico/`
5. Melhorar configuração de JWT

## Código a Implementar

### Arquivo: ConfiguracaoSeguranca.java
```java
package br.gov.md.parla_md_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracaoSeguranca {

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/publico/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorAutenticacaoJwt()))
                );
        return http.build();
    }

    private JwtAuthenticationConverter conversorAutenticacaoJwt() {
        JwtGrantedAuthoritiesConverter conversorAutoridadesJwt = new JwtGrantedAuthoritiesConverter();
        conversorAutoridadesJwt.setAuthoritiesClaimName("roles");
        conversorAutoridadesJwt.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversorAutenticacao = new JwtAuthenticationConverter();
        conversorAutenticacao.setJwtGrantedAuthoritiesConverter(conversorAutoridadesJwt);
        return conversorAutenticacao;
    }
}
```
## Testes

- Verificar se endpoints /api/publico/** estão acessíveis sem autenticação
- Verificar se endpoints da documentação OpenAPI estão acessíveis
- Verificar se endpoints protegidos requerem autenticação válida
- Verificar se a extração de roles de JWT funciona corretamente

## Dependências

* Nenhuma dependência de outras etapas

## Status

[ ] Análise Concluída
[ ] Implementação Iniciada
[ ] Testes Realizados
[ ] Revisão Concluída
[ ] Implementação Concluída