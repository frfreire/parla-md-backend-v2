package br.gov.md.parla_md_backend.service;


import br.gov.md.parla_md_backend.config.OllamaConfig;
import br.gov.md.parla_md_backend.domain.dto.RequisicaoLlamaDTO;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Serviço responsável pela comunicação com o Ollama/Llama
 */
@Slf4j
@Service
public class LlamaService {
    private final RestTemplate restTemplate;
    private final OllamaConfig ollamaConfig;
    private final ObjectMapper objectMapper;

    @Value("${ollama.model:llama3.2:3b}")
    private String modeloPadrao;

    @Value("${ollama.temperature:0.7}")
    private Double temperature;

    public LlamaService(
            @Qualifier("ollamaRestTemplate") RestTemplate restTemplate,
            OllamaConfig ollamaConfig,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.ollamaConfig = ollamaConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Envia requisição para o Llama e retorna resposta estruturada
     */
    public RespostaLlamaDTO enviarRequisicao(String promptUsuario) {
        return enviarRequisicao(promptUsuario, null, false);
    }

    /**
     * Envia requisição com contexto de sistema
     */
    public RespostaLlamaDTO enviarRequisicao(
            String promptUsuario,
            String promptSistema,
            boolean respostaJson) {

        try {
            RequisicaoLlamaDTO requisicao = construirRequisicao(
                    promptUsuario,
                    promptSistema,
                    respostaJson
            );

            log.info("Enviando requisição para Llama: {} caracteres",
                    promptUsuario.length());

            String url = ollamaConfig.getOllamaUrl() + "/api/chat";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RequisicaoLlamaDTO> entity = new HttpEntity<>(requisicao, headers);

            ResponseEntity<RespostaLlamaDTO> response = restTemplate.postForEntity(
                    url,
                    entity,
                    RespostaLlamaDTO.class
            );

            RespostaLlamaDTO resposta = response.getBody();

            if (resposta == null || !resposta.getDone()) {
                throw new IAException("Resposta incompleta do Llama");
            }

            log.info("Resposta recebida do Llama: {} tokens gerados",
                    resposta.getEvalCount());

            return resposta;

        } catch (ResourceAccessException e) {
            log.error("Ollama indisponível: {}", e.getMessage());
            throw new LlamaIndisponivelException(
                    "Serviço Llama está indisponível. Tente novamente em instantes.",
                    e
            );
        } catch (Exception e) {
            log.error("Erro ao comunicar com Llama", e);
            throw new IAException("Erro ao processar requisição de IA: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai resposta JSON do conteúdo retornado pelo Llama
     */
    public <T> T extrairJson(RespostaLlamaDTO resposta, Class<T> classe) {
        try {
            String conteudo = resposta.getMessage().getContent();

            conteudo = limparMarkdown(conteudo);

            return objectMapper.readValue(conteudo, classe);

        } catch (JsonProcessingException e) {
            log.error("Erro ao parsear JSON da resposta: {}",
                    resposta.getMessage().getContent(), e);
            throw new IAException("Resposta do LLM em formato inválido", e);
        }
    }

    /**
     * Verifica se o Ollama está disponível
     */
    public boolean verificarDisponibilidade() {
        try {
            String url = ollamaConfig.getOllamaUrl() + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Ollama não está disponível: {}", e.getMessage());
            return false;
        }
    }

    private RequisicaoLlamaDTO construirRequisicao(
            String promptUsuario,
            String promptSistema,
            boolean respostaJson) {

        RequisicaoLlamaDTO.RequisicaoLlamaDTOBuilder builder = RequisicaoLlamaDTO.builder()
                .model(modeloPadrao)
                .stream(false)
                .options(RequisicaoLlamaDTO.Options.builder()
                        .temperature(temperature)
                        .topP(0.9)
                        .topK(40)
                        .numPredict(2048)
                        .build());

        List<RequisicaoLlamaDTO.Mensagem> mensagens = new java.util.ArrayList<>();

        if (promptSistema != null && !promptSistema.isBlank()) {
            mensagens.add(RequisicaoLlamaDTO.Mensagem.builder()
                    .role("system")
                    .content(promptSistema)
                    .build());
        }

        mensagens.add(RequisicaoLlamaDTO.Mensagem.builder()
                .role("user")
                .content(promptUsuario)
                .build());

        builder.messages(mensagens);

        if (respostaJson) {
            builder.format("json");
        }

        return builder.build();
    }

    private String limparMarkdown(String conteudo) {
        return conteudo
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
    }
}