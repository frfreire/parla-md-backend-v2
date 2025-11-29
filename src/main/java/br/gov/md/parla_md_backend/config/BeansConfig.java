package br.gov.md.parla_md_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuração de beans globais necessários para o funcionamento do sistema Parla-MD.
 * 
 * <p>Esta configuração centraliza a criação de beans comuns que são utilizados
 * em diferentes partes da aplicação, garantindo configurações consistentes
 * e otimizadas para o ambiente de produção.</p>
 * 
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
@Configuration
public class BeansConfig {

    /**
     * Configura um RestTemplate otimizado para comunicação HTTP.
     * 
     * <p>Este RestTemplate é usado principalmente para comunicação com o Keycloak
     * e outras APIs externas. Inclui configurações de timeout otimizadas para
     * evitar travamentos em caso de problemas de conectividade.</p>
     * 
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Timeouts otimizados para ambiente corporativo
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        
        // Configurar buffer para requisições grandes
        factory.setBufferRequestBody(false);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Adicionar interceptors se necessário (logging, autenticação, etc.)
        // restTemplate.setInterceptors(Arrays.asList(new LoggingInterceptor()));
        
        return restTemplate;
    }
}