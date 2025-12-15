package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.posicionamento.TipoPosicionamento;
import br.gov.md.parla_md_backend.domain.dto.PosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitacaoPosicionamentoDTO;
import br.gov.md.parla_md_backend.service.posicionamento.PosicionamentoService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

/**
 * Controller para gerenciamento de posicionamentos externos
 */
@Slf4j
@RestController
@RequestMapping("/api/posicionamentos")
@RequiredArgsConstructor
@Tag(name = "Posicionamentos", description = "Endpoints para solicitação e registro de posicionamentos de órgãos externos")
@SecurityRequirement(name = "bearer-jwt")
public class PosicionamentoController {

    private final PosicionamentoService posicionamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Solicitar posicionamento a órgão externo",
            description = "Solicita posicionamento institucional a Ministério ou Força Armada")
    public ResponseEntity<PosicionamentoDTO> solicitarPosicionamento(
            @Valid @RequestBody SolicitacaoPosicionamentoDTO dto,
            Authentication authentication) {

        log.info("Solicitando posicionamento para processo {} ao órgão {}",
                dto.getProcessoId(), dto.getOrgaoEmissorId());

        String solicitanteId = authentication.getName();
        PosicionamentoDTO posicionamento = posicionamentoService.solicitarPosicionamento(
                dto, solicitanteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(posicionamento);
    }

    @PutMapping("/{posicionamentoId}/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXTERNO')")
    @Operation(summary = "Registrar posicionamento recebido",
            description = "Registra posicionamento recebido de órgão externo")
    public ResponseEntity<PosicionamentoDTO> registrarPosicionamento(
            @PathVariable String posicionamentoId,
            @RequestParam String representanteNome,
            @RequestParam String representanteCargo,
            @RequestParam TipoPosicionamento posicao,
            @RequestParam String manifestacao,
            @RequestParam String justificativa,
            @RequestParam(required = false) List<String> fundamentacao,
            @RequestParam String numeroOficio) {

        log.info("Registrando posicionamento recebido: {}", posicionamentoId);

        PosicionamentoDTO posicionamento = posicionamentoService.registrarPosicionamento(
                posicionamentoId, representanteNome, representanteCargo,
                posicao, manifestacao, justificativa, fundamentacao, numeroOficio);

        return ResponseEntity.ok(posicionamento);
    }

    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamentos de um processo")
    public ResponseEntity<List<PosicionamentoDTO>> buscarPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando posicionamentos do processo: {}", processoId);

        List<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPorProcesso(
                processoId);
        return ResponseEntity.ok(posicionamentos);
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamentos pendentes")
    public ResponseEntity<Page<PosicionamentoDTO>> buscarPendentes(
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando posicionamentos pendentes");

        Page<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPendentes(
                pageable);
        return ResponseEntity.ok(posicionamentos);
    }

    @GetMapping("/prazo-vencido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar posicionamentos com prazo vencido")
    public ResponseEntity<List<PosicionamentoDTO>> buscarComPrazoVencido() {
        log.debug("Buscando posicionamentos com prazo vencido");

        List<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarComPrazoVencido();
        return ResponseEntity.ok(posicionamentos);
    }
}