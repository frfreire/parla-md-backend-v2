package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.config.OllamaConfig;
import br.gov.md.parla_md_backend.domain.dto.RequisicaoLlamaDTO;
import br.gov.md.parla_md_backend.domain.dto.RespostaLlamaDTO;
import br.gov.md.parla_md_backend.domain.InteracaoLlama;
import br.gov.md.parla_md_backend.exception.IAException;
import br.gov.md.parla_md_backend.exception.LlamaIndisponivelException;
import br.gov.md.parla_md_backend.exception.LlamaTimeoutException;
import br.gov.md.parla_md_backend.repository.IInteracaoLlamaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlamaService {

    private final RestTemplate restTemplate;
    private final OllamaConfig ollamaConfig;
    private final ObjectMapper objectMapper;
    private final IInteracaoLlamaRepository interacaoRepository;
    private final MeterRegistry meterRegistry;

    @Value("${ollama.model:llama3.2:3b}")
    private String modeloPadrao;

    @Value("${ollama.temperature:0.7}")
    private Double temperaturePadrao;

    @Value("${cache.llm.ttl:3600}")
    private int cacheTtlSegundos;

    public RespostaLlamaDTO enviarRequisicao(String promptUsuario) {
        return enviarRequisicao(promptUsuario, null, false);
    }

    @Retryable(
            value = {ResourceAccessException.class, RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public RespostaLlamaDTO enviarRequisicao(
            String promptUsuario,
            String promptSistema,
            boolean respostaJson) {

        long inicioMs = System.currentTimeMillis();

        try {
            validarPrompt(promptUsuario);

            RequisicaoLlamaDTO requisicao = construirRequisicao(
                    promptUsuario,
                    promptSistema,
                    respostaJson
            );

            log.info("Enviando requisição Llama: {} caracteres", promptUsuario.length());

            RespostaLlamaDTO resposta = executarRequisicao(requisicao);

            validarResposta(resposta);

            long duracaoMs = System.currentTimeMillis() - inicioMs;

            registrarSucesso(requisicao, resposta, duracaoMs);
            registrarMetrica("llama.requisicao.sucesso", duracaoMs);

            log.info("Resposta Llama recebida: {} tokens", resposta.getEvalCount());

            return resposta;

        } catch (ResourceAccessException e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(promptUsuario, promptSistema, e.getMessage(), duracaoMs);
            registrarMetrica("llama.requisicao.indisponivel", duracaoMs);

            log.error("Ollama indisponível: {}", e.getMessage());
            throw LlamaIndisponivelException.erroConexao(e);

        } catch (RestClientException e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(promptUsuario, promptSistema, e.getMessage(), duracaoMs);
            registrarMetrica("llama.requisicao.erro", duracaoMs);

            log.error("Erro ao comunicar com Llama", e);
            throw IAException.processingError(e.getMessage(), e);
        }
    }

    public <T> T extrairJson(RespostaLlamaDTO resposta, Class<T> classe) {
        try {
            String conteudo = extrairConteudo(resposta);
            String conteudoLimpo = limparMarkdown(conteudo);

            return objectMapper.readValue(conteudoLimpo, classe);

        } catch (JsonProcessingException e) {
            log.error("Erro ao parsear JSON: {}", resposta.getMessage().getContent(), e);
            throw IAException.respostaInvalida("JSON malformado");
        }
    }

    @Cacheable(value = "llama-disponibilidade", key = "'status'")
    public boolean verificarDisponibilidade() {
        try {
            String url = ollamaConfig.getOllamaUrl() + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            boolean disponivel = response.getStatusCode().is2xxSuccessful();

            registrarMetrica(
                    disponivel ? "llama.disponibilidade.online" : "llama.disponibilidade.offline",
                    1
            );

            return disponivel;

        } catch (Exception e) {
            log.warn("Ollama indisponível: {}", e.getMessage());
            registrarMetrica("llama.disponibilidade.erro", 1);
            return false;
        }
    }

    public List<InteracaoLlama> buscarHistorico(String usuarioId) {
        return interacaoRepository.findByUsuarioId(usuarioId);
    }

    public List<InteracaoLlama> buscarFalhasRecentes(int ultimasHoras) {
        LocalDateTime limite = LocalDateTime.now().minusHours(ultimasHoras);
        return interacaoRepository.buscarFalhasRecentes(limite);
    }

    @Transactional
    public void limparExpirados() {
        LocalDateTime agora = LocalDateTime.now();
        List<InteracaoLlama> expirados = interacaoRepository.buscarExpiradas(agora);

        if (!expirados.isEmpty()) {
            interacaoRepository.deleteAll(expirados);
            log.info("Removidas {} interações expiradas", expirados.size());
        }
    }

    private void validarPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt não pode ser vazio");
        }

        if (prompt.length() > 100000) {
            throw new IllegalArgumentException("Prompt excede limite de 100.000 caracteres");
        }
    }

    private RespostaLlamaDTO executarRequisicao(RequisicaoLlamaDTO requisicao) {
        String url = ollamaConfig.getOllamaUrl() + "/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RequisicaoLlamaDTO> entity = new HttpEntity<>(requisicao, headers);

        ResponseEntity<RespostaLlamaDTO> response = restTemplate.postForEntity(
                url,
                entity,
                RespostaLlamaDTO.class
        );

        return response.getBody();
    }

    private void validarResposta(RespostaLlamaDTO resposta) {
        if (resposta == null || resposta.getDone() == null || !resposta.getDone()) {
            throw IAException.respostaIncompleta();
        }

        if (resposta.getMessage() == null || resposta.getMessage().getContent() == null) {
            throw IAException.respostaInvalida("Conteúdo vazio");
        }
    }

    private RequisicaoLlamaDTO construirRequisicao(
            String promptUsuario,
            String promptSistema,
            boolean respostaJson) {

        RequisicaoLlamaDTO.RequisicaoLlamaDTOBuilder builder = RequisicaoLlamaDTO.builder()
                .model(modeloPadrao)
                .stream(false)
                .options(construirOpcoes());

        builder.messages(construirMensagens(promptUsuario, promptSistema));

        if (respostaJson) {
            builder.format("json");
        }

        return builder.build();
    }

    private RequisicaoLlamaDTO.Options construirOpcoes() {
        return RequisicaoLlamaDTO.Options.builder()
                .temperature(temperaturePadrao)
                .topP(0.9)
                .topK(40)
                .numPredict(2048)
                .build();
    }

    private List<RequisicaoLlamaDTO.Mensagem> construirMensagens(
            String promptUsuario,
            String promptSistema) {

        List<RequisicaoLlamaDTO.Mensagem> mensagens = new ArrayList<>();

        if (promptSistema != null && !promptSistema.isBlank()) {
            mensagens.add(construirMensagem("system", promptSistema));
        }

        mensagens.add(construirMensagem("user", promptUsuario));

        return mensagens;
    }

    private RequisicaoLlamaDTO.Mensagem construirMensagem(String role, String content) {
        return RequisicaoLlamaDTO.Mensagem.builder()
                .role(role)
                .content(content)
                .build();
    }

    private String extrairConteudo(RespostaLlamaDTO resposta) {
        return resposta.getMessage().getContent();
    }

    private String limparMarkdown(String conteudo) {
        return conteudo
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
    }

    private void registrarSucesso(
            RequisicaoLlamaDTO requisicao,
            RespostaLlamaDTO resposta,
            long duracaoMs) {

        InteracaoLlama interacao = InteracaoLlama.builder()
                .modelo(requisicao.getModel())
                .promptUsuario(extrairPromptUsuario(requisicao))
                .promptSistema(extrairPromptSistema(requisicao))
                .respostaConteudo(resposta.getMessage().getContent())
                .respostaJson(requisicao.getFormat() != null)
                .sucesso(true)
                .dataHoraRequisicao(LocalDateTime.now())
                .duracaoTotalMs(resposta.getTotalDuration() != null ?
                        resposta.getTotalDuration() / 1_000_000 : duracaoMs)
                .duracaoCarregamentoMs(resposta.getLoadDuration() != null ?
                        resposta.getLoadDuration() / 1_000_000 : null)
                .tokensPrompt(resposta.getPromptEvalCount())
                .tokensResposta(resposta.getEvalCount())
                .temperature(requisicao.getOptions().getTemperature())
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        interacaoRepository.save(interacao);
    }

    private void registrarFalha(
            String promptUsuario,
            String promptSistema,
            String mensagemErro,
            long duracaoMs) {

        InteracaoLlama interacao = InteracaoLlama.builder()
                .modelo(modeloPadrao)
                .promptUsuario(promptUsuario)
                .promptSistema(promptSistema)
                .sucesso(false)
                .mensagemErro(mensagemErro)
                .dataHoraRequisicao(LocalDateTime.now())
                .duracaoTotalMs(duracaoMs)
                .temperature(temperaturePadrao)
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        interacaoRepository.save(interacao);
    }

    private void registrarMetrica(String nomeMetrica, long valor) {
        meterRegistry.counter(nomeMetrica).increment(valor);
    }

    private String extrairPromptUsuario(RequisicaoLlamaDTO requisicao) {
        return requisicao.getMessages().stream()
                .filter(m -> "user".equals(m.getRole()))
                .findFirst()
                .map(RequisicaoLlamaDTO.Mensagem::getContent)
                .orElse(null);
    }

    private String extrairPromptSistema(RequisicaoLlamaDTO requisicao) {
        return requisicao.getMessages().stream()
                .filter(m -> "system".equals(m.getRole()))
                .findFirst()
                .map(RequisicaoLlamaDTO.Mensagem::getContent)
                .orElse(null);
    }
}