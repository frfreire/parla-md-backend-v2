package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.domain.Previsao;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.exception.ModeloNaoTreinadoException;
import br.gov.md.parla_md_backend.exception.PrevisaoException;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IItemLegislativoRepository;
import br.gov.md.parla_md_backend.repository.IPrevisaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final LlamaService llamaService;
    private final IPrevisaoRepository previsaoRepository;
    private final IItemLegislativoRepository itemLegislativoRepository;
    private final ObjectMapper objectMapper;

    @Value("${previsao.cache.ttl:86400}")
    private int cacheTtlSegundos;

    @Value("${previsao.modelo.versao:1.0.0}")
    private String modeloVersao;

    @Transactional
    public PrevisaoDTO prever(SolicitarPrevisaoDTO request) {
        long inicioMs = System.currentTimeMillis();

        ItemLegislativo item = buscarItemLegislativo(request.getItemLegislativoId());

        if (!request.isForcarNovaPrevisao()) {
            Previsao previsaoCache = buscarPrevisaoRecente(item);
            if (previsaoCache != null) {
                log.info("Retornando previsão do cache: {}", previsaoCache.getId());
                return PrevisaoDTO.from(previsaoCache);
            }
        }

        try {
            Previsao previsao = gerarNovaPrevisao(item, request.getTipoPrevisaoOrDefault(), inicioMs);
            Previsao salva = previsaoRepository.save(previsao);

            log.info("Previsão gerada: {} - Probabilidade: {}%",
                    salva.getId(),
                    salva.getProbabilidadeAprovacao() * 100);

            return PrevisaoDTO.from(salva);

        } catch (Exception e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(item, request.getTipoPrevisaoOrDefault(), e, duracaoMs);

            log.error("Erro ao gerar previsão: {}", e.getMessage(), e);
            throw PrevisaoException.erroCalculo(e.getMessage(), e);
        }
    }

    @Transactional
    public List<PrevisaoDTO> preverLote(PrevisaoLoteDTO request) {
        log.info("Processando lote de {} previsões", request.getItemLegislativoIds().size());

        List<PrevisaoDTO> previsoes = new ArrayList<>();

        for (String itemId : request.getItemLegislativoIds()) {
            try {
                SolicitarPrevisaoDTO solicitacao = SolicitarPrevisaoDTO.builder()
                        .itemLegislativoId(itemId)
                        .tipoPrevisao(request.getTipoPrevisaoOrDefault())
                        .forcarNovaPrevisao(request.isForcarNovaPrevisao())
                        .build();

                PrevisaoDTO previsao = prever(solicitacao);
                previsoes.add(previsao);

            } catch (Exception e) {
                log.error("Erro ao prever item {}: {}", itemId, e.getMessage());
            }
        }

        log.info("Lote concluído: {} de {} previsões realizadas",
                previsoes.size(),
                request.getItemLegislativoIds().size());

        return previsoes;
    }

    @Cacheable(value = "previsoes", key = "#itemId")
    public PrevisaoDTO buscarPrevisaoPorItem(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);

        Previsao previsao = previsaoRepository
                .findFirstByItemLegislativoOrderByDataPrevisaoDesc(item)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma previsão encontrada para o item: " + itemId));

        return PrevisaoDTO.from(previsao);
    }

    public Page<PrevisaoDTO> buscarPrevisoesRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);

        return previsaoRepository.findByDataPrevisaoAfter(limite, pageable)
                .map(PrevisaoDTO::from);
    }

    public EstatisticasPrevisaoDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<Previsao> previsoes = previsaoRepository.findByDataPrevisaoAfter(inicio);

        if (previsoes.isEmpty()) {
            return estatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDePrevisoes(previsoes, inicio);
    }

    @Transactional
    public void limparExpiradas() {
        LocalDateTime agora = LocalDateTime.now();
        List<Previsao> expiradas = previsaoRepository.buscarExpiradas(agora);

        if (!expiradas.isEmpty()) {
            previsaoRepository.deleteAll(expiradas);
            log.info("Removidas {} previsões expiradas", expiradas.size());
        }
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

    private Previsao buscarPrevisaoRecente(ItemLegislativo item) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return previsaoRepository
                .findByItemLegislativo(item).stream()
                .filter(p -> p.getDataPrevisao().isAfter(limite))
                .filter(p -> p.getSucesso() != null && p.getSucesso())
                .findFirst()
                .orElse(null);
    }

    private Previsao gerarNovaPrevisao(
            ItemLegislativo item,
            String tipoPrevisao,
            long inicioMs) {

        String prompt = construirPrompt(item, tipoPrevisao);
        String promptSistema = construirPromptSistema();

        RespostaLlamaDTO resposta = llamaService.enviarRequisicao(
                prompt,
                promptSistema,
                true
        );

        ResultadoPrevisaoIA resultado = parsearResposta(resposta);

        long duracaoMs = System.currentTimeMillis() - inicioMs;

        return construirPrevisao(item, tipoPrevisao, resultado, resposta, duracaoMs);
    }

    private String construirPrompt(ItemLegislativo item, String tipoPrevisao) {
        return String.format("""
            Analise a seguinte proposição legislativa e preveja a probabilidade de aprovação:
            
            TIPO: %s
            ANO: %d
            EMENTA: %s
            TEMA: %s
            
            Responda APENAS com um JSON no formato:
            {
                "probabilidade": <número entre 0 e 1>,
                "confianca": <número entre 0 e 1>,
                "justificativa": "<texto explicativo>",
                "fatoresPositivos": "<fatores que favorecem aprovação>",
                "fatoresNegativos": "<fatores que dificultam aprovação>"
            }
            """,
                item.getTipo(),
                item.getAno(),
                item.getEmenta(),
                item.getTema()
        );
    }

    private String construirPromptSistema() {
        return """
            Você é um especialista em análise legislativa do Congresso Nacional Brasileiro.
            Sua tarefa é prever a probabilidade de aprovação de proposições com base em:
            - Tema e conteúdo da proposição
            - Contexto político atual
            - Histórico de proposições similares
            - Complexidade e impacto
            
            Seja preciso, objetivo e baseie-se em análise racional.
            Sempre responda em formato JSON válido.
            """;
    }

    private ResultadoPrevisaoIA parsearResposta(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, ResultadoPrevisaoIA.class);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Llama: {}", e.getMessage());
            throw PrevisaoException.erroCalculo("Resposta em formato inválido", e);
        }
    }

    private Previsao construirPrevisao(
            ItemLegislativo item,
            String tipoPrevisao,
            ResultadoPrevisaoIA resultado,
            RespostaLlamaDTO resposta,
            long duracaoMs) {

        return Previsao.builder()
                .itemLegislativo(item)
                .tipoPrevisao(tipoPrevisao)
                .probabilidadeAprovacao(resultado.probabilidade())
                .confianca(resultado.confianca())
                .justificativa(resultado.justificativa())
                .fatoresPositivos(resultado.fatoresPositivos())
                .fatoresNegativos(resultado.fatoresNegativos())
                .dataPrevisao(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(true)
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();
    }

    private void registrarFalha(
            ItemLegislativo item,
            String tipoPrevisao,
            Exception erro,
            long duracaoMs) {

        Previsao previsaoFalha = Previsao.builder()
                .itemLegislativo(item)
                .tipoPrevisao(tipoPrevisao)
                .dataPrevisao(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        previsaoRepository.save(previsaoFalha);
    }

    private EstatisticasPrevisaoDTO estatisticasVazias(LocalDateTime inicio, LocalDateTime fim) {
        return EstatisticasPrevisaoDTO.builder()
                .totalPrevisoes(0L)
                .previsoesComSucesso(0L)
                .previsoesFalhas(0L)
                .taxaSucesso(0.0)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private EstatisticasPrevisaoDTO calcularEstatisticasDePrevisoes(
            List<Previsao> previsoes,
            LocalDateTime inicio) {

        long total = previsoes.size();
        long sucesso = previsoes.stream().filter(p -> p.getSucesso() != null && p.getSucesso()).count();
        long falhas = total - sucesso;

        List<Previsao> sucessos = previsoes.stream()
                .filter(p -> p.getSucesso() != null && p.getSucesso())
                .toList();

        return EstatisticasPrevisaoDTO.builder()
                .totalPrevisoes(total)
                .previsoesComSucesso(sucesso)
                .previsoesFalhas(falhas)
                .taxaSucesso(total > 0 ? (double) sucesso / total : 0.0)
                .probabilidadeMedia(calcularMedia(sucessos, Previsao::getProbabilidadeAprovacao))
                .confiancaMedia(calcularMedia(sucessos, Previsao::getConfianca))
                .tempoMedioMs(calcularMediaLong(previsoes, Previsao::getTempoProcessamentoMs))
                .tempoMinimoMs(calcularMinimo(previsoes, Previsao::getTempoProcessamentoMs))
                .tempoMaximoMs(calcularMaximo(previsoes, Previsao::getTempoProcessamentoMs))
                .periodoInicio(inicio)
                .periodoFim(LocalDateTime.now())
                .previsoesMuitoProvaveis(contarPorClassificacao(sucessos, "MUITO_PROVAVEL"))
                .previsoesProvaveis(contarPorClassificacao(sucessos, "PROVAVEL"))
                .previsoesImprovaveis(contarPorClassificacao(sucessos, "IMPROVAVEL"))
                .previsoesMuitoImprovaveis(contarPorClassificacao(sucessos, "MUITO_IMPROVAVEL"))
                .build();
    }

    private Double calcularMedia(List<Previsao> previsoes, java.util.function.ToDoubleFunction<Previsao> extractor) {
        return previsoes.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(0.0);
    }

    private Long calcularMediaLong(List<Previsao> previsoes, java.util.function.ToLongFunction<Previsao> extractor) {
        return (long) previsoes.stream()
                .mapToLong(extractor)
                .average()
                .orElse(0.0);
    }

    private Long calcularMinimo(List<Previsao> previsoes, java.util.function.ToLongFunction<Previsao> extractor) {
        return previsoes.stream()
                .mapToLong(extractor)
                .min()
                .orElse(0L);
    }

    private Long calcularMaximo(List<Previsao> previsoes, java.util.function.ToLongFunction<Previsao> extractor) {
        return previsoes.stream()
                .mapToLong(extractor)
                .max()
                .orElse(0L);
    }

    private Long contarPorClassificacao(List<Previsao> previsoes, String classificacao) {
        return previsoes.stream()
                .filter(p -> classificacao.equals(p.getClassificacao()))
                .count();
    }

    private record ResultadoPrevisaoIA(
            double probabilidade,
            double confianca,
            String justificativa,
            String fatoresPositivos,
            String fatoresNegativos
    ) {}
}