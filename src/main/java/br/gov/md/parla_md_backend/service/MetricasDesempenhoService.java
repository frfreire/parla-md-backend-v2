package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.domain.HistoricoMetricas;
import br.gov.md.parla_md_backend.domain.MetricaDashboard;
import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.exception.MetricasException;
import br.gov.md.parla_md_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.DoubleSummaryStatistics;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricasDesempenhoService {

    private final LlamaService llamaService;
    private final TendenciasIAService tendenciasIAService;
    private final IMetricaDashboardRepository metricaRepository;
    private final IHistoricoMetricasRepository historicoRepository;
    private final IProposicaoRepository proposicaoRepository;
    private final IMateriaRepository materiaRepository;
    private final IParecerRepository parecerRepository;
    private final IPosicionamentoRepository posicionamentoRepository;
    private final IPrevisaoRepository previsaoRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${metricas.cache.ttl:3600}")
    private int cacheTtlSegundos;

    @Value("${metricas.historico.retencao:90}")
    private int diasRetencaoHistorico;

    @Cacheable(value = "dashboard-metricas", key = "'atual'")
    public DashboardMetricasDTO obterMetricasAtuais() {
        log.debug("Obtendo métricas atuais do dashboard");

        MetricaDashboard metrica = metricaRepository
                .findFirstByTipoMetricaOrderByDataCalculoDesc("GERAL")
                .orElseGet(this::calcularMetricasCompletas);

        if (metrica.isPrecisaAtualizar()) {
            metrica = calcularMetricasCompletas();
        }

        return DashboardMetricasDTO.from(metrica);
    }

    @CacheEvict(value = "dashboard-metricas", allEntries = true)
    @Transactional
    public DashboardMetricasDTO recalcularMetricas() {
        log.info("Forçando recálculo de métricas do dashboard");

        MetricaDashboard metrica = calcularMetricasCompletas();

        registrarHistorico(metrica);

        return DashboardMetricasDTO.from(metrica);
    }

    private long countByStatusEmElaboracao() {
        return parecerRepository.countByStatus(StatusParecer.EM_ELABORACAO);
    }

    private long countByStatusSolicitado() {
        return posicionamentoRepository.countByStatus(StatusPosicionamento.SOLICITADO);
    }

    public KPIsDTO obterKPIs() {
        log.debug("Calculando KPIs do sistema");

        LocalDate agora = LocalDate.now();
        LocalDate inicioHoje = LocalDate.from(agora.atStartOfDay());
        LocalDate inicioSemana = agora.minusWeeks(1);
        LocalDate inicioMes = agora.minusMonths(1);

        long documentosNovosHoje = contarDocumentosNovos(inicioHoje, agora);
        long documentosNovosEstaSemana = contarDocumentosNovos(inicioSemana, agora);
        long documentosNovosEsteMes = contarDocumentosNovos(inicioMes, agora);

        long totalAtivos = proposicaoRepository.count() + materiaRepository.count();

        double crescimentoSemanal = calcularTaxaCrescimento(
                inicioSemana, inicioSemana, inicioSemana, agora);
        double crescimentoMensal = calcularTaxaCrescimento(
                inicioMes, inicioMes, inicioMes, agora);

        return KPIsDTO.builder()
                .totalDocumentosAtivos(totalAtivos)
                .documentosNovosHoje(documentosNovosHoje)
                .documentosNovosEstaSemana(documentosNovosEstaSemana)
                .documentosNovosEsteMes(documentosNovosEsteMes)
                .taxaCrescimentoSemanal(crescimentoSemanal)
                .taxaCrescimentoMensal(crescimentoMensal)
                .documentosPendentesTriagem(contarPendentesTriagem())
                .pareceresPendentes(countByStatusEmElaboracao())
                .posicionamentosPendentes(countByStatusSolicitado())
                .tempoMedioTramitacao(calcularTempoMedioTramitacao())
                .documentosComPrazoVencido(contarPrazosVencidos())
                .alertasCriticos(contarAlertasCriticos())
                .eficienciaProcessamento(calcularEficiencia())
                .statusGeral(determinarStatusGeral())
                .build();
    }

    public SerieTemporalDTO obterSerieTemporal(
            String metrica,
            LocalDateTime inicio,
            LocalDateTime fim) {

        log.debug("Obtendo série temporal: metrica={}, inicio={}, fim={}",
                metrica, inicio, fim);

        validarPeriodo(inicio, fim);

        List<HistoricoMetricas> historicos = historicoRepository
                .buscarSerieTemporalTemporal("GERAL", metrica, inicio);

        List<SerieTemporalDTO.PontoTemporalDTO> pontos = historicos.stream()
                .filter(h -> h.getDataRegistro().isBefore(fim))
                .map(h -> SerieTemporalDTO.PontoTemporalDTO.builder()
                        .data(h.getDataRegistro())
                        .valor(h.getValor())
                        .categoria(h.getCategoria())
                        .build())
                .sorted(Comparator.comparing(SerieTemporalDTO.PontoTemporalDTO::getData))
                .toList();

        DoubleSummaryStatistics stats = pontos.stream()
                .mapToDouble(SerieTemporalDTO.PontoTemporalDTO::getValor)
                .summaryStatistics();

        return SerieTemporalDTO.builder()
                .metrica(metrica)
                .tipo("GERAL")
                .periodoInicio(inicio)
                .periodoFim(fim)
                .pontos(pontos)
                .valorMinimo(stats.getMin())
                .valorMaximo(stats.getMax())
                .valorMedio(stats.getAverage())
                .tendencia(analisarTendencia(pontos))
                .build();
    }

    public TendenciasDTO analisarTendenciasComIA() {
        log.info("Analisando tendências com IA");

        try {
            Map<String, Object> dadosContexto = coletarDadosParaTendencias();

            TendenciasIADTO resultado = tendenciasIAService.analisarTendencias(dadosContexto);

            return TendenciasDTO.builder()
                    .temasEmAlta(resultado.temasEmergentes())
                    .analiseIA(resultado.analiseGeral())
                    .alertas(resultado.alertas())
                    .previsaoProximoMes(resultado.previsaoProximoMes())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao analisar tendências com IA: {}", e.getMessage(), e);
            return construirTendenciasFallback();
        }
    }

    @Scheduled(cron = "${metricas.atualizacao.cron:0 0 * * * *}")
    @CacheEvict(value = "dashboard-metricas", allEntries = true)
    @Transactional
    public void atualizarMetricasAutomaticamente() {
        log.info("Iniciando atualização automática de métricas");

        try {
            MetricaDashboard metrica = calcularMetricasCompletas();
            registrarHistorico(metrica);

            log.info("Métricas atualizadas com sucesso");

        } catch (Exception e) {
            log.error("Erro ao atualizar métricas automaticamente: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${metricas.limpeza.cron:0 0 2 * * *}")
    @Transactional
    public void limparHistoricoAntigo() {
        log.info("Iniciando limpeza de histórico antigo");

        LocalDateTime dataLimite = LocalDateTime.now().minusDays(diasRetencaoHistorico);

        metricaRepository.deleteByDataCalculoBefore(dataLimite);
        historicoRepository.deleteByDataRegistroBefore(dataLimite);

        log.info("Histórico anterior a {} removido", dataLimite);
    }

    private MetricaDashboard calcularMetricasCompletas() {
        log.debug("Calculando métricas completas do sistema");

        LocalDate agora = LocalDate.now();
        LocalDate umaSemanaAtras = agora.minusWeeks(1);
        LocalDate umMesAtras = agora.minusMonths(1);

        long totalProposicoes = proposicaoRepository.count();
        long totalMaterias = materiaRepository.count();
        long totalDocumentos = totalProposicoes + totalMaterias;

        long docsUltimaSemana = contarDocumentosNovos(umaSemanaAtras, agora);
        long docsUltimoMes = contarDocumentosNovos(umMesAtras, agora);

        Map<String, Long> porTipo = calcularDistribuicaoPorTipo();
        Map<String, Long> porPartido = calcularDistribuicaoPorPartido();
        Map<String, Long> porEstado = calcularDistribuicaoPorEstado();
        Map<String, Long> porTema = calcularDistribuicaoPorTema();
        Map<String, Long> porStatus = calcularDistribuicaoPorStatus();

        DoubleSummaryStatistics statsAprovacao = calcularEstatisticasAprovacao();

        Map<String, Object> kpis = calcularKPIsMap();
        Map<String, Object> tendencias = calcularTendenciasMap();

        MetricaDashboard metrica = MetricaDashboard.builder()
                .dataCalculo(agora)
                .tipoMetrica("GERAL")
                .periodo("ATUAL")
                .totalProposicoes(totalProposicoes)
                .totalMaterias(totalMaterias)
                .totalDocumentos(totalDocumentos)
                .documentosUltimaSemana(docsUltimaSemana)
                .documentosUltimoMes(docsUltimoMes)
                .porTipo(porTipo)
                .porPartido(porPartido)
                .porEstado(porEstado)
                .porTema(porTema)
                .porStatus(porStatus)
                .probabilidadeAprovacaoMedia(statsAprovacao.getAverage())
                .documentosAprovados(contarAprovados())
                .documentosRejeitados(contarRejeitados())
                .documentosEmTramitacao(contarEmTramitacao())
                .taxaAprovacao(calcularTaxaAprovacao())
                .taxaRejeicao(calcularTaxaRejeicao())
                .kpis(kpis)
                .tendencias(tendencias)
                .proximaAtualizacao(agora.plusWeeks(cacheTtlSegundos))
                .build();

        return metricaRepository.save(metrica);
    }

    private Map<String, Long> calcularDistribuicaoPorTipo() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("tipo").count().as("total"),
                Aggregation.project("total").and("tipo").previousOperation()
        );

        return executarAgregacao(aggregation, "proposicoes");
    }

    private Map<String, Long> calcularDistribuicaoPorPartido() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("partidoAutor").count().as("total"),
                Aggregation.sort(Sort.Direction.DESC, "total"),
                Aggregation.limit(10)
        );

        return executarAgregacao(aggregation, "proposicoes");
    }

    private Map<String, Long> calcularDistribuicaoPorEstado() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("estadoAutor").count().as("total"),
                Aggregation.sort(Sort.Direction.DESC, "total"),
                Aggregation.limit(10)
        );

        return executarAgregacao(aggregation, "proposicoes");
    }

    private Map<String, Long> calcularDistribuicaoPorTema() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("tema").count().as("total"),
                Aggregation.sort(Sort.Direction.DESC, "total"),
                Aggregation.limit(10)
        );

        return executarAgregacao(aggregation, "proposicoes");
    }

    private Map<String, Long> calcularDistribuicaoPorStatus() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("triagemStatus").count().as("total")
        );

        return executarAgregacao(aggregation, "proposicoes");
    }

    private Map<String, Long> executarAgregacao(Aggregation aggregation, String collection) {
        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation, collection, Map.class);

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        map -> String.valueOf(map.get("_id")),
                        map -> ((Number) map.get("total")).longValue()
                ));
    }

    private DoubleSummaryStatistics calcularEstatisticasAprovacao() {
        return previsaoRepository.findAll().stream()
                .filter(prev -> prev.getProbabilidadeAprovacao() != null)
                .filter(prev -> prev.getSucesso() != null && prev.getSucesso())
                .mapToDouble(prev -> prev.getProbabilidadeAprovacao())
                .summaryStatistics();
    }

    private long contarDocumentosNovos(LocalDate inicio, LocalDate fim) {
        long proposicoes = proposicaoRepository
                .countByDataApresentacaoBetween(inicio, fim);

        long materias = materiaRepository
                .countByDataApresentacaoBetween(inicio, fim);

        return proposicoes + materias;
    }

    private double calcularTaxaCrescimento(
            LocalDate periodoAnteriorInicio,
            LocalDate periodoAnteriorFim,
            LocalDate periodoAtualInicio,
            LocalDate periodoAtualFim) {

        long anterior = contarDocumentosNovos(periodoAnteriorInicio, periodoAnteriorFim);
        long atual = contarDocumentosNovos(periodoAtualInicio, periodoAtualFim);

        if (anterior == 0) return atual > 0 ? 100.0 : 0.0;

        return ((double) (atual - anterior) / anterior) * 100.0;
    }

    private void registrarHistorico(MetricaDashboard metrica) {
        List<HistoricoMetricas> registros = new ArrayList<>();

        registros.add(criarRegistroHistorico(
                "GERAL", "total_documentos", metrica.getTotalDocumentos().doubleValue()));
        registros.add(criarRegistroHistorico(
                "GERAL", "total_proposicoes", metrica.getTotalProposicoes().doubleValue()));
        registros.add(criarRegistroHistorico(
                "GERAL", "total_materias", metrica.getTotalMaterias().doubleValue()));
        registros.add(criarRegistroHistorico(
                "GERAL", "prob_aprovacao_media", metrica.getProbabilidadeAprovacaoMedia()));
        registros.add(criarRegistroHistorico(
                "GERAL", "taxa_aprovacao", metrica.getTaxaAprovacao()));

        historicoRepository.saveAll(registros);
    }

    private HistoricoMetricas criarRegistroHistorico(
            String tipo,
            String metrica,
            Double valor) {

        return HistoricoMetricas.builder()
                .tipo(tipo)
                .dataRegistro(LocalDateTime.now())
                .metrica(metrica)
                .valor(valor)
                .unidade(determinarUnidade(metrica))
                .categoria("SISTEMA")
                .build();
    }

    private String determinarUnidade(String metrica) {
        if (metrica.contains("taxa") || metrica.contains("prob")) {
            return "PERCENTUAL";
        }
        if (metrica.contains("total")) {
            return "QUANTIDADE";
        }
        return "NUMERO";
    }

    private Map<String, Object> coletarDadosParaTendencias() {
        Map<String, Object> dados = new java.util.HashMap<>();

        LocalDate agora = LocalDate.now();
        LocalDate umMesAtras = agora.minusMonths(1);

        long totalProposicoes = proposicaoRepository.count();
        long totalMaterias = materiaRepository.count();

        dados.put("totalDocumentos", totalProposicoes + totalMaterias);
        dados.put("totalProposicoes", totalProposicoes);
        dados.put("totalMaterias", totalMaterias);
        dados.put("documentosUltimoMes", contarDocumentosNovos(umMesAtras, agora));
        dados.put("pareceresPendentes", parecerRepository.countByStatus(StatusParecer.EM_ELABORACAO));
        dados.put("posicionamentosPendentes", posicionamentoRepository.countByStatus(StatusPosicionamento.SOLICITADO));
        dados.put("taxaEficiencia", calcularEficiencia());

        return dados;
    }


    private String construirPromptTendencias(Map<String, Object> dados) {
        return String.format("""
            Analise as tendências legislativas com base nos seguintes dados:
            
            DADOS DO SISTEMA:
            - Documentos novos (últimos 30 dias): %d
            - Crescimento mensal: %.2f%%
            - Temas mais populares: %s
            - Partidos mais ativos: %s
            
            TAREFA:
            Retorne um JSON com:
            
            {
              "analiseGeral": "<análise das tendências em 3-4 frases>",
              "temasEmergentes": ["<tema 1>", "<tema 2>", "<tema 3>"],
              "alertas": ["<alerta 1>", "<alerta 2>"],
              "previsaoProximoMes": "<previsão em 2-3 frases>"
            }
            """,
                dados.get("documentosUltimos30Dias"),
                dados.get("crescimentoMensal"),
                dados.get("temasPopulares"),
                dados.get("partidosAtivos")
        );
    }

    private String construirPromptSistemaTendencias() {
        return """
            Você é um especialista em análise de dados legislativos.
            Sua tarefa é identificar tendências e padrões em dados parlamentares.
            
            Diretrizes:
            - Seja objetivo e baseado em dados
            - Identifique padrões significativos
            - Destaque anomalias ou mudanças importantes
            - Forneça previsões fundamentadas
            
            Sempre responda em formato JSON válido.
            """;
    }

    private ResultadoTendenciasIA parsearRespostaTendencias(RespostaLlamaDTO resposta) {
        try {
            return llamaService.extrairJson(resposta, ResultadoTendenciasIA.class);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta de tendências: {}", e.getMessage());
            throw MetricasException.erroCalculo("Resposta em formato inválido", e);
        }
    }

    private TendenciasDTO construirTendenciasDTO(
            ResultadoTendenciasIA resultado,
            Map<String, Object> dadosContexto) {

        return TendenciasDTO.builder()
                .temasEmAlta(resultado.temasEmergentes())
                .analiseIA(resultado.analiseGeral())
                .alertas(resultado.alertas())
                .previsaoProximoMes(resultado.previsaoProximoMes())
                .build();
    }

    private TendenciasDTO construirTendenciasFallback() {
        return TendenciasDTO.builder()
                .temasEmAlta(List.of("Análise indisponível"))
                .analiseIA("Sistema de análise temporariamente indisponível")
                .alertas(new ArrayList<>())
                .build();
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio.isAfter(fim)) {
            throw MetricasException.periodoInvalido(
                    "Data início não pode ser posterior à data fim");
        }

        if (inicio.isBefore(LocalDateTime.now().minusYears(1))) {
            throw MetricasException.periodoInvalido(
                    "Período máximo de consulta: 1 ano");
        }
    }

    private String analisarTendencia(List<SerieTemporalDTO.PontoTemporalDTO> pontos) {
        if (pontos.size() < 2) return "INSUFICIENTE";

        double primeiro = pontos.get(0).getValor();
        double ultimo = pontos.get(pontos.size() - 1).getValor();

        double variacao = ((ultimo - primeiro) / primeiro) * 100;

        if (variacao > 10) return "CRESCENTE";
        if (variacao < -10) return "DECRESCENTE";
        return "ESTAVEL";
    }

   private long contarAprovados() {
        long proposicoesAprovadas = proposicaoRepository.findAll().stream()
                .filter(p -> p.isAprovada() == true)
                .count();

        long materiasAprovadas = materiaRepository.findAll().stream()
                .filter(m -> m.isAprovada() == true)
                .count();

        return proposicoesAprovadas + materiasAprovadas;
    }

    private long contarRejeitados() {
        long proposicoesRejeitadas = proposicaoRepository.findAll().stream()
                .filter(p -> p.isAprovada() == false)
                .filter(p -> p.getStatusProposicao() != null &&
                        p.getStatusProposicao().toLowerCase().contains("rejeitad"))
                .count();

        long materiasRejeitadas = materiaRepository.findAll().stream()
                .filter(m -> m.isAprovada() == false)
                .filter(m -> m.getSituacaoAtual() != null &&
                        m.getSituacaoAtual().toLowerCase().contains("rejeitad"))
                .count();

        return proposicoesRejeitadas + materiasRejeitadas;
    }

    private long contarEmTramitacao() {
        long proposicoesEmTramitacao = proposicaoRepository.findAll().stream()
                .filter(p -> p.getStatusTramitacao() != null)
                .filter(p -> p.getStatusTramitacao() == StatusTramitacao.EM_ANDAMENTO)
                .count();

        long materiasEmTramitacao = materiaRepository.findAll().stream()
                .filter(m -> m.getSituacaoAtual() != null)
                .filter(m -> !m.getSituacaoAtual().toLowerCase().contains("arquivad"))
                .filter(m -> !m.getSituacaoAtual().toLowerCase().contains("rejeitad"))
                .count();

        return proposicoesEmTramitacao + materiasEmTramitacao;
    }

    private double calcularTaxaAprovacao() {
        long aprovados = contarAprovados();
        long total = contarDocumentosFinalizados();

        if (total == 0) {
            return 0.0;
        }

        return (double) aprovados / total;
    }

    private double calcularTaxaRejeicao() {
        long rejeitados = contarRejeitados();
        long total = contarDocumentosFinalizados();

        if (total == 0) {
            return 0.0;
        }

        return (double) rejeitados / total;
    }

    private long contarDocumentosFinalizados() {
        return contarAprovados() + contarRejeitados();
    }

    private Map<String, Object> calcularKPIsMap() {
        Map<String, Object> kpis = new HashMap<>();

        LocalDate agora = LocalDate.now();
        LocalDate inicioHoje = agora;
        LocalDate inicioSemana = agora.minusWeeks(1);
        LocalDate inicioMes = agora.minusMonths(1);

        kpis.put("totalDocumentosAtivos",
                proposicaoRepository.count() + materiaRepository.count());
        kpis.put("documentosNovosHoje",
                contarDocumentosNovos(inicioHoje, agora));
        kpis.put("documentosNovosEstaSemana",
                contarDocumentosNovos(inicioSemana, agora));
        kpis.put("documentosNovosEsteMes",
                contarDocumentosNovos(inicioMes, agora));

        kpis.put("taxaCrescimentoSemanal",
                calcularTaxaCrescimento(
                        inicioSemana.minusWeeks(1), inicioSemana,
                        inicioSemana, agora));

        kpis.put("taxaCrescimentoMensal",
                calcularTaxaCrescimento(
                        inicioMes.minusMonths(1), inicioMes,
                        inicioMes, agora));

        kpis.put("documentosPendentesTriagem", contarPendentesTriagem());
        kpis.put("pareceresPendentes", contarPareceresPendentes());
        kpis.put("posicionamentosPendentes", contarPosicionamentosPendentes());
        kpis.put("tempoMedioTramitacao", calcularTempoMedioTramitacao());
        kpis.put("documentosComPrazoVencido", contarPrazosVencidos());
        kpis.put("medianaTramitacao", calcularMediana());
        kpis.put("alertasCriticos", contarAlertasCriticos());
        kpis.put("eficienciaProcessamento", calcularEficiencia());
        kpis.put("statusGeral", determinarStatusGeral());

        return kpis;
    }

    private Map<String, Object> calcularTendenciasMap() {
        Map<String, Object> tendencias = new HashMap<>();

        Map<String, Long> temasPorFrequencia = calcularDistribuicaoPorTema();
        List<String> temasEmAlta = temasPorFrequencia.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        Map<String, Long> partidosPorFrequencia = calcularDistribuicaoPorPartido();
        List<String> partidosMaisAtivos = partidosPorFrequencia.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        Map<String, Long> estadosPorFrequencia = calcularDistribuicaoPorEstado();
        List<String> estadosMaisAtivos = estadosPorFrequencia.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        LocalDate agora = LocalDate.now();
        LocalDate umaSemanaAtras = agora.minusWeeks(1);
        LocalDate duasSemanasAtras = agora.minusWeeks(2);
        LocalDate umMesAtras = agora.minusMonths(1);
        LocalDate doisMesesAtras = agora.minusMonths(2);

        double crescimentoSemanal = calcularTaxaCrescimento(
                duasSemanasAtras, umaSemanaAtras, umaSemanaAtras, agora);
        double crescimentoMensal = calcularTaxaCrescimento(
                doisMesesAtras, umMesAtras, umMesAtras, agora);

        tendencias.put("temasEmAlta", temasEmAlta);
        tendencias.put("partidosMaisAtivos", partidosMaisAtivos);
        tendencias.put("estadosMaisAtivos", estadosMaisAtivos);
        tendencias.put("crescimentoSemanal", Map.of("percentual", crescimentoSemanal));
        tendencias.put("crescimentoMensal", Map.of("percentual", crescimentoMensal));

        List<String> alertas = gerarAlertas();
        tendencias.put("alertas", alertas);

        return tendencias;
    }

    private long contarPendentesTriagem() {
        return proposicaoRepository.findAll().stream()
                .filter(p -> p.getStatusTriagem() == StatusTriagem.NAO_AVALIADO)
                .count();
    }

    private double calcularTempoMedioTramitacao() {
        List<Long> tempos = new ArrayList<>();

        proposicaoRepository.findAll().stream()
                .filter(p -> p.getDataApresentacao() != null)
                .forEach(p -> {
                    LocalDateTime inicio = p.getDataApresentacao().atStartOfDay();
                    LocalDateTime fim = p.isAprovada() == true
                            ? (p.getDataUltimaAtualizacao() != null ?
                            p.getDataUltimaAtualizacao() : LocalDateTime.now())
                            : LocalDateTime.now();

                    long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim);
                    tempos.add(dias);
                });

        if (tempos.isEmpty()) {
            return 0.0;
        }

        return tempos.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    private double calcularMediana() {
        List<Double> probabilidades = previsaoRepository.findAll().stream()
                .filter(prev -> prev.getProbabilidadeAprovacao() != null)
                .filter(prev -> Boolean.TRUE.equals(prev.getSucesso()))
                .map(prev -> prev.getProbabilidadeAprovacao())
                .filter(prob -> prob > 0.0)
                .sorted()
                .toList();

        if (probabilidades.isEmpty()) {
            return 0.0;
        }

        int size = probabilidades.size();
        if (size % 2 == 0) {
            return (probabilidades.get(size / 2 - 1) + probabilidades.get(size / 2)) / 2.0;
        } else {
            return probabilidades.get(size / 2);
        }
    }

    private int contarPrazosVencidos() {
        LocalDateTime agora = LocalDateTime.now();
        long count = 0;

        count += parecerRepository.findAll().stream()
                .filter(p -> p.getPrazo() != null)
                .filter(p -> p.getPrazo().isBefore(agora))
                .filter(p -> {

                    StatusParecer status = p.getStatus();
                    return status != null && status != StatusParecer.APROVADO;
                })
                .count();


        count += posicionamentoRepository.findAll().stream()
                .filter(p -> p.getPrazo() != null)
                .filter(p -> p.getPrazo().isBefore(agora))
                .filter(p -> {
                    StatusPosicionamento status = p.getStatus();
                    return status != null && status != StatusPosicionamento.RECEBIDO;
                })
                .count();

        return (int) count;
    }

    private int contarAlertasCriticos() {
        int alertas = 0;

        long pendentesTriagem = contarPendentesTriagem();
        if (pendentesTriagem > 50) {
            alertas++;
        }

        int prazosVencidos = contarPrazosVencidos();
        if (prazosVencidos > 10) {
            alertas++;
        }

        long pareceresPendentes = contarPareceresPendentes();
        if (pareceresPendentes > 30) {
            alertas++;
        }

        long posicionamentosPendentes = contarPosicionamentosPendentes();
        if (posicionamentosPendentes > 20) {
            alertas++;
        }

        double eficiencia = calcularEficiencia();
        if (eficiencia < 0.7) {
            alertas++;
        }

        return alertas;
    }

    private double calcularEficiencia() {
        LocalDate umMesAtras = LocalDate.now().minusMonths(1);

        long documentosNovos = contarDocumentosNovos(umMesAtras, LocalDate.now());
        long documentosProcessados = contarDocumentosProcessados(umMesAtras);

        if (documentosNovos == 0) {
            return 1.0;
        }

        double eficiencia = (double) documentosProcessados / documentosNovos;

        return Math.min(eficiencia, 1.0);
    }

    private long contarDocumentosProcessados(LocalDate desde) {
        return proposicaoRepository.findAll().stream()
                .filter(p -> p.getDataUltimaAtualizacao() != null)
                .filter(p -> p.getDataUltimaAtualizacao().isAfter(desde.atStartOfDay()))
                .filter(p -> p.getStatusTriagem() != StatusTriagem.NAO_AVALIADO)
                .count();
    }

    private String determinarStatusGeral() {
        int alertas = contarAlertasCriticos();
        double eficiencia = calcularEficiencia();

        if (alertas >= 3 || eficiencia < 0.6) {
            return "CRITICO";
        }

        if (alertas >= 2 || eficiencia < 0.75) {
            return "ATENCAO";
        }

        if (alertas == 1 || eficiencia < 0.85) {
            return "ALERTA";
        }

        return "OPERACIONAL";
    }

    private List<String> gerarAlertas() {
        List<String> alertas = new ArrayList<>();

        long pendentesTriagem = contarPendentesTriagem();
        if (pendentesTriagem > 50) {
            alertas.add(String.format("Alto volume de proposições pendentes de triagem: %d",
                    pendentesTriagem));
        }

        int prazosVencidos = contarPrazosVencidos();
        if (prazosVencidos > 10) {
            alertas.add(String.format("Atenção: %d documentos com prazo vencido",
                    prazosVencidos));
        }

        long pareceresPendentes = contarPareceresPendentes();
        if (pareceresPendentes > 30) {
            alertas.add(String.format("Grande quantidade de pareceres pendentes: %d",
                    pareceresPendentes));
        }

        long posicionamentosPendentes = contarPosicionamentosPendentes();
        if (posicionamentosPendentes > 20) {
            alertas.add(String.format("Posicionamentos externos aguardando resposta: %d",
                    posicionamentosPendentes));
        }

        double eficiencia = calcularEficiencia();
        if (eficiencia < 0.7) {
            alertas.add(String.format("Eficiência de processamento abaixo do ideal: %.1f%%",
                    eficiencia * 100));
        }

        LocalDate agora = LocalDate.now();
        LocalDate umaSemanaAtras = agora.minusWeeks(1);
        LocalDate duasSemanasAtras = agora.minusWeeks(2);

        double crescimento = calcularTaxaCrescimento(
                duasSemanasAtras, umaSemanaAtras, umaSemanaAtras, agora);

        if (crescimento > 50) {
            alertas.add(String.format("Crescimento acelerado de documentos: +%.1f%% na última semana",
                    crescimento));
        }

        if (crescimento < -20) {
            alertas.add(String.format("Redução significativa no volume de documentos: %.1f%% na última semana",
                    crescimento));
        }

        return alertas;
    }

    private long contarPareceresPendentes() {
        return parecerRepository.countByStatus(StatusParecer.EM_ELABORACAO);
    }

    private long contarPosicionamentosPendentes() {
        return posicionamentoRepository.countByStatus(StatusPosicionamento.SOLICITADO);
    }

    private record ResultadoTendenciasIA(
            String analiseGeral,
            List<String> temasEmergentes,
            List<String> alertas,
            String previsaoProximoMes
    ) {}
}