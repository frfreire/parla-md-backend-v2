package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.service.*;
import br.gov.md.parla_md_backend.service.ai.PredictionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.gov.md.parla_md_backend.service.ProposicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Optional;

@RestController
@RequestMapping("/api/public/legislative-data")
@Tag(name = "Dados Legislativos", description = "Operações relacionadas a dados legislativos")
public class DadosLegislativosController {

    private final CamaraService camaraService;
    private final SenadoService senadoService;
    private final ProposicaoService proposicaoService;
    private final PredictionService predictionService;
    private ProcedimentoProposicaoService procedimentoProposicaoService;
    private ProcedimentoMateriaService procedimentoMateriaService;

    private final ConcurrentHashMap<String, String> updateStatuses = new ConcurrentHashMap<>();

    public DadosLegislativosController(CamaraService camaraService,
                                       SenadoService senadoService,
                                       ProposicaoService proposicaoService,
                                       PredictionService predictionService,
                                       ProcedimentoProposicaoService procedimentoProposicaoService,
                                       ProcedimentoMateriaService procedimentoMateriaService
                                     ) {
        this.camaraService = camaraService;
        this.senadoService = senadoService;
        this.proposicaoService = proposicaoService;
        this.predictionService = predictionService;
        this.procedimentoProposicaoService = procedimentoProposicaoService;
        this.procedimentoMateriaService = procedimentoMateriaService;

    }

    @PostMapping("/fetch-camara")
    public ResponseEntity<List<Proposicao>> fetchCamaraData() {
        List<Proposicao> fetchedProposicaos = camaraService.fetchAndSavePropositions();
        return ResponseEntity.ok(fetchedProposicaos);
    }

    @GetMapping("/propositions")
    public ResponseEntity<List<Proposicao>> getAllPropositions() {
        List<Proposicao> proposicaos = proposicaoService.buscarTodasProposicoes();
        return ResponseEntity.ok(proposicaos);
    }

    @GetMapping("/propositions/{id}")
    @Operation(
            summary = "Buscar proposição por ID",
            description = "Retorna os detalhes de uma proposição específica com base no ID fornecido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposição encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Proposicao.class))),
            @ApiResponse(responseCode = "404", description = "Proposição não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    public ResponseEntity<Proposicao> buscarProposicaoPorId(@Parameter(description = "ID da proposição", required = true) @PathVariable String id) {
        try {
            Proposicao proposicao = proposicaoService.buscarProposicaoPorId(id);
            return ResponseEntity.ok(proposicao);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/propositions/theme/{theme}")
    public ResponseEntity<List<Proposicao>> buscaProposicaoPorTema(@PathVariable String theme) {
        List<Proposicao> proposicaos = proposicaoService.buscarProposicoesPorTema(theme);
        return ResponseEntity.ok(proposicaos);
    }

    @PostMapping("/propositions")
    public ResponseEntity<Proposicao> criarProposicao(@RequestBody Proposicao proposicao) {
        Proposicao savedProposicao = proposicaoService.salvarProposicao(proposicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProposicao);
    }

    @PutMapping("/propositions/{id}")
    public ResponseEntity<Proposicao> atualizarProposicao(@PathVariable String id, @RequestBody Proposicao proposicao) {
        try {
            proposicao.setId(id);
            Proposicao updatedProposicao = proposicaoService.atualizarProposicao(proposicao);
            return ResponseEntity.ok(updatedProposicao);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/propositions/{id}")
    public ResponseEntity<Void> excluirProposicao(@PathVariable String id) {
        try {
            proposicaoService.excluirProposicao(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/matters")
    public ResponseEntity<List<Materia>> buscarTodasMaterias(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer itens) {
        List<Materia> materias;
        if (ano != null && itens != null) {
            materias = senadoService.fetchAndSaveMatters(ano, itens);
        } else if (ano != null) {
            materias = senadoService.fetchAndSaveMatters(ano, senadoService.getDefaultItems());
        } else if (itens != null) {
            materias = senadoService.fetchAndSaveMatters(senadoService.getDefaultYear(), itens);
        } else {
            materias = senadoService.fetchAndSaveMatters();
        }
        return ResponseEntity.ok(materias);
    }

    @GetMapping("/propositions/{id}/procedures")
    public ResponseEntity<List<ProcedimentoProposicao>> getPropositionProcedures(@PathVariable String id) {
        try {
            List<ProcedimentoProposicao> procedimentoProposicaos = camaraService.fetchAndSaveProcedures(Integer.parseInt(id));
            return ResponseEntity.ok(procedimentoProposicaos);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/predict-approval")
    public ResponseEntity<Double> predictApproval(@RequestBody Proposicao proposicao) {
        double probability = predictionService.predictApprovalProbability(proposicao);
        return ResponseEntity.ok(probability);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("API is working!");
    }

    @PostMapping("/start-matter-update")
    public ResponseEntity<String> startMatterUpdate() {
        String updateId = "update-" + System.currentTimeMillis();
        updateStatuses.put(updateId, "Em andamento");

        CompletableFuture.runAsync(() -> {
            try {
                procedimentoMateriaService.scheduledUpdateAllMatterProcedures();
                updateStatuses.put(updateId, "Concluído");
            } catch (Exception e) {
                updateStatuses.put(updateId, "Erro: " + e.getMessage());
            }
        });

        return ResponseEntity.ok("Atualização iniciada. ID: " + updateId);
    }

    @GetMapping("/status-matter-update/{updateId}")
    public ResponseEntity<String> getUpdateStatus(@PathVariable String updateId) {
        String status = updateStatuses.get(updateId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/matter/{matterCode}")
    public ResponseEntity<String> updateSingleMatter(@PathVariable String matterCode) {
        try {
            Optional<Materia> optionalMatter = Optional.ofNullable(senadoService.findMatterById(matterCode));
            optionalMatter.ifPresentOrElse(
                    matter -> {
                        procedimentoMateriaService.fetchAndSaveProcedures(matter);
                        senadoService.updateMatter(matter);
                    },
                    () -> { throw new RuntimeException("Matéria não encontrada"); }
            );
            return ResponseEntity.ok("Atualização da matéria " + matterCode + " concluída.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar matéria: " + e.getMessage());
        }
    }

    @GetMapping("/last-update-matter")
    public ResponseEntity<String> getLastUpdateTime() {
        String lastUpdateTime = senadoService.getLastUpdateTime();
        return ResponseEntity.ok("Última atualização: " + lastUpdateTime);
    }
}