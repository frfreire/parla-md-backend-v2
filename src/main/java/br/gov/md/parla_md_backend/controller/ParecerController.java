package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.dto.AprovarParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.EmitirParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.ParecerDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarParecerDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import br.gov.md.parla_md_backend.service.ParecerService;
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

@Slf4j
@RestController
@RequestMapping("/api/pareceres")
@RequiredArgsConstructor
public class ParecerController {

    private final ParecerService parecerService;
    private final IUsuarioRepository usuarioRepository;

    /**
     * Solicita parecer de um setor
     */
    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<ParecerDTO> solicitarParecer(
            @Valid @RequestBody SolicitarParecerDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Solicitação de parecer recebida do usuário: {}", userDetails.getUsername());

        String solicitanteId = extrairUsuarioId(userDetails);
        ParecerDTO parecer = parecerService.solicitarParecer(dto, solicitanteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(parecer);
    }

    /**
     * Emite parecer (analista)
     */
    @PutMapping("/emitir")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    public ResponseEntity<ParecerDTO> emitirParecer(
            @Valid @RequestBody EmitirParecerDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Emissão de parecer {} pelo usuário: {}", dto.parecerId(), userDetails.getUsername());

        String analistaId = extrairUsuarioId(userDetails);
        String analistaNome = buscarNomeUsuario(analistaId);

        ParecerDTO parecer = parecerService.emitirParecer(dto, analistaId, analistaNome);

        return ResponseEntity.ok(parecer);
    }

    /**
     * Aprova ou rejeita parecer (superior hierárquico)
     */
    @PutMapping("/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<ParecerDTO> aprovarParecer(
            @Valid @RequestBody AprovarParecerDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Aprovação de parecer {} pelo usuário: {}", dto.parecerId(), userDetails.getUsername());

        String aprovadorId = extrairUsuarioId(userDetails);
        String aprovadorNome = buscarNomeUsuario(aprovadorId);

        ParecerDTO parecer = parecerService.aprovarParecer(dto, aprovadorId, aprovadorNome);

        return ResponseEntity.ok(parecer);
    }

    /**
     * Busca parecer por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    public ResponseEntity<ParecerDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando parecer: {}", id);

        ParecerDTO parecer = parecerService.buscarPorId(id);
        return ResponseEntity.ok(parecer);
    }

    /**
     * Busca pareceres de um processo
     */
    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    public ResponseEntity<List<ParecerDTO>> buscarPorProcesso(@PathVariable String processoId) {
        log.debug("Buscando pareceres do processo: {}", processoId);

        List<ParecerDTO> pareceres = parecerService.buscarPorProcesso(processoId);
        return ResponseEntity.ok(pareceres);
    }

    /**
     * Busca pareceres pendentes de emissão de um setor
     */
    @GetMapping("/pendentes/setor/{setorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    public ResponseEntity<Page<ParecerDTO>> buscarPendentesPorSetor(
            @PathVariable String setorId,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando pareceres pendentes do setor: {}", setorId);

        Page<ParecerDTO> pareceres = parecerService.buscarPendentesPorSetor(setorId, pageable);
        return ResponseEntity.ok(pareceres);
    }

    /**
     * Busca pareceres emitidos pendentes de aprovação
     */
    @GetMapping("/pendentes/aprovacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Page<ParecerDTO>> buscarPendentesAprovacao(
            @PageableDefault(size = 20, sort = "dataEmissao", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando pareceres pendentes de aprovação");

        Page<ParecerDTO> pareceres = parecerService.buscarPendentesAprovacao(pageable);
        return ResponseEntity.ok(pareceres);
    }

    /**
     * Busca pareceres com prazo vencido
     */
    @GetMapping("/prazo-vencido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<List<ParecerDTO>> buscarComPrazoVencido() {
        log.debug("Buscando pareceres com prazo vencido");

        List<ParecerDTO> pareceres = parecerService.buscarComPrazoVencido();
        return ResponseEntity.ok(pareceres);
    }

    /**
     * Busca pareceres pendentes do usuário logado
     */
    @GetMapping("/meus-pendentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    public ResponseEntity<Page<ParecerDTO>> buscarMeusPendentes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Buscando pareceres pendentes do usuário: {}", userDetails.getUsername());

        String usuarioId = extrairUsuarioId(userDetails);
        Usuario usuario = buscarUsuario(usuarioId);

        Page<ParecerDTO> pareceres = parecerService.buscarPendentesPorSetor(usuario.getSetorId(), pageable);
        return ResponseEntity.ok(pareceres);
    }

    /**
     * Busca estatísticas de pareceres por processo
     */
    @GetMapping("/estatisticas/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<EstatisticasParecerDTO> buscarEstatisticasPorProcesso(
            @PathVariable String processoId) {

        log.debug("Buscando estatísticas de pareceres do processo: {}", processoId);

        List<ParecerDTO> pareceres = parecerService.buscarPorProcesso(processoId);

        long total = pareceres.size();
        long pendentes = pareceres.stream()
                .filter(p -> p.dataEmissao() == null)
                .count();
        long emitidos = pareceres.stream()
                .filter(p -> p.dataEmissao() != null && p.dataAprovacao() == null)
                .count();
        long aprovados = pareceres.stream()
                .filter(p -> p.dataAprovacao() != null)
                .count();
        long prazoVencido = pareceres.stream()
                .filter(p -> p.prazo() != null && p.dataEmissao() == null)
                .filter(p -> java.time.LocalDateTime.now().isAfter(p.prazo()))
                .count();
        long atendidoPrazo = pareceres.stream()
                .filter(p -> p.dataEmissao() != null)
                .filter(ParecerDTO::atendidoPrazo)
                .count();

        EstatisticasParecerDTO estatisticas = new EstatisticasParecerDTO(
                total,
                pendentes,
                emitidos,
                aprovados,
                prazoVencido,
                atendidoPrazo
        );

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Busca estatísticas gerais de pareceres do setor
     */
    @GetMapping("/estatisticas/setor/{setorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<EstatisticasSetorDTO> buscarEstatisticasPorSetor(
            @PathVariable String setorId) {

        log.debug("Buscando estatísticas do setor: {}", setorId);

        // Buscar todos os pareceres do setor (sem paginação para estatísticas)
        Page<ParecerDTO> page = parecerService.buscarPendentesPorSetor(
                setorId,
                Pageable.unpaged()
        );

        List<ParecerDTO> pareceres = page.getContent();

        long totalPendentes = pareceres.size();
        long prazoVencido = pareceres.stream()
                .filter(p -> p.prazo() != null)
                .filter(p -> java.time.LocalDateTime.now().isAfter(p.prazo()))
                .count();
        long urgentes = pareceres.stream()
                .filter(p -> p.prazo() != null)
                .filter(p -> {
                    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
                    java.time.LocalDateTime limite = agora.plusDays(3);
                    return p.prazo().isAfter(agora) && p.prazo().isBefore(limite);
                })
                .count();

        EstatisticasSetorDTO estatisticas = new EstatisticasSetorDTO(
                totalPendentes,
                prazoVencido,
                urgentes
        );

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Extrai o ID do usuário do UserDetails
     */
    private String extrairUsuarioId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    /**
     * Busca o nome do usuário pelo ID
     */
    private String buscarNomeUsuario(String usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return usuario.getNome();
    }

    /**
     * Busca usuário completo
     */
    private Usuario buscarUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    /**
     * DTO para estatísticas de pareceres de um processo
     */
    public record EstatisticasParecerDTO(
            long total,
            long pendentes,
            long emitidos,
            long aprovados,
            long prazoVencido,
            long atendidoPrazo
    ) {}

    /**
     * DTO para estatísticas de pareceres de um setor
     */
    public record EstatisticasSetorDTO(
            long totalPendentes,
            long prazoVencido,
            long urgentes
    ) {}
}