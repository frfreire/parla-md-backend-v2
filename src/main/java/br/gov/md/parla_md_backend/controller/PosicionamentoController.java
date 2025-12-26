package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.PosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.RegistrarPosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitacaoPosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import br.gov.md.parla_md_backend.service.PosicionamentoService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final IUsuarioRepository usuarioRepository;

    /**
     * Solicita posicionamento a órgão externo
     */
    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Solicitar posicionamento a órgão externo",
            description = "Solicita posicionamento institucional a Ministério ou Força Armada")
    public ResponseEntity<PosicionamentoDTO> solicitarPosicionamento(
            @Valid @RequestBody SolicitacaoPosicionamentoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Solicitando posicionamento para processo {} ao órgão {}",
                dto.processoId(), dto.orgaoExternoId());

        String solicitanteId = extrairUsuarioId(userDetails);
        PosicionamentoDTO posicionamento = posicionamentoService.solicitarPosicionamento(dto, solicitanteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(posicionamento);
    }

    /**
     * Registra posicionamento recebido de órgão externo
     */
    @PutMapping("/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXTERNO')")
    @Operation(summary = "Registrar posicionamento recebido",
            description = "Registra posicionamento recebido de órgão externo")
    public ResponseEntity<PosicionamentoDTO> registrarPosicionamento(
            @Valid @RequestBody RegistrarPosicionamentoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Registrando posicionamento: {}", dto.posicionamentoId());

        String registradorId = extrairUsuarioId(userDetails);
        PosicionamentoDTO posicionamento = posicionamentoService.registrarPosicionamento(dto, registradorId);

        return ResponseEntity.ok(posicionamento);
    }

    /**
     * Consolida posicionamento recebido
     */
    @PutMapping("/{posicionamentoId}/consolidar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Consolidar posicionamento",
            description = "Consolida posicionamento que já foi recebido")
    public ResponseEntity<PosicionamentoDTO> consolidarPosicionamento(
            @PathVariable String posicionamentoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Consolidando posicionamento: {}", posicionamentoId);

        String consolidadorId = extrairUsuarioId(userDetails);
        PosicionamentoDTO posicionamento = posicionamentoService.consolidarPosicionamento(
                posicionamentoId,
                consolidadorId
        );

        return ResponseEntity.ok(posicionamento);
    }

    /**
     * Busca posicionamento por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamento por ID")
    public ResponseEntity<PosicionamentoDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando posicionamento: {}", id);

        PosicionamentoDTO posicionamento = posicionamentoService.buscarPorId(id);
        return ResponseEntity.ok(posicionamento);
    }

    /**
     * Busca posicionamento por número
     */
    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamento por número")
    public ResponseEntity<PosicionamentoDTO> buscarPorNumero(@PathVariable String numero) {
        log.debug("Buscando posicionamento por número: {}", numero);

        PosicionamentoDTO posicionamento = posicionamentoService.buscarPorNumero(numero);
        return ResponseEntity.ok(posicionamento);
    }

    /**
     * Busca posicionamentos de um processo
     */
    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamentos de um processo")
    public ResponseEntity<List<PosicionamentoDTO>> buscarPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando posicionamentos do processo: {}", processoId);

        List<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPorProcesso(processoId);
        return ResponseEntity.ok(posicionamentos);
    }

    /**
     * Busca posicionamentos pendentes por órgão
     */
    @GetMapping("/pendentes/orgao/{orgaoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar posicionamentos pendentes por órgão")
    public ResponseEntity<Page<PosicionamentoDTO>> buscarPendentesPorOrgao(
            @PathVariable String orgaoId,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando posicionamentos pendentes do órgão: {}", orgaoId);

        Page<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPendentesPorOrgao(
                orgaoId,
                pageable
        );
        return ResponseEntity.ok(posicionamentos);
    }

    /**
     * Busca posicionamentos pendentes de consolidação
     */
    @GetMapping("/pendentes/consolidacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar posicionamentos pendentes de consolidação")
    public ResponseEntity<Page<PosicionamentoDTO>> buscarPendentesConsolidacao(
            @PageableDefault(size = 20, sort = "dataRecebimento", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando posicionamentos pendentes de consolidação");

        Page<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPendentesConsolidacao(pageable);
        return ResponseEntity.ok(posicionamentos);
    }

    /**
     * Busca posicionamentos com prazo vencido
     */
    @GetMapping("/prazo-vencido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar posicionamentos com prazo vencido")
    public ResponseEntity<List<PosicionamentoDTO>> buscarComPrazoVencido() {
        log.debug("Buscando posicionamentos com prazo vencido");

        List<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarComPrazoVencido();
        return ResponseEntity.ok(posicionamentos);
    }

    /**
     * Busca estatísticas de posicionamentos por processo
     */
    @GetMapping("/estatisticas/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Estatísticas de posicionamentos do processo")
    public ResponseEntity<EstatisticasPosicionamentoDTO> buscarEstatisticasPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando estatísticas de posicionamentos do processo: {}", processoId);

        List<PosicionamentoDTO> posicionamentos = posicionamentoService.buscarPorProcesso(processoId);

        long total = posicionamentos.size();
        long pendentes = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.PENDENTE)
                .count();
        long recebidos = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.RECEBIDO)
                .count();
        long consolidados = posicionamentos.stream()
                .filter(p -> p.getStatus() == StatusPosicionamento.CONSOLIDADO)
                .count();
        long prazoVencido = posicionamentos.stream()
                .filter(p -> p.getPrazo() != null && p.getStatus() == StatusPosicionamento.PENDENTE)
                .filter(p -> java.time.LocalDateTime.now().isAfter(p.getPrazo()))
                .count();
        long atendidoPrazo = posicionamentos.stream()
                .filter(p -> p.getDataRecebimento() != null)
                .filter(PosicionamentoDTO::isAtendidoPrazo)
                .count();

        EstatisticasPosicionamentoDTO estatisticas = new EstatisticasPosicionamentoDTO(
                total,
                pendentes,
                recebidos,
                consolidados,
                prazoVencido,
                atendidoPrazo
        );

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Busca dashboard de posicionamentos por órgão
     */
    @GetMapping("/dashboard/orgao/{orgaoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Dashboard de posicionamentos do órgão")
    public ResponseEntity<DashboardOrgaoDTO> buscarDashboardOrgao(
            @PathVariable String orgaoId) {

        log.debug("Buscando dashboard do órgão: {}", orgaoId);

        Page<PosicionamentoDTO> page = posicionamentoService.buscarPendentesPorOrgao(
                orgaoId,
                Pageable.unpaged()
        );

        List<PosicionamentoDTO> pendentes = page.getContent();

        long totalPendentes = pendentes.size();
        long prazoVencido = pendentes.stream()
                .filter(p -> p.getPrazo() != null)
                .filter(p -> java.time.LocalDateTime.now().isAfter(p.getPrazo()))
                .count();
        long urgentes = pendentes.stream()
                .filter(p -> p.getPrazo() != null)
                .filter(p -> {
                    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
                    java.time.LocalDateTime limite = agora.plusDays(3);
                    return p.getPrazo().isAfter(agora) && p.getPrazo().isBefore(limite);
                })
                .count();

        DashboardOrgaoDTO dashboard = new DashboardOrgaoDTO(
                orgaoId,
                totalPendentes,
                prazoVencido,
                urgentes
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Extrai o ID do usuário do UserDetails
     */
    private String extrairUsuarioId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    /**
     * DTO para estatísticas de posicionamentos
     */
    public record EstatisticasPosicionamentoDTO(
            long total,
            long pendentes,
            long recebidos,
            long consolidados,
            long prazoVencido,
            long atendidoPrazo
    ) {}

    /**
     * DTO para dashboard do órgão
     */
    public record DashboardOrgaoDTO(
            String orgaoId,
            long totalPendentes,
            long prazoVencido,
            long urgentes
    ) {}
}