package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.service.MetricasDesempenhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Métricas e Dashboard", description = "Métricas de desempenho e dashboard do sistema")
@SecurityRequirement(name = "bearer-key")
public class MetricasDesempenhoController {

    private final MetricasDesempenhoService metricasService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter métricas atuais",
            description = "Retorna as métricas atuais do dashboard (com cache)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas retornadas"),
            @ApiResponse(responseCode = "500", description = "Erro ao calcular métricas", content = @Content)
    })
    public ResponseEntity<DashboardMetricasDTO> obterMetricasAtuais() {
        log.debug("Requisição para obter métricas atuais do dashboard");

        DashboardMetricasDTO metricas = metricasService.obterMetricasAtuais();

        return ResponseEntity.ok(metricas);
    }

    @PostMapping("/recalcular")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Recalcular métricas",
            description = "Força o recálculo completo de todas as métricas (limpa cache)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas recalculadas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro ao recalcular métricas", content = @Content)
    })
    public ResponseEntity<DashboardMetricasDTO> recalcularMetricas() {
        log.info("Requisição para recalcular métricas do dashboard");

        DashboardMetricasDTO metricas = metricasService.recalcularMetricas();

        log.info("Métricas recalculadas com sucesso");

        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter KPIs do sistema",
            description = "Retorna os principais indicadores de desempenho do sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "KPIs retornados"),
            @ApiResponse(responseCode = "500", description = "Erro ao calcular KPIs", content = @Content)
    })
    public ResponseEntity<KPIsDTO> obterKPIs() {
        log.debug("Requisição para obter KPIs do sistema");

        KPIsDTO kpis = metricasService.obterKPIs();

        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/serie-temporal")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter série temporal",
            description = "Retorna série temporal de uma métrica específica em um período"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Série temporal retornada"),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro ao obter série temporal", content = @Content)
    })
    public ResponseEntity<SerieTemporalDTO> obterSerieTemporal(
            @Parameter(description = "Nome da métrica", example = "total_documentos")
            @RequestParam String metrica,

            @Parameter(description = "Data início (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,

            @Parameter(description = "Data fim (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        log.debug("Requisição série temporal: metrica={}, inicio={}, fim={}",
                metrica, inicio, fim);

        SerieTemporalDTO serie = metricasService.obterSerieTemporal(metrica, inicio, fim);

        return ResponseEntity.ok(serie);
    }

    @GetMapping("/tendencias")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Analisar tendências com IA",
            description = "Usa IA (Llama) para analisar tendências legislativas e gerar insights"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise de tendências retornada"),
            @ApiResponse(responseCode = "503", description = "Llama indisponível (retorna fallback)", content = @Content)
    })
    public ResponseEntity<TendenciasDTO> analisarTendencias() {
        log.info("Requisição para análise de tendências com IA");

        TendenciasDTO tendencias = metricasService.analisarTendenciasComIA();

        return ResponseEntity.ok(tendencias);
    }

    @GetMapping("/historico/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Obter histórico de métricas",
            description = "Retorna histórico de uma métrica específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content)
    })
    public ResponseEntity<SerieTemporalDTO> obterHistorico(
            @Parameter(description = "Tipo de métrica") @PathVariable String tipo,

            @Parameter(description = "Últimos N dias", example = "30")
            @RequestParam(defaultValue = "30") int dias) {

        log.debug("Requisição histórico: tipo={}, dias={}", tipo, dias);

        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(dias);

        SerieTemporalDTO serie = metricasService.obterSerieTemporal(tipo, inicio, fim);

        return ResponseEntity.ok(serie);
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter resumo executivo",
            description = "Retorna resumo com principais métricas e KPIs"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado")
    })
    public ResponseEntity<ResumoExecutivoDTO> obterResumoExecutivo() {
        log.debug("Requisição para resumo executivo");

        DashboardMetricasDTO metricas = metricasService.obterMetricasAtuais();
        KPIsDTO kpis = metricasService.obterKPIs();

        ResumoExecutivoDTO resumo = ResumoExecutivoDTO.builder()
                .dataGeracao(LocalDateTime.now())
                .totalDocumentos(metricas.getTotalDocumentos())
                .documentosNovosHoje(kpis.getDocumentosNovosHoje())
                .documentosNovosEstaSemana(kpis.getDocumentosNovosEstaSemana())
                .documentosNovosEsteMes(kpis.getDocumentosNovosEsteMes())
                .taxaCrescimentoSemanal(kpis.getTaxaCrescimentoSemanal())
                .taxaCrescimentoMensal(kpis.getTaxaCrescimentoMensal())
                .pendentesTriagem(kpis.getDocumentosPendentesTriagem())
                .pareceresPendentes(kpis.getPareceresPendentes())
                .posicionamentosPendentes(kpis.getPosicionamentosPendentes())
                .prazosVencidos(kpis.getDocumentosComPrazoVencido())
                .alertasCriticos(kpis.getAlertasCriticos())
                .eficiencia(kpis.getEficienciaProcessamento())
                .statusGeral(kpis.getStatusGeral())
                .probabilidadeAprovacaoMedia(metricas.getProbabilidadeAprovacaoMedia())
                .taxaAprovacao(metricas.getTaxaAprovacao())
                .build();

        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Status geral do sistema",
            description = "Retorna status operacional e alertas críticos"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado")
    })
    public ResponseEntity<StatusSistemaDTO> obterStatusSistema() {
        log.debug("Requisição para status do sistema");

        KPIsDTO kpis = metricasService.obterKPIs();

        StatusSistemaDTO status = StatusSistemaDTO.builder()
                .timestamp(LocalDateTime.now())
                .statusGeral(kpis.getStatusGeral())
                .alertasCriticos(kpis.getAlertasCriticos())
                .eficiencia(kpis.getEficienciaProcessamento())
                .documentosAtivos(kpis.getTotalDocumentosAtivos())
                .pendentesTriagem(kpis.getDocumentosPendentesTriagem())
                .pareceresPendentes(kpis.getPareceresPendentes())
                .posicionamentosPendentes(kpis.getPosicionamentosPendentes())
                .prazosVencidos(kpis.getDocumentosComPrazoVencido())
                .tempoMedioTramitacao(kpis.getTempoMedioTramitacao())
                .build();

        return ResponseEntity.ok(status);
    }

    @GetMapping("/distribuicao/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Distribuição de documentos",
            description = "Retorna distribuição de documentos por tipo, partido, estado ou tema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distribuição retornada"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido", content = @Content)
    })
    public ResponseEntity<DistribuicaoDTO> obterDistribuicao(
            @Parameter(description = "Tipo de distribuição",
                    example = "tipo|partido|estado|tema")
            @PathVariable String tipo) {

        log.debug("Requisição distribuição por: {}", tipo);

        DashboardMetricasDTO metricas = metricasService.obterMetricasAtuais();

        DistribuicaoDTO distribuicao = switch (tipo.toLowerCase()) {
            case "tipo" -> DistribuicaoDTO.builder()
                    .titulo("Distribuição por Tipo")
                    .dados(metricas.getDistribuicaoPorTipo())
                    .build();
            case "partido" -> DistribuicaoDTO.builder()
                    .titulo("Distribuição por Partido")
                    .dados(metricas.getDistribuicaoPorPartido())
                    .build();
            case "estado" -> DistribuicaoDTO.builder()
                    .titulo("Distribuição por Estado")
                    .dados(metricas.getDistribuicaoPorEstado())
                    .build();
            case "tema" -> DistribuicaoDTO.builder()
                    .titulo("Distribuição por Tema")
                    .dados(metricas.getDistribuicaoPorTema())
                    .build();
            default -> throw new IllegalArgumentException(
                    "Tipo inválido. Use: tipo, partido, estado ou tema");
        };

        return ResponseEntity.ok(distribuicao);
    }
}