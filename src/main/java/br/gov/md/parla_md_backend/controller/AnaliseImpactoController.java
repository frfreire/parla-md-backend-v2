package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.AnaliseImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.EstatisticasImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarAnaliseImpactoDTO;
import br.gov.md.parla_md_backend.service.AnaliseImpactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analise-impacto")
@RequiredArgsConstructor
@Tag(name = "Análise de Impacto", description = "Análise de impacto de proposições legislativas")
@SecurityRequirement(name = "bearer-jwt")
public class AnaliseImpactoController {

    private final AnaliseImpactoService analiseImpactoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Analisar impacto de item legislativo",
            description = "Solicita análise de impacto de um item legislativo em uma ou mais áreas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises realizadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item legislativo não encontrado", content = @Content)
    })
    public ResponseEntity<List<AnaliseImpactoDTO>> analisar(
            @Valid @RequestBody SolicitarAnaliseImpactoDTO request) {

        log.info("Solicitando análise de impacto para item: {}", request.getItemLegislativoId());

        List<AnaliseImpactoDTO> analises = analiseImpactoService.analisar(request);

        log.info("Geradas {} análises para item: {}", analises.size(), request.getItemLegislativoId());

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/item/{itemId}/area/{areaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análise por item e área",
            description = "Retorna análise específica de um item legislativo em uma área de impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise encontrada"),
            @ApiResponse(responseCode = "404", description = "Análise não encontrada", content = @Content)
    })
    public ResponseEntity<AnaliseImpactoDTO> buscarPorItemEArea(
            @Parameter(description = "ID do item legislativo") @PathVariable String itemId,
            @Parameter(description = "ID da área de impacto") @PathVariable String areaId) {

        log.debug("Buscando análise: item={}, area={}", itemId, areaId);

        AnaliseImpactoDTO analise = analiseImpactoService.buscarPorItemEArea(itemId, areaId);

        return ResponseEntity.ok(analise);
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por item legislativo",
            description = "Retorna todas as análises de um item legislativo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorItem(
            @Parameter(description = "ID do item legislativo") @PathVariable String itemId,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises do item: {}", itemId);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorItem(itemId, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/area/{areaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por área de impacto",
            description = "Retorna todas as análises de uma área de impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorArea(
            @Parameter(description = "ID da área de impacto") @PathVariable String areaId,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises da área: {}", areaId);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorArea(areaId, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/nivel/{nivelImpacto}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por nível de impacto",
            description = "Retorna análises filtradas por nível (ALTO, MEDIO, BAIXO, NENHUM)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorNivel(
            @Parameter(description = "Nível de impacto") @PathVariable String nivelImpacto,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de nível: {}", nivelImpacto);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorNivelImpacto(nivelImpacto, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/nivel/alto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de alto impacto",
            description = "Retorna análises classificadas como alto impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarAltoImpacto(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de alto impacto");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarAltoImpacto(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/nivel/medio")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de médio impacto",
            description = "Retorna análises classificadas como médio impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarMedioImpacto(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de médio impacto");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarMedioImpacto(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/nivel/baixo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de baixo impacto",
            description = "Retorna análises classificadas como baixo impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarBaixoImpacto(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de baixo impacto");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarBaixoImpacto(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/tipo/{tipoImpacto}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por tipo de impacto",
            description = "Retorna análises filtradas por tipo (POSITIVO, NEGATIVO, MISTO, NEUTRO)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorTipo(
            @Parameter(description = "Tipo de impacto") @PathVariable String tipoImpacto,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de tipo: {}", tipoImpacto);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorTipoImpacto(tipoImpacto, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/tipo/negativo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de impacto negativo",
            description = "Retorna análises com impacto negativo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarImpactoNegativo(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de impacto negativo");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarImpactoNegativo(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/tipo/positivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de impacto positivo",
            description = "Retorna análises com impacto positivo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarImpactoPositivo(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de impacto positivo");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarImpactoPositivo(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/criticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises críticas",
            description = "Retorna análises de alto impacto negativo (máxima prioridade)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarAnalisesCriticas(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises críticas");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarAnalisesCriticas(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/alto-impacto-negativo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises de alto impacto negativo",
            description = "Retorna análises classificadas como alto impacto e tipo negativo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarAltoImpactoNegativo(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises de alto impacto negativo");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarAltoImpactoNegativo(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/percentual-minimo/{percentual}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por percentual mínimo",
            description = "Retorna análises com percentual de impacto maior ou igual ao especificado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<List<AnaliseImpactoDTO>> buscarPorPercentualMinimo(
            @Parameter(description = "Percentual mínimo (0.0 a 1.0)") @PathVariable Double percentual) {

        log.debug("Buscando análises com percentual >= {}", percentual);

        List<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorPercentualMinimo(percentual);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/percentual")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por faixa de percentual",
            description = "Retorna análises com percentual de impacto em um intervalo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorPercentualEntre(
            @Parameter(description = "Percentual mínimo") @RequestParam Double min,
            @Parameter(description = "Percentual máximo") @RequestParam Double max,
            @PageableDefault(size = 20, sort = "percentualImpacto", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises com percentual entre {} e {}", min, max);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorPercentualEntre(min, max, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/bem-sucedidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises bem-sucedidas",
            description = "Retorna apenas análises que foram processadas com sucesso"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarBemSucedidas(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises bem-sucedidas");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarBemSucedidas(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/falhas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar análises que falharam",
            description = "Retorna análises que apresentaram erro no processamento"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarFalhas(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises que falharam");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarFalhas(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises recentes",
            description = "Retorna análises dos últimos 30 dias"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarRecentes(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises recentes");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarRecentes(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/recentes/bem-sucedidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises recentes bem-sucedidas",
            description = "Retorna análises bem-sucedidas dos últimos 30 dias"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarRecentesBemSucedidas(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises recentes bem-sucedidas");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarRecentesBemSucedidas(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/recentes/alto-impacto")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises recentes de alto impacto",
            description = "Retorna análises de alto impacto dos últimos 30 dias"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarAltoImpactoRecentes(
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises recentes de alto impacto");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarAltoImpactoRecentes(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises por período",
            description = "Retorna análises realizadas entre duas datas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorPeriodo(
            @Parameter(description = "Data inicial")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data final")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises entre {} e {}", inicio, fim);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorPeriodo(inicio, fim, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/area/{areaId}/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar análises de área por período",
            description = "Retorna análises de uma área específica em um período"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<List<AnaliseImpactoDTO>> buscarPorAreaNoPeriodo(
            @Parameter(description = "ID da área de impacto") @PathVariable String areaId,
            @Parameter(description = "Data inicial")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data final")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        log.debug("Buscando análises da área {} entre {} e {}", areaId, inicio, fim);

        List<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorAreaNoPeriodo(areaId, inicio, fim);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/modelo/{versao}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar análises por versão do modelo",
            description = "Retorna análises geradas por uma versão específica do modelo de IA"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarPorModeloVersao(
            @Parameter(description = "Versão do modelo") @PathVariable String versao,
            @PageableDefault(size = 20, sort = "dataAnalise", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando análises do modelo versão: {}", versao);

        Page<AnaliseImpactoDTO> analises = analiseImpactoService.buscarPorModeloVersao(versao, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Obter estatísticas de análises",
            description = "Retorna métricas agregadas das análises de impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas calculadas")
    })
    public ResponseEntity<EstatisticasImpactoDTO> obterEstatisticas(
            @Parameter(description = "Últimos N dias") @RequestParam(defaultValue = "30") int dias) {

        log.debug("Calculando estatísticas dos últimos {} dias", dias);

        EstatisticasImpactoDTO estatisticas = analiseImpactoService.calcularEstatisticas(dias);

        return ResponseEntity.ok(estatisticas);
    }

    @DeleteMapping("/limpar-expiradas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Limpar análises expiradas",
            description = "Remove análises antigas além do TTL configurado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Limpeza executada")
    })
    public ResponseEntity<Void> limparExpiradas() {

        log.info("Executando limpeza de análises expiradas");

        analiseImpactoService.limparExpiradas();

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contar/nivel/{nivelImpacto}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Contar análises por nível",
            description = "Retorna o total de análises de um nível específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarPorNivel(
            @Parameter(description = "Nível de impacto") @PathVariable String nivelImpacto) {

        log.debug("Contando análises de nível: {}", nivelImpacto);

        long total = analiseImpactoService.contarPorNivel(nivelImpacto);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/contar/tipo/{tipoImpacto}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Contar análises por tipo",
            description = "Retorna o total de análises de um tipo específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarPorTipo(
            @Parameter(description = "Tipo de impacto") @PathVariable String tipoImpacto) {

        log.debug("Contando análises de tipo: {}", tipoImpacto);

        long total = analiseImpactoService.contarPorTipo(tipoImpacto);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/contar/bem-sucedidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Contar análises bem-sucedidas",
            description = "Retorna o total de análises processadas com sucesso"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarBemSucedidas() {

        log.debug("Contando análises bem-sucedidas");

        long total = analiseImpactoService.contarBemSucedidas();

        return ResponseEntity.ok(total);
    }

    @GetMapping("/contar/falhas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Contar análises que falharam",
            description = "Retorna o total de análises que apresentaram erro"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarFalhas() {

        log.debug("Contando análises que falharam");

        long total = analiseImpactoService.contarFalhas();

        return ResponseEntity.ok(total);
    }

    @GetMapping("/existe/item/{itemId}/area/{areaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Verificar se existe análise",
            description = "Verifica se existe análise para item e área específicos"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado da verificação")
    })
    public ResponseEntity<Boolean> existeAnalisePara(
            @Parameter(description = "ID do item legislativo") @PathVariable String itemId,
            @Parameter(description = "ID da área de impacto") @PathVariable String areaId) {

        log.debug("Verificando existência de análise: item={}, area={}", itemId, areaId);

        boolean existe = analiseImpactoService.existeAnalisePara(itemId, areaId);

        return ResponseEntity.ok(existe);
    }
}