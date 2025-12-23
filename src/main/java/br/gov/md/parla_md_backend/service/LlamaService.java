package br.gov.md.parla_md_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlamaService {

    private final RestTemplate restTemplate;

    @Value("${ollama.api.url:http://ollama:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:llama3.2:3b}")
    private String model;

    @Value("${ollama.timeout:60000}")
    private int timeout;

    @Value("${ollama.temperature:0.7}")
    private double temperature;

    @Cacheable(value = "llm-responses", key = "#prompt.hashCode()")
    public String generate(String prompt) {
        return generate(prompt, null);
    }

    @Cacheable(value = "llm-responses", key = "#prompt.hashCode() + '-' + #systemPrompt.hashCode()")
    public String generate(String prompt, String systemPrompt) {
        log.debug("Gerando resposta LLM para prompt de {} caracteres", prompt.length());

        String url = ollamaUrl + "/api/generate";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "system", systemPrompt != null ? systemPrompt : "",
                "stream", false,
                "options", Map.of(
                        "temperature", temperature,
                        "num_predict", 2048
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("response")) {
                String result = (String) response.get("response");
                log.info("Resposta LLM gerada com sucesso - {} caracteres", result.length());
                return result;
            }

            throw new RuntimeException("Resposta inválida do Ollama");

        } catch (Exception e) {
            log.error("Erro ao chamar Ollama API", e);
            throw new RuntimeException("Erro ao gerar resposta LLM", e);
        }
    }

    public String chat(List<Map<String, String>> messages) {
        log.debug("Gerando resposta LLM via chat com {} mensagens", messages.size());

        String url = ollamaUrl + "/api/chat";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "stream", false,
                "options", Map.of("temperature", temperature)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("message")) {
                Map<String, String> message = (Map<String, String>) response.get("message");
                String result = message.get("content");
                log.info("Resposta LLM chat gerada com sucesso");
                return result;
            }

            throw new RuntimeException("Resposta inválida do Ollama");

        } catch (Exception e) {
            log.error("Erro ao chamar Ollama Chat API", e);
            throw new RuntimeException("Erro ao gerar resposta LLM", e);
        }
    }

    public boolean isModelAvailable() {
        try {
            String url = ollamaUrl + "/api/tags";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null;
        } catch (Exception e) {
            log.error("Erro ao verificar disponibilidade do modelo", e);
            return false;
        }
    }
}
