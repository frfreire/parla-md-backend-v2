package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.dto.CriarProcessoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import br.gov.md.parla_md_backend.service.ProcessoLegislativoService;
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
    private final IUsuarioRepository usuarioRepository;

    /**
     * Cria novo processo legislativo
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Criar novo processo legislativo",
            description = "Cria um novo processo agrupando proposições relacionadas")
    public ResponseEntity<ProcessoLegislativo> criar(
            @Valid @RequestBody CriarProcessoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Criando processo legislativo: {} por usuário: {}",
                dto.getNumeroProcesso(), userDetails.getUsername());

        String criadorId = extrairUsuarioId(userDetails);
        ProcessoLegislativo processo = processoService.criar(dto, criadorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(processo);
    }

    /**
     * Busca processo por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar processo por ID")
    public ResponseEntity<ProcessoLegislativoDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando processo: {}", id);

        ProcessoLegislativoDTO processo = processoService.buscarPorId(id);
        return ResponseEntity.ok(processo);
    }

    /**
     * Busca processo por número
     */
    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar processo por número")
    public ResponseEntity<ProcessoLegislativoDTO> buscarPorNumero(@PathVariable String numero) {
        log.debug("Buscando processo por número: {}", numero);

        ProcessoLegislativoDTO processo = processoService.buscarPorNumero(numero);
        return ResponseEntity.ok(processo);
    }

    /**
     * Lista todos os processos com paginação
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Listar todos os processos com paginação")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> listar(
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Listando processos - página: {}", pageable.getPageNumber());

        Page<ProcessoLegislativoDTO> processos = processoService.listar(pageable);
        return ResponseEntity.ok(processos);
    }

    /**
     * Busca processos por status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar processos por status")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> buscarPorStatus(
            @PathVariable StatusProcesso status,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando processos com status: {}", status);

        Page<ProcessoLegislativoDTO> processos = processoService.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(processos);
    }

    /**
     * Busca processos por setor responsável
     */
    @GetMapping("/setor/{setorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar processos por setor responsável")
    public ResponseEntity<Page<ProcessoLegislativoDTO>> buscarPorSetor(
            @PathVariable String setorId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando processos do setor: {}", setorId);

        Page<ProcessoLegislativoDTO> processos = processoService.buscarPorSetor(setorId, pageable);
        return ResponseEntity.ok(processos);
    }

    /**
     * Busca processos por gestor
     */
    @GetMapping("/gestor/{gestorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar processos por gestor responsável")
    public ResponseEntity<List<ProcessoLegislativoDTO>> buscarPorGestor(
            @PathVariable String gestorId) {

        log.debug("Buscando processos do gestor: {}", gestorId);

        List<ProcessoLegislativoDTO> processos = processoService.buscarPorGestor(gestorId);
        return ResponseEntity.ok(processos);
    }

    /**
     * Busca processos do usuário logado
     */
    @GetMapping("/meus-processos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Buscar processos do usuário logado")
    public ResponseEntity<List<ProcessoLegislativoDTO>> buscarMeusProcessos(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("Buscando processos do usuário: {}", userDetails.getUsername());

        String gestorId = extrairUsuarioId(userDetails);
        List<ProcessoLegislativoDTO> processos = processoService.buscarPorGestor(gestorId);
        return ResponseEntity.ok(processos);
    }

    /**
     * Atualiza status do processo
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Atualizar status do processo")
    public ResponseEntity<ProcessoLegislativo> atualizarStatus(
            @PathVariable String id,
            @RequestParam StatusProcesso novoStatus) {

        log.info("Atualizando status do processo {} para {}", id, novoStatus);

        ProcessoLegislativo processo = processoService.atualizarStatus(id, novoStatus);
        return ResponseEntity.ok(processo);
    }

    /**
     * Adiciona proposição ao processo
     */
    @PostMapping("/{processoId}/proposicoes/{proposicaoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Adicionar proposição ao processo")
    public ResponseEntity<Void> adicionarProposicao(
            @PathVariable String processoId,
            @PathVariable String proposicaoId) {

        log.info("Adicionando proposição {} ao processo {}", proposicaoId, processoId);

        processoService.adicionarProposicao(processoId, proposicaoId);
        return ResponseEntity.ok().build();
    }

    /**
     * Adiciona matéria ao processo
     */
    @PostMapping("/{processoId}/materias/{materiaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Adicionar matéria ao processo")
    public ResponseEntity<Void> adicionarMateria(
            @PathVariable String processoId,
            @PathVariable String materiaId) {

        log.info("Adicionando matéria {} ao processo {}", materiaId, processoId);

        processoService.adicionarMateria(processoId, materiaId);
        return ResponseEntity.ok().build();
    }

    /**
     * Busca estatísticas do processo
     */
    @GetMapping("/{processoId}/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar estatísticas do processo")
    public ResponseEntity<EstatisticasProcessoDTO> buscarEstatisticas(
            @PathVariable String processoId) {

        log.debug("Buscando estatísticas do processo: {}", processoId);

        ProcessoLegislativoDTO processo = processoService.buscarPorId(processoId);

        EstatisticasProcessoDTO estatisticas = new EstatisticasProcessoDTO(
                processo.getId(),
                processo.getNumero(),
                processo.getTitulo(),
                processo.getStatus(),
                processo.getPrioridade(),
                processo.getProposicaoIds() != null ? processo.getProposicaoIds().size() : 0,
                processo.getMateriaIds() != null ? processo.getMateriaIds().size() : 0,
                processo.getNumeroPareceresPendentes(),
                processo.getNumeroPosicionamentosPendentes(),
                processo.getDataCriacao(),
                processo.getDataUltimaAtualizacao()
        );

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Busca dashboard de processos do setor
     */
    @GetMapping("/dashboard/setor/{setorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Dashboard de processos do setor")
    public ResponseEntity<DashboardSetorDTO> buscarDashboardSetor(
            @PathVariable String setorId) {

        log.debug("Buscando dashboard do setor: {}", setorId);

        Page<ProcessoLegislativoDTO> page = processoService.buscarPorSetor(
                setorId,
                Pageable.unpaged()
        );

        List<ProcessoLegislativoDTO> processos = page.getContent();

        long total = processos.size();
        long emAndamento = processos.stream()
                .filter(p -> p.getStatus() == StatusProcesso.EM_ANDAMENTO)
                .count();
        long aguardandoParecer = processos.stream()
                .filter(p -> p.getNumeroPareceresPendentes() > 0)
                .count();
        long aguardandoPosicionamento = processos.stream()
                .filter(p -> p.getNumeroPosicionamentosPendentes() > 0)
                .count();
        long concluidos = processos.stream()
                .filter(p -> p.getStatus() == StatusProcesso.FINALIZADO)
                .count();

        DashboardSetorDTO dashboard = new DashboardSetorDTO(
                setorId,
                total,
                emAndamento,
                aguardandoParecer,
                aguardandoPosicionamento,
                concluidos
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
     * DTO para estatísticas do processo
     */
    public record EstatisticasProcessoDTO(
            String id,
            String numero,
            String titulo,
            StatusProcesso status,
            PrioridadeProcesso prioridade,
            int totalProposicoes,
            int totalMaterias,
            int pareceresPendentes,
            int posicionamentosPendentes,
            java.time.LocalDateTime dataCriacao,
            java.time.LocalDateTime dataUltimaAtualizacao
    ) {}

    /**
     * DTO para dashboard do setor
     */
    public record DashboardSetorDTO(
            String setorId,
            long totalProcessos,
            long emAndamento,
            long aguardandoParecer,
            long aguardandoPosicionamento,
            long concluidos
    ) {}
}
