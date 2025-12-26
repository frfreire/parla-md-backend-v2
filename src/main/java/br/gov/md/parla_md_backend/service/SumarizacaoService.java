package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.domain.Sumario;
import br.gov.md.parla_md_backend.domain.ItemLegislativo;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.exception.SumarizacaoException;
import br.gov.md.parla_md_backend.repository.IItemLegislativoRepository;
import br.gov.md.parla_md_backend.repository.ISumarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SumarizacaoService {

    private final LlamaService llamaService;
    private final ISumarioRepository sumarioRepository;
    private final IItemLegislativoRepository itemLegislativoRepository;

    @Value("${sumario.cache.ttl:86400}")
    private int cacheTtlSegundos;

    @Value("${sumario.modelo.versao:1.0.0}")
    private String modeloVersao;

    @Value("${sumario.texto.minimo:100}")
    private int textoMinimoCaracteres;

    @Transactional
    public SumarioDTO sumarizar(SolicitarSumarioDTO request) {
        long inicioMs = System.currentTimeMillis();

        ItemLegislativo item = buscarItemLegislativo(request.getItemLegislativoId());

        String textoParaSumarizar = determinarTexto(request, item);
        validarTexto(textoParaSumarizar);

        if (!request.isForcarNovoSumario()) {
            Sumario sumarioCache = buscarSumarioRecente(item);
            if (sumarioCache != null) {
                log.info("Retornando sumário do cache: {}", sumarioCache.getId());
                return SumarioDTO.from(sumarioCache);
            }
        }

        try {
            Sumario sumario = gerarNovoSumario(
                    item,
                    textoParaSumarizar,
                    request,
                    inicioMs
            );

            Sumario salvo = sumarioRepository.save(sumario);

            log.info("Sumário gerado: {} - Compressão: {}%",
                    salvo.getId(),
                    salvo.getTaxaCompressao() * 100);

            return SumarioDTO.from(salvo);

        } catch (Exception e) {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarFalha(item, request.getTipoSumarioOrDefault(), e, duracaoMs);

            log.error("Erro ao gerar sumário: {}", e.getMessage(), e);
            throw SumarizacaoException.erroProcessamento(e.getMessage(), e);
        }
    }

    @Cacheable(value = "sumarios", key = "#itemId")
    public SumarioDTO buscarSumarioPorItem(String itemId) {
        ItemLegislativo item = buscarItemLegislativo(itemId);

        Sumario sumario = sumarioRepository
                .findFirstByItemLegislativoOrderByDataCriacaoDesc(item)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum sumário encontrado para o item: " + itemId));

        return SumarioDTO.from(sumario);
    }

    public Page<SumarioDTO> buscarSumariosRecentes(Pageable pageable) {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);

        return sumarioRepository.findByDataCriacaoAfter(limite, pageable)
                .map(SumarioDTO::from);
    }

    public EstatisticasSumarioDTO calcularEstatisticas(int dias) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(dias);
        List<Sumario> sumarios = sumarioRepository.findByDataCriacaoAfter(inicio);

        if (sumarios.isEmpty()) {
            return estatisticasVazias(inicio, LocalDateTime.now());
        }

        return calcularEstatisticasDeSumarios(sumarios, inicio);
    }

    @Transactional
    public void limparExpirados() {
        LocalDateTime agora = LocalDateTime.now();
        List<Sumario> expirados = sumarioRepository.buscarExpirados(agora);

        if (!expirados.isEmpty()) {
            sumarioRepository.deleteAll(expirados);
            log.info("Removidos {} sumários expirados", expirados.size());
        }
    }

    private ItemLegislativo buscarItemLegislativo(String itemId) {
        return itemLegislativoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item legislativo não encontrado: " + itemId));
    }

    private String determinarTexto(SolicitarSumarioDTO request, ItemLegislativo item) {
        if (request.getTextoCustomizado() != null && !request.getTextoCustomizado().isBlank()) {
            return request.getTextoCustomizado();
        }

        return construirTextoCompleto(item);
    }

    private String construirTextoCompleto(ItemLegislativo item) {
        StringBuilder texto = new StringBuilder();

        if (item.getEmenta() != null) {
            texto.append("EMENTA: ").append(item.getEmenta()).append("\n\n");
        }

        if (item.getEmentaDetalhada() != null) {
            texto.append(item.getEmentaDetalhada());
        }

        return texto.toString();
    }

    private void validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw SumarizacaoException.textoVazio();
        }

        if (texto.length() < textoMinimoCaracteres) {
            throw SumarizacaoException.textoMuitoCurto(textoMinimoCaracteres);
        }
    }

    private Sumario buscarSumarioRecente(ItemLegislativo item) {
        LocalDateTime limite = LocalDateTime.now().minusSeconds(cacheTtlSegundos);

        return sumarioRepository
                .findByItemLegislativo(item).stream()
                .filter(s -> s.getDataCriacao().isAfter(limite))
                .filter(s -> s.getSucesso() != null && s.getSucesso())
                .findFirst()
                .orElse(null);
    }

    private Sumario gerarNovoSumario(
            ItemLegislativo item,
            String texto,
            SolicitarSumarioDTO request,
            long inicioMs) {

        String prompt = construirPrompt(texto, request);
        String promptSistema = construirPromptSistema();

        RespostaLlamaDTO resposta = llamaService.enviarRequisicao(
                prompt,
                promptSistema,
                true
        );

        ResultadoSumarizacaoIA resultado = parsearResposta(resposta);

        long duracaoMs = System.currentTimeMillis() - inicioMs;

        return construirSumario(item, texto, request, resultado, resposta, duracaoMs);
    }

    private String construirPrompt(String texto, SolicitarSumarioDTO request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Crie um sumário executivo do seguinte texto legislativo:\n\n");
        prompt.append(texto);
        prompt.append("\n\nResponda APENAS com um JSON no formato:\n");
        prompt.append("{\n");
        prompt.append("  \"sumarioExecutivo\": \"<sumário em 3-5 frases>\",\n");
        prompt.append("  \"pontosPrincipais\": [\"<ponto 1>\", \"<ponto 2>\", \"<ponto 3>\"],\n");

        if (request.isIncluirPalavrasChave()) {
            prompt.append("  \"palavrasChave\": [\"<palavra 1>\", \"<palavra 2>\"],\n");
        }

        if (request.isIncluirEntidades()) {
            prompt.append("  \"entidadesRelevantes\": [\"<entidade 1>\", \"<entidade 2>\"],\n");
        }

        if (request.isIncluirSentimento()) {
            prompt.append("  \"sentimentoGeral\": \"<positivo|neutro|negativo>\",\n");
        }

        prompt.append("  \"temasPrincipais\": \"<tema principal em 1 frase>\",\n");
        prompt.append("  \"impactoEstimado\": \"<impacto esperado em 1 frase>\"\n");
        prompt.append("}");

        return prompt.toString();
    }

    private String construirPromptSistema() {
        return """
            Você é um especialista em sumarização de textos legislativos brasileiros.
            Sua tarefa é criar sumários claros, objetivos e informativos.
            
            Diretrizes:
            - Use linguagem acessível mas técnica
            - Mantenha precisão jurídica
            - Destaque pontos mais relevantes
            - Seja conciso sem perder informação essencial
            
            Sempre responda em formato JSON válido.
            """;
    }

    private ResultadoSumarizacaoIA parsearResposta(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, ResultadoSumarizacaoIA.class);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Llama: {}", e.getMessage());
            throw SumarizacaoException.erroProcessamento("Resposta em formato inválido", e);
        }
    }

    private Sumario construirSumario(
            ItemLegislativo item,
            String textoOriginal,
            SolicitarSumarioDTO request,
            ResultadoSumarizacaoIA resultado,
            RespostaLlamaDTO resposta,
            long duracaoMs) {

        int tamanhoOriginal = textoOriginal.length();
        int tamanhoSumario = resultado.sumarioExecutivo().length();
        double taxaCompressao = (double) tamanhoSumario / tamanhoOriginal;

        return Sumario.builder()
                .itemLegislativo(item)
                .tipoSumario(request.getTipoSumarioOrDefault())
                .sumarioExecutivo(resultado.sumarioExecutivo())
                .pontosPrincipais(resultado.pontosPrincipais())
                .palavrasChave(resultado.palavrasChave())
                .entidadesRelevantes(resultado.entidadesRelevantes())
                .temasPrincipais(resultado.temasPrincipais())
                .sentimentoGeral(resultado.sentimentoGeral())
                .impactoEstimado(resultado.impactoEstimado())
                .dataCriacao(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .promptUtilizado(resposta.getMessage().getContent())
                .respostaCompleta(resposta.getMessage().getContent())
                .tempoProcessamentoMs(duracaoMs)
                .tamanhoTextoOriginal(tamanhoOriginal)
                .tamanhoSumario(tamanhoSumario)
                .taxaCompressao(taxaCompressao)
                .sucesso(true)
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();
    }

    private void registrarFalha(
            ItemLegislativo item,
            String tipoSumario,
            Exception erro,
            long duracaoMs) {

        Sumario sumarioFalha = Sumario.builder()
                .itemLegislativo(item)
                .tipoSumario(tipoSumario)
                .dataCriacao(LocalDateTime.now())
                .modeloVersao(modeloVersao)
                .tempoProcessamentoMs(duracaoMs)
                .sucesso(false)
                .mensagemErro(erro.getMessage())
                .dataExpiracao(LocalDateTime.now().plusSeconds(cacheTtlSegundos))
                .build();

        sumarioRepository.save(sumarioFalha);
    }

    private EstatisticasSumarioDTO estatisticasVazias(LocalDateTime inicio, LocalDateTime fim) {
        return EstatisticasSumarioDTO.builder()
                .totalSumarios(0L)
                .sumariosComSucesso(0L)
                .sumariosFalhas(0L)
                .taxaSucesso(0.0)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .build();
    }

    private EstatisticasSumarioDTO calcularEstatisticasDeSumarios(
            List<Sumario> sumarios,
            LocalDateTime inicio) {

        long total = sumarios.size();
        long sucesso = sumarios.stream()
                .filter(s -> s.getSucesso() != null && s.getSucesso())
                .count();
        long falhas = total - sucesso;

        List<Sumario> sucessos = sumarios.stream()
                .filter(s -> s.getSucesso() != null && s.getSucesso())
                .toList();

        return EstatisticasSumarioDTO.builder()
                .totalSumarios(total)
                .sumariosComSucesso(sucesso)
                .sumariosFalhas(falhas)
                .taxaSucesso(total > 0 ? (double) sucesso / total : 0.0)
                .taxaCompressaoMedia(calcularMedia(sucessos, Sumario::getTaxaCompressao))
                .taxaCompressaoMinima(calcularMinimo(sucessos, Sumario::getTaxaCompressao))
                .taxaCompressaoMaxima(calcularMaximo(sucessos, Sumario::getTaxaCompressao))
                .tempoMedioMs(calcularMediaLong(sumarios, Sumario::getTempoProcessamentoMs))
                .tempoMinimoMs(calcularMinimoLong(sumarios, Sumario::getTempoProcessamentoMs))
                .tempoMaximoMs(calcularMaximoLong(sumarios, Sumario::getTempoProcessamentoMs))
                .tamanhoMedioOriginal(calcularMediaInt(sucessos, Sumario::getTamanhoTextoOriginal))
                .tamanhoMedioSumario(calcularMediaInt(sucessos, Sumario::getTamanhoSumario))
                .periodoInicio(inicio)
                .periodoFim(LocalDateTime.now())
                .build();
    }

    private Double calcularMedia(List<Sumario> sumarios, java.util.function.ToDoubleFunction<Sumario> extractor) {
        return sumarios.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(0.0);
    }

    private Double calcularMinimo(List<Sumario> sumarios, java.util.function.ToDoubleFunction<Sumario> extractor) {
        return sumarios.stream()
                .mapToDouble(extractor)
                .min()
                .orElse(0.0);
    }

    private Double calcularMaximo(List<Sumario> sumarios, java.util.function.ToDoubleFunction<Sumario> extractor) {
        return sumarios.stream()
                .mapToDouble(extractor)
                .max()
                .orElse(0.0);
    }

    private Long calcularMediaLong(List<Sumario> sumarios, java.util.function.ToLongFunction<Sumario> extractor) {
        return (long) sumarios.stream()
                .mapToLong(extractor)
                .average()
                .orElse(0.0);
    }

    private Long calcularMinimoLong(List<Sumario> sumarios, java.util.function.ToLongFunction<Sumario> extractor) {
        return sumarios.stream()
                .mapToLong(extractor)
                .min()
                .orElse(0L);
    }

    private Long calcularMaximoLong(List<Sumario> sumarios, java.util.function.ToLongFunction<Sumario> extractor) {
        return sumarios.stream()
                .mapToLong(extractor)
                .max()
                .orElse(0L);
    }

    private Integer calcularMediaInt(List<Sumario> sumarios, java.util.function.ToIntFunction<Sumario> extractor) {
        return (int) sumarios.stream()
                .mapToInt(extractor)
                .average()
                .orElse(0.0);
    }

    private record ResultadoSumarizacaoIA(
            String sumarioExecutivo,
            List<String> pontosPrincipais,
            List<String> palavrasChave,
            List<String> entidadesRelevantes,
            String temasPrincipais,
            String sentimentoGeral,
            String impactoEstimado
    ) {}
}