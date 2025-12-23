package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.parecer.Recomendacao;
import br.gov.md.parla_md_backend.domain.dto.ParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarParecerDTO;
import br.gov.md.parla_md_backend.service.ParecerService;
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
 * Controller para gerenciamento de pareceres técnicos
 */
@Slf4j
@RestController
@RequestMapping("/api/pareceres")
@RequiredArgsConstructor
@Tag(name = "Pareceres", description = "Endpoints para gerenciamento de pareceres técnicos internos")
@SecurityRequirement(name = "bearer-jwt")
public class ParecerController {

    private final ParecerService parecerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Solicitar parecer a um setor",
            description = "Solicita parecer técnico a setor interno do MD")
    public ResponseEntity<ParecerDTO> solicitarParecer(
            @Valid @RequestBody SolicitarParecerDTO dto,
            Authentication authentication) {

        log.info("Solicitando parecer para processo {} ao setor {}",
                dto.getProcessoId(), dto.getSetorEmissorId());

        String solicitanteId = authentication.getName();
        ParecerDTO parecer = parecerService.solicitarParecer(dto, solicitanteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(parecer);
    }

    @PutMapping("/{parecerId}/emitir")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Emitir parecer",
            description = "Emite parecer técnico sobre processo")
    public ResponseEntity<ParecerDTO> emitirParecer(
            @PathVariable String parecerId,
            @RequestParam String contexto,
            @RequestParam String analise,
            @RequestParam Recomendacao recomendacao,
            @RequestParam String justificativa,
            @RequestParam(required = false) List<String> fundamentacaoLegal,
            @RequestParam(required = false) List<String> impactosIdentificados,
            @RequestParam String conclusao,
            Authentication authentication) {

        log.info("Emitindo parecer {} por usuário: {}",
                parecerId, authentication.getName());

        String analistaId = authentication.getName();
        String analistaNome = authentication.getName();

        ParecerDTO parecer = parecerService.emitirParecer(
                parecerId, analistaId, analistaNome, contexto, analise,
                recomendacao, justificativa, fundamentacaoLegal,
                impactosIdentificados, conclusao);

        return ResponseEntity.ok(parecer);
    }

    @PutMapping("/{parecerId}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Aprovar parecer",
            description = "Aprova parecer emitido (superior hierárquico)")
    public ResponseEntity<ParecerDTO> aprovarParecer(
            @PathVariable String parecerId,
            Authentication authentication) {

        log.info("Aprovando parecer {} por usuário: {}",
                parecerId, authentication.getName());

        String aprovadorId = authentication.getName();
        String aprovadorNome = authentication.getName();

        ParecerDTO parecer = parecerService.aprovarParecer(
                parecerId, aprovadorId, aprovadorNome);

        return ResponseEntity.ok(parecer);
    }

    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR', 'EXTERNO')")
    @Operation(summary = "Buscar pareceres de um processo")
    public ResponseEntity<List<ParecerDTO>> buscarPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando pareceres do processo: {}", processoId);

        List<ParecerDTO> pareceres = parecerService.buscarPorProcesso(processoId);
        return ResponseEntity.ok(pareceres);
    }

    @GetMapping("/setor/{setorId}/pendentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar pareceres pendentes de um setor")
    public ResponseEntity<Page<ParecerDTO>> buscarPendentesPorSetor(
            @PathVariable String setorId,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando pareceres pendentes do setor: {}", setorId);

        Page<ParecerDTO> pareceres = parecerService.buscarPendentesPorSetor(
                setorId, pageable);
        return ResponseEntity.ok(pareceres);
    }

    @GetMapping("/pendentes-aprovacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar pareceres pendentes de aprovação")
    public ResponseEntity<Page<ParecerDTO>> buscarPendentesAprovacao(
            @PageableDefault(size = 20, sort = "dataEmissao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando pareceres pendentes de aprovação");

        Page<ParecerDTO> pareceres = parecerService.buscarPendentesAprovacao(pageable);
        return ResponseEntity.ok(pareceres);
    }

    @GetMapping("/prazo-vencido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar pareceres com prazo vencido")
    public ResponseEntity<List<ParecerDTO>> buscarComPrazoVencido() {
        log.debug("Buscando pareceres com prazo vencido");

        List<ParecerDTO> pareceres = parecerService.buscarComPrazoVencido();
        return ResponseEntity.ok(pareceres);
    }
}