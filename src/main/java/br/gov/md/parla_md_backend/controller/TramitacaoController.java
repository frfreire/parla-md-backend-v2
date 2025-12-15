package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.EncaminhamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.TramitacaoDTO;
import br.gov.md.parla_md_backend.service.tramitacao.TramitacaoService;
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
 * Controller para gerenciamento de tramitações
 */
@Slf4j
@RestController
@RequestMapping("/api/tramitacoes")
@RequiredArgsConstructor
@Tag(name = "Tramitações", description = "Endpoints para tramitação de processos entre setores e órgãos")
@SecurityRequirement(name = "bearer-jwt")
public class TramitacaoController {

    private final TramitacaoService tramitacaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Encaminhar processo",
            description = "Encaminha processo para setor interno ou órgão externo")
    public ResponseEntity<TramitacaoDTO> encaminharProcesso(
            @Valid @RequestBody EncaminhamentoDTO dto,
            Authentication authentication) {

        log.info("Encaminhando processo {} por usuário: {}",
                dto.getProcessoId(), authentication.getName());

        String usuarioId = authentication.getName();
        TramitacaoDTO tramitacao = tramitacaoService.encaminharProcesso(dto, usuarioId);

        return ResponseEntity.status(HttpStatus.CREATED).body(tramitacao);
    }

    @PutMapping("/{tramitacaoId}/receber")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Receber tramitação",
            description = "Registra o recebimento de uma tramitação")
    public ResponseEntity<TramitacaoDTO> receberTramitacao(
            @PathVariable String tramitacaoId,
            Authentication authentication) {

        log.info("Recebendo tramitação {} por usuário: {}",
                tramitacaoId, authentication.getName());

        String usuarioId = authentication.getName();
        TramitacaoDTO tramitacao = tramitacaoService.receberTramitacao(
                tramitacaoId, usuarioId);

        return ResponseEntity.ok(tramitacao);
    }

    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar tramitações de um processo",
            description = "Lista todo o histórico de tramitações do processo")
    public ResponseEntity<List<TramitacaoDTO>> buscarPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando tramitações do processo: {}", processoId);

        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorProcesso(processoId);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/pendentes/{destinatarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar tramitações pendentes de recebimento")
    public ResponseEntity<Page<TramitacaoDTO>> buscarPendentesRecebimento(
            @PathVariable String destinatarioId,
            @PageableDefault(size = 20, sort = "dataEnvio", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando tramitações pendentes para: {}", destinatarioId);

        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPendentesRecebimento(
                destinatarioId, pageable);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/enviadas/{remetenteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar tramitações enviadas")
    public ResponseEntity<Page<TramitacaoDTO>> buscarEnviadasPor(
            @PathVariable String remetenteId,
            @PageableDefault(size = 20, sort = "dataEnvio", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando tramitações enviadas por: {}", remetenteId);

        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarEnviadasPor(
                remetenteId, pageable);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/recebidas/{destinatarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar tramitações recebidas")
    public ResponseEntity<Page<TramitacaoDTO>> buscarRecebidasPor(
            @PathVariable String destinatarioId,
            @PageableDefault(size = 20, sort = "dataRecebimento", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando tramitações recebidas por: {}", destinatarioId);

        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarRecebidasPor(
                destinatarioId, pageable);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/prazo-vencido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar tramitações com prazo vencido")
    public ResponseEntity<List<TramitacaoDTO>> buscarComPrazoVencido() {
        log.debug("Buscando tramitações com prazo vencido");

        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarComPrazoVencido();
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/urgentes/{destinatarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar tramitações urgentes pendentes")
    public ResponseEntity<List<TramitacaoDTO>> buscarUrgentePendentes(
            @PathVariable String destinatarioId) {

        log.debug("Buscando tramitações urgentes para: {}", destinatarioId);

        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarUrgentePendentes(
                destinatarioId);
        return ResponseEntity.ok(tramitacoes);
    }
}