package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.service.AnaliseParlamentarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analise-parlamentar")
@RequiredArgsConstructor
@Tag(name = "Análise Parlamentar", description = "Análise de comportamento parlamentar com IA")
@SecurityRequirement(name = "bearer-jwt")
public class AnaliseParlamentarController {

    private final AnaliseParlamentarService analiseParlamentarService;

    @PostMapping("/analisar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Analisar comportamento parlamentar",
            description = "Gera análise de comportamento de votação de parlamentar usando IA"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise gerada"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Parlamentar não encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Llama indisponível", content = @Content)
    })
    public ResponseEntity<AnaliseParlamentarDTO> analisar(
            @Valid @RequestBody SolicitarAnaliseParlamentarDTO request) {

        log.info("Solicitando análise parlamentar: parlamentar={}, tema={}",
                request.getParlamentarId(), request.getTema());

        AnaliseParlamentarDTO analise = analiseParlamentarService.analisar(request);

        return ResponseEntity.ok(analise);
    }

    @GetMapping("/parlamentar/{parlamentarId}/tema/{tema}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análise específica",
            description = "Retorna análise de um parlamentar sobre tema específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise retornada"),
            @ApiResponse(responseCode = "404", description = "Análise não encontrada", content = @Content)
    })
    public ResponseEntity<AnaliseParlamentarDTO> buscarPorParlamentarETema(
            @Parameter(description = "ID do parlamentar") @PathVariable String parlamentarId,
            @Parameter(description = "Tema") @PathVariable String tema) {

        log.debug("Buscando análise: parlamentar={}, tema={}", parlamentarId, tema);

        AnaliseParlamentarDTO analise = analiseParlamentarService
                .buscarAnalisePorParlamentarETema(parlamentarId, tema);

        return ResponseEntity.ok(analise);
    }

    @GetMapping("/parlamentar/{parlamentarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises por parlamentar",
            description = "Retorna todas as análises de um parlamentar"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<List<AnaliseParlamentarDTO>> buscarPorParlamentar(
            @Parameter(description = "ID do parlamentar") @PathVariable String parlamentarId) {

        log.debug("Buscando análises do parlamentar: {}", parlamentarId);

        List<AnaliseParlamentarDTO> analises = analiseParlamentarService
                .buscarAnalisesPorParlamentar(parlamentarId);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/tema/{tema}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises por tema",
            description = "Retorna análises de todos parlamentares sobre um tema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseParlamentarDTO>> buscarPorTema(
            @Parameter(description = "Tema") @PathVariable String tema,
            @PageableDefault(size = 20, sort = "dataAnalise") Pageable pageable) {

        log.debug("Buscando análises por tema: {}", tema);

        Page<AnaliseParlamentarDTO> analises = analiseParlamentarService
                .buscarAnalisesPorTema(tema, pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/comportamento/{parlamentarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter comportamento de votação",
            description = "Retorna estatísticas de comportamento de votação do parlamentar"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comportamento retornado"),
            @ApiResponse(responseCode = "404", description = "Parlamentar não encontrado", content = @Content)
    })
    public ResponseEntity<ComportamentoParlamentarDTO> obterComportamento(
            @Parameter(description = "ID do parlamentar") @PathVariable String parlamentarId,
            @Parameter(description = "Tema (opcional)") @RequestParam(required = false) String tema,
            @Parameter(description = "Data início (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data fim (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        log.debug("Obtendo comportamento: parlamentar={}, tema={}", parlamentarId, tema);

        ComportamentoParlamentarDTO comportamento = analiseParlamentarService
                .obterComportamento(parlamentarId, tema, inicio, fim);

        return ResponseEntity.ok(comportamento);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises recentes",
            description = "Retorna análises dos últimos 30 dias com paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<Page<AnaliseParlamentarDTO>> buscarRecentes(
            @PageableDefault(size = 20, sort = "dataAnalise") Pageable pageable) {

        log.debug("Buscando análises parlamentares recentes");

        Page<AnaliseParlamentarDTO> analises = analiseParlamentarService
                .buscarAnalisesRecentes(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Estatísticas de análises",
            description = "Retorna métricas agregadas das análises parlamentares"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas calculadas")
    })
    public ResponseEntity<EstatisticasParlamentarDTO> obterEstatisticas(
            @Parameter(description = "Últimos N dias")
            @RequestParam(defaultValue = "30") int dias) {

        log.debug("Calculando estatísticas dos últimos {} dias", dias);

        EstatisticasParlamentarDTO stats = analiseParlamentarService
                .calcularEstatisticas(dias);

        return ResponseEntity.ok(stats);
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

        log.info("Executando limpeza de análises parlamentares expiradas");

        analiseParlamentarService.limparExpiradas();

        return ResponseEntity.noContent().build();
    }
}
