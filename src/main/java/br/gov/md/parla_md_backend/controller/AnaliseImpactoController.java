package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.*;
import br.gov.md.parla_md_backend.service.AnaliseImpactoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analise-impacto")
@RequiredArgsConstructor
@Tag(name = "Análise de Impacto", description = "Análise de impacto legislativo com IA")
@SecurityRequirement(name = "bearer-key")
public class AnaliseImpactoController {

    private final AnaliseImpactoService analiseImpactoService;

    @PostMapping("/analisar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Analisar impacto de item legislativo",
            description = "Gera análise de impacto usando IA para uma ou mais áreas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises geradas"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "503", description = "Llama indisponível", content = @Content)
    })
    public ResponseEntity<List<AnaliseImpactoDTO>> analisar(
            @Valid @RequestBody SolicitarAnaliseImpactoDTO request) {

        log.info("Solicitando análise de impacto: {}", request.getItemLegislativoId());

        List<AnaliseImpactoDTO> analises = analiseImpactoService.analisar(request);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análises por item",
            description = "Retorna todas as análises de um item legislativo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análises retornadas")
    })
    public ResponseEntity<List<AnaliseImpactoDTO>> buscarPorItem(
            @Parameter(description = "ID do item legislativo") @PathVariable String itemId) {

        log.debug("Buscando análises do item: {}", itemId);

        List<AnaliseImpactoDTO> analises = analiseImpactoService.buscarAnalisesPorItem(itemId);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/item/{itemId}/area/{areaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análise específica",
            description = "Retorna análise de um item em uma área específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise retornada"),
            @ApiResponse(responseCode = "404", description = "Análise não encontrada", content = @Content)
    })
    public ResponseEntity<AnaliseImpactoDTO> buscarPorItemEArea(
            @Parameter(description = "ID do item") @PathVariable String itemId,
            @Parameter(description = "ID da área") @PathVariable String areaId) {

        log.debug("Buscando análise: item={}, area={}", itemId, areaId);

        AnaliseImpactoDTO analise = analiseImpactoService
                .buscarAnalisePorItemEArea(itemId, areaId);

        return ResponseEntity.ok(analise);
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
    public ResponseEntity<Page<AnaliseImpactoDTO>> buscarRecentes(
            @PageableDefault(size = 20, sort = "dataAnalise") Pageable pageable) {

        log.debug("Buscando análises recentes");

        Page<AnaliseImpactoDTO> analises = analiseImpactoService
                .buscarAnalisesRecentes(pageable);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Estatísticas de análises",
            description = "Retorna métricas agregadas das análises de impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas calculadas")
    })
    public ResponseEntity<EstatisticasImpactoDTO> obterEstatisticas(
            @Parameter(description = "Últimos N dias")
            @RequestParam(defaultValue = "30") int dias) {

        log.debug("Calculando estatísticas dos últimos {} dias", dias);

        EstatisticasImpactoDTO stats = analiseImpactoService.calcularEstatisticas(dias);

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

        log.info("Executando limpeza de análises expiradas");

        analiseImpactoService.limparExpiradas();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar área de impacto",
            description = "Cria nova área de impacto para análises"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> criarArea(
            @Valid @RequestBody AreaImpactoDTO dto) {

        log.info("Criando área de impacto: {}", dto.getNome());

        AreaImpactoDTO area = analiseImpactoService.criarAreaImpacto(dto);

        return ResponseEntity.ok(area);
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar área de impacto",
            description = "Atualiza área de impacto existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área atualizada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> atualizarArea(
            @PathVariable String id,
            @Valid @RequestBody AreaImpactoDTO dto) {

        log.info("Atualizando área de impacto: {}", id);

        AreaImpactoDTO area = analiseImpactoService.atualizarAreaImpacto(id, dto);

        return ResponseEntity.ok(area);
    }

    @DeleteMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deletar área de impacto",
            description = "Remove área de impacto do sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Área deletada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deletarArea(@PathVariable String id) {

        log.info("Deletando área de impacto: {}", id);

        analiseImpactoService.deletarAreaImpacto(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/areas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar áreas de impacto",
            description = "Retorna todas as áreas de impacto cadastradas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarAreas() {

        log.debug("Listando todas as áreas de impacto");

        List<AreaImpactoDTO> areas = analiseImpactoService.listarTodasAreas();

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/areas/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar áreas ativas",
            description = "Retorna apenas áreas de impacto ativas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarAreasAtivas() {

        log.debug("Listando áreas de impacto ativas");

        List<AreaImpactoDTO> areas = analiseImpactoService.listarAreasAtivas();

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/areas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar área por ID",
            description = "Retorna detalhes de uma área específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área retornada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> buscarAreaPorId(@PathVariable String id) {

        log.debug("Buscando área de impacto: {}", id);

        AreaImpactoDTO area = analiseImpactoService.buscarAreaPorId(id);

        return ResponseEntity.ok(area);
    }
}