package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.dto.MateriaDTO;
import br.gov.md.parla_md_backend.domain.dto.PrevisaoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcedimentoProposicaoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarPrevisaoDTO;
import br.gov.md.parla_md_backend.service.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/dados-legislativos")
@RequiredArgsConstructor
@Tag(name = "Dados Legislativos", description = "Operações relacionadas a dados legislativos")
@SecurityRequirement(name = "bearer-key")
public class DadosLegislativosController {

    private final CamaraService camaraService;
    private final SenadoService senadoService;
    private final ProposicaoService proposicaoService;
    private final PrevisaoService previsaoService;
    private final ProcedimentoProposicaoService procedimentoProposicaoService;
    private final ProcedimentoMateriaService procedimentoMateriaService;

    private final ConcurrentHashMap<String, String> updateStatuses = new ConcurrentHashMap<>();

    @PostMapping("/camara/sincronizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Sincronizar proposições da Câmara",
            description = "Busca e salva proposições mais recentes da API da Câmara dos Deputados"
    )
    public ResponseEntity<String> sincronizarProposicoesCamara() {
        try {
            camaraService.sincronizarProposicoes();

            return ResponseEntity.ok("Sincronização de proposições iniciada");

        } catch (Exception e) {
            log.error("Erro ao sincronizar proposições da Câmara: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/proposicoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar todas as proposições")
    public ResponseEntity<List<Proposicao>> listarProposicoes() {
        List<Proposicao> proposicoes = proposicaoService.buscarTodas();
        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/proposicoes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar proposição por ID",
            description = "Retorna detalhes de uma proposição específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposição encontrada",
                    content = @Content(schema = @Schema(implementation = Proposicao.class))),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content)
    })
    public ResponseEntity<Proposicao> buscarProposicao(
            @Parameter(description = "ID da proposição") @PathVariable String id) {

        try {
            Proposicao proposicao = proposicaoService.buscarPorId(id);
            return ResponseEntity.ok(proposicao);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/proposicoes/tema/{tema}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar proposições por tema")
    public ResponseEntity<List<Proposicao>> buscarPorTema(@PathVariable String tema) {
        List<Proposicao> proposicoes = proposicaoService.buscarPorTema(tema);
        return ResponseEntity.ok(proposicoes);
    }

    @PostMapping("/proposicoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Criar nova proposição")
    public ResponseEntity<Proposicao> criarProposicao(@RequestBody Proposicao proposicao) {
        Proposicao salva = proposicaoService.salvar(proposicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @PutMapping("/proposicoes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Atualizar proposição")
    public ResponseEntity<Proposicao> atualizarProposicao(
            @PathVariable String id,
            @RequestBody Proposicao proposicao) {

        try {
            Proposicao atualizada = proposicaoService.atualizar(id, proposicao);
            return ResponseEntity.ok(atualizada);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/proposicoes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir proposição")
    public ResponseEntity<Void> excluirProposicao(@PathVariable String id) {
        try {
            proposicaoService.excluir(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/proposicoes/{id}/procedimentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar tramitações de uma proposição")
    public ResponseEntity<List<ProcedimentoProposicaoDTO>> buscarProcedimentos(
            @PathVariable String id) {

        try {
            List<ProcedimentoProposicaoDTO> procedimentos =
                    camaraService.buscarProcedimentosPorProposicao(id);

            return ResponseEntity.ok(procedimentos);

        } catch (Exception e) {
            log.error("Erro ao buscar procedimentos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/senado/sincronizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Sincronizar matérias do Senado",
            description = "Busca e salva matérias mais recentes da API do Senado Federal"
    )
    public ResponseEntity<Map<String, Object>> sincronizarMateriasSenado(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer itens) {

        try {
            List<MateriaDTO> materias;

            if (ano != null && itens != null) {
                materias = senadoService.buscarESalvarMaterias(ano, itens);
            } else if (ano != null) {
                materias = senadoService.buscarESalvarMaterias(ano, 100);
            } else {
                materias = senadoService.buscarESalvarMaterias();
            }

            return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "mensagem", "Sincronização de matérias concluída",
                    "materiasProcessadas", materias.size()
            ));

        } catch (Exception e) {
            log.error("Erro ao sincronizar matérias do Senado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "erro",
                    "mensagem", "Erro: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/materias")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar matérias do Senado")
    public ResponseEntity<List<Materia>> listarMaterias() {
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/proposicoes/{id}/prever")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Prever probabilidade de aprovação",
            description = "Usa IA (Llama) para prever probabilidade de aprovação de uma proposição"
    )
    public ResponseEntity<PrevisaoDTO> preverAprovacao(@PathVariable String id) {
        try {
            SolicitarPrevisaoDTO solicitacao = SolicitarPrevisaoDTO.builder()
                    .itemLegislativoId(id)
                    .tipoPrevisao("APROVACAO")
                    .forcarNovaPrevisao(false)
                    .build();

            PrevisaoDTO previsao = previsaoService.prever(solicitacao);

            return ResponseEntity.ok(previsao);

        } catch (Exception e) {
            log.error("Erro ao prever aprovação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/materias/atualizar-procedimentos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar procedimentos de todas as matérias (async)")
    public ResponseEntity<String> atualizarProcedimentosMaterias() {
        String updateId = "update-" + System.currentTimeMillis();
        updateStatuses.put(updateId, "Em andamento");

        CompletableFuture.runAsync(() -> {
            try {
                procedimentoMateriaService.atualizarTodasTramitacoesAgendadas();
                updateStatuses.put(updateId, "Concluído");

            } catch (Exception e) {
                log.error("Erro na atualização em lote: {}", e.getMessage());
                updateStatuses.put(updateId, "Erro: " + e.getMessage());
            }
        });

        return ResponseEntity.ok("Atualização iniciada. ID: " + updateId);
    }

    @GetMapping("/status-atualizacao/{updateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Consultar status de atualização em lote")
    public ResponseEntity<String> consultarStatus(@PathVariable String updateId) {
        String status = updateStatuses.get(updateId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/teste")
    @Operation(summary = "Testar disponibilidade da API")
    public ResponseEntity<String> testar() {
        return ResponseEntity.ok("API Parla-MD operacional!");
    }
}