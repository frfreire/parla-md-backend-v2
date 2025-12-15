package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.processo.StatusProcesso;
import br.gov.md.parla_md_backend.domain.dto.CriarProcessoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.service.processo.ProcessoLegislativoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciamento de processos legislativos
 */
@Slf4j
@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
@Tag(name = "Processos Legislativos", description = "Endpoints para gerenciamento de processos legislativos")
@SecurityRequirement(name = "bearer-jwt")
public class ProcessoLegislativoController {

    private final ProcessoLegislativoService processoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Criar novo processo legislativo",
            description = "Cria um novo processo agrupando proposições relacionadas")
    public ResponseEntity<ProcessoLegislativoDTO> criarProcesso(
            @Valid @RequestBody CriarProcessoDTO dto,
            Authentication authentication) {

        log.info("Criando processo legislativo: {} por usuário: {}",
                dto.getTitulo(), authentication.getName());

        String usuarioId = authentication.getName();
        ProcessoLegislativoDTO processo = processoService.criarProcesso(dto, usuarioId);

        return ResponseEntity.status(HttpStatus.CREATED).body(processo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar processo por ID")
    public ResponseEntity<ProcessoLegislativoDTO> buscarPorId(
            @PathVariable String id) {

        log.debug("Buscando processo: {}", id);

        ProcessoLegislativoDTO processo = processoService.buscarPorId(id);
        return ResponseEntity.ok(processo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Listar todos os processos com paginação")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> listarProcessos(
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Listando processos - página: {}", pageable.getPageNumber());

        Page<ProcessoLegislativoDTO> processos = processoService.listarProcessos(pageable);
        return ResponseEntity.ok(processos);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar processos por status")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> buscarPorStatus(
            @PathVariable StatusProcesso status,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando processos com status: {}", status);

        Page<ProcessoLegislativoDTO> processos = processoService.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(processos);
    }

    @GetMapping("/setor/{setorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar processos por setor responsável")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> buscarPorSetor(
            @PathVariable String setorId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando processos do setor: {}", setorId);

        Page<ProcessoLegislativoDTO> processos = processoService.buscarPorSetor(setorId, pageable);
        return ResponseEntity.ok(processos);
    }

    @GetMapping("/analista/{analistaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar processos por analista responsável")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> buscarPorAnalista(
            @PathVariable String analistaId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando processos do analista: {}", analistaId);

        Page<ProcessoLegislativoDTO> processos = processoService.buscarPorAnalista(
                analistaId, pageable);
        return ResponseEntity.ok(processos);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Atualizar status do processo")
    public ResponseEntity<ProcessoLegislativoDTO> atualizarStatus(
            @PathVariable String id,
            @RequestParam StatusProcesso novoStatus,
            @RequestParam(required = false) String observacao) {

        log.info("Atualizando status do processo {} para {}", id, novoStatus);

        ProcessoLegislativoDTO processo = processoService.atualizarStatus(
                id, novoStatus, observacao);
        return ResponseEntity.ok(processo);
    }

    @PostMapping("/{processoId}/proposicoes/{proposicaoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Vincular proposição ao processo")
    public ResponseEntity<ProcessoLegislativoDTO> vincularProposicao(
            @PathVariable String processoId,
            @PathVariable String proposicaoId) {

        log.info("Vinculando proposição {} ao processo {}", proposicaoId, processoId);

        ProcessoLegislativoDTO processo = processoService.vincularProposicao(
                processoId, proposicaoId);
        return ResponseEntity.ok(processo);
    }

    @DeleteMapping("/{processoId}/proposicoes/{proposicaoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Desvincular proposição do processo")
    public ResponseEntity<ProcessoLegislativoDTO> desvincularProposicao(
            @PathVariable String processoId,
            @PathVariable String proposicaoId) {

        log.info("Desvinculando proposição {} do processo {}", proposicaoId, processoId);

        ProcessoLegislativoDTO processo = processoService.desvincularProposicao(
                processoId, proposicaoId);
        return ResponseEntity.ok(processo);
    }

    @PutMapping("/{processoId}/posicao-final")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Definir posição final do MD sobre o processo",
            description = "Apenas autoridades podem definir a posição final")
    public ResponseEntity<ProcessoLegislativoDTO> definirPosicaoFinal(
            @PathVariable String processoId,
            @RequestParam String posicao,
            @RequestParam String justificativa,
            Authentication authentication) {

        log.info("Definindo posição final do MD para processo: {}", processoId);

        String usuarioId = authentication.getName();
        ProcessoLegislativoDTO processo = processoService.definirPosicaoFinal(
                processoId, posicao, justificativa, usuarioId);

        return ResponseEntity.ok(processo);
    }
}
