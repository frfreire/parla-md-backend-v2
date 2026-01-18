package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.dto.ProposicaoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProposicaoResumoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcedimentoProposicaoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.exception.EntidadeNotFoundException;
import br.gov.md.parla_md_backend.exception.ValidacaoException;
import br.gov.md.parla_md_backend.service.CamaraService;
import br.gov.md.parla_md_backend.service.ProposicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/proposicoes")
@RequiredArgsConstructor
@Tag(name = "Proposições", description = "Gestão de proposições legislativas da Câmara dos Deputados")
@SecurityRequirement(name = "bearer-jwt")
public class ProposicaoController {

    private final CamaraService camaraService;
    private final ProposicaoService proposicaoService;

    @PostMapping("/sincronizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Sincronizar proposições com API da Câmara",
            description = "Busca e sincroniza proposições do ano atual da API da Câmara dos Deputados"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao sincronizar", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> sincronizar() {
        log.info("Solicitação de sincronização de proposições");
        List<ProposicaoDTO> proposicoes = camaraService.sincronizarProposicoes();
        return ResponseEntity.ok(proposicoes);
    }

    @PostMapping("/sincronizar/{ano}/{itens}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Sincronizar proposições por ano e quantidade",
            description = "Busca e sincroniza proposições de um ano específico limitando quantidade de itens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao sincronizar", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> sincronizarComParametros(
            @Parameter(description = "Ano das proposições") @PathVariable Integer ano,
            @Parameter(description = "Quantidade máxima de itens") @PathVariable Integer itens) {
        log.info("Solicitação de sincronização - ano: {}, itens: {}", ano, itens);
        List<ProposicaoDTO> proposicoes = camaraService.sincronizarProposicoes(ano, itens);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Listar todas as proposições",
            description = "Retorna lista completa de proposições cadastradas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> listarTodas() {
        log.debug("Solicitação de listagem de todas as proposições");
        List<ProposicaoDTO> proposicoes = camaraService.buscarTodasProposicoes();
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Listar resumo das proposições",
            description = "Retorna lista resumida de proposições (campos principais apenas)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoResumoDTO>> listarResumo() {
        log.debug("Solicitação de resumo de proposições");
        List<ProposicaoResumoDTO> proposicoes = camaraService.buscarTodasProposicoesResumo();
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposição por ID",
            description = "Retorna proposição específica pelo ID interno do sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposição encontrada"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ProposicaoDTO> buscarPorId(
            @Parameter(description = "ID da proposição") @PathVariable String id) {
        log.debug("Buscando proposição por ID: {}", id);
        ProposicaoDTO proposicao = camaraService.buscarPorId(id);
        return ResponseEntity.ok(proposicao);
    }

    @GetMapping("/ano/{ano}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposições por ano",
            description = "Retorna proposições de um ano específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> buscarPorAno(
            @Parameter(description = "Ano das proposições") @PathVariable Integer ano) {
        log.debug("Buscando proposições do ano: {}", ano);
        List<ProposicaoDTO> proposicoes = camaraService.buscarPorAno(ano);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/tipo/{siglaTipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposições por tipo",
            description = "Retorna proposições de um tipo específico (PL, PEC, etc.)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> buscarPorTipo(
            @Parameter(description = "Sigla do tipo de proposição") @PathVariable String siglaTipo) {
        log.debug("Buscando proposições do tipo: {}", siglaTipo);
        List<ProposicaoDTO> proposicoes = camaraService.buscarPorSiglaTipo(siglaTipo);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/autor/{autorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposições por autor",
            description = "Retorna proposições de um autor específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> buscarPorAutor(
            @Parameter(description = "ID do autor") @PathVariable Long autorId) {
        log.debug("Buscando proposições do autor: {}", autorId);
        List<ProposicaoDTO> proposicoes = camaraService.buscarPorAutor(autorId);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/tema/{tema}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposições por tema",
            description = "Retorna proposições de um tema específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> buscarPorTema(
            @Parameter(description = "Tema das proposições") @PathVariable String tema) {
        log.debug("Buscando proposições do tema: {}", tema);
        List<ProposicaoDTO> proposicoes = camaraService.buscarPorTema(tema);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/triagem/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar proposições por status de triagem",
            description = "Retorna proposições com status de triagem específico (paginado)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Page<ProposicaoDTO>> buscarPorStatusTriagem(
            @Parameter(description = "Status de triagem") @PathVariable StatusTriagem status,
            Pageable pageable) {
        log.debug("Buscando proposições com status de triagem: {}", status);
        Page<ProposicaoDTO> proposicoes = camaraService.buscarPorStatusTriagem(status, pageable);
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/{id}/procedimentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Listar procedimentos de uma proposição",
            description = "Retorna histórico de tramitação (procedimentos) de uma proposição"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProcedimentoProposicaoDTO>> listarProcedimentos(
            @Parameter(description = "ID da proposição") @PathVariable String id) {
        log.debug("Buscando procedimentos da proposição: {}", id);
        List<ProcedimentoProposicaoDTO> procedimentos =
                camaraService.buscarProcedimentosPorProposicao(id);
        return ResponseEntity.ok(procedimentos);
    }

    @PostMapping("/{idCamara}/sincronizar-procedimentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Sincronizar procedimentos de uma proposição",
            description = "Busca e sincroniza histórico de tramitação da API da Câmara"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro ao sincronizar", content = @Content)
    })
    public ResponseEntity<List<ProcedimentoProposicaoDTO>> sincronizarProcedimentos(
            @Parameter(description = "ID da proposição na API da Câmara")
            @PathVariable Long idCamara) {
        log.info("Solicitação de sincronização de procedimentos - ID Câmara: {}", idCamara);
        List<ProcedimentoProposicaoDTO> procedimentos =
                camaraService.sincronizarProcedimentos(idCamara);
        return ResponseEntity.ok(procedimentos);
    }

    @GetMapping("/estatisticas/ano/{ano}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Contar proposições por ano",
            description = "Retorna quantidade de proposições de um ano específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Long> contarPorAno(
            @Parameter(description = "Ano das proposições") @PathVariable Integer ano) {
        log.debug("Contando proposições do ano: {}", ano);
        long count = camaraService.contarPorAno(ano);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/estatisticas/tipo/{siglaTipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Contar proposições por tipo",
            description = "Retorna quantidade de proposições de um tipo específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Long> contarPorTipo(
            @Parameter(description = "Sigla do tipo") @PathVariable String siglaTipo) {
        log.debug("Contando proposições do tipo: {}", siglaTipo);
        long count = camaraService.contarPorTipo(siglaTipo);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(
            summary = "Buscar proposições por período",
            description = "Retorna proposições apresentadas entre duas datas específicas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<ProposicaoDTO>> buscarPorPeriodo(
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        log.debug("Buscando proposições entre {} e {}", inicio, fim);

        if (inicio.isAfter(fim)) {
            log.warn("Data inicial posterior à data final: {} > {}", inicio, fim);
            return ResponseEntity.badRequest().build();
        }

        List<Proposicao> proposicoes = proposicaoService.buscarPorPeriodo(inicio, fim);

        List<ProposicaoDTO> dtos = proposicoes.stream()
                .map(ProposicaoDTO::fromEntity)
                .toList();

        log.debug("Retornadas {} proposições no período", dtos.size());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Atualizar proposição",
            description = "Atualiza dados de uma proposição existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposição atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ProposicaoDTO> atualizarProposicao(
            @Parameter(description = "ID da proposição") @PathVariable String id,
            @RequestBody ProposicaoDTO dto) {

        log.info("Atualizando proposição: {}", id);

        try {
            Proposicao proposicao = dto.toEntity();

            Proposicao atualizada = proposicaoService.atualizar(id, proposicao);

            log.info("Proposição {} atualizada com sucesso", id);

            return ResponseEntity.ok(ProposicaoDTO.fromEntity(atualizada));

        } catch (EntidadeNotFoundException e) {
            log.warn("Proposição não encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (ValidacaoException e) {
            log.warn("Dados inválidos para atualização: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Excluir proposição",
            description = "Remove permanentemente uma proposição do sistema (apenas ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proposição excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Void> excluirProposicao(
            @Parameter(description = "ID da proposição") @PathVariable String id) {

        log.info("Excluindo proposição: {}", id);

        try {
            proposicaoService.excluir(id);

            log.info("Proposição {} excluída com sucesso", id);

            return ResponseEntity.noContent().build();

        } catch (EntidadeNotFoundException e) {
            log.warn("Proposição não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status-triagem")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Atualizar status de triagem",
            description = "Atualiza apenas o status de triagem de uma proposição"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ProposicaoDTO> atualizarStatusTriagem(
            @Parameter(description = "ID da proposição") @PathVariable String id,
            @Parameter(description = "Novo status de triagem", required = true)
            @RequestParam StatusTriagem status) {

        log.info("Atualizando status de triagem da proposição {} para {}", id, status);

        try {
            Proposicao atualizada = proposicaoService.atualizarStatusTriagem(id, status);

            log.info("Status de triagem atualizado - Proposição: {}, Novo status: {}", id, status);

            return ResponseEntity.ok(ProposicaoDTO.fromEntity(atualizada));

        } catch (EntidadeNotFoundException e) {
            log.warn("Proposição não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}