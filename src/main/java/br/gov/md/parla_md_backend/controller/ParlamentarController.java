package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.dto.AnaliseParlamentarDTO;
import br.gov.md.parla_md_backend.domain.dto.ComportamentoParlamentarDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitarAnaliseParlamentarDTO;
import br.gov.md.parla_md_backend.service.AnaliseParlamentarService;
import br.gov.md.parla_md_backend.service.ParlamentarService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/parlamentares")
@RequiredArgsConstructor
@Tag(name = "Parlamentares", description = "Operações relacionadas a parlamentares")
@SecurityRequirement(name = "bearer-key")
public class ParlamentarController {

    private final AnaliseParlamentarService analiseParlamentarService;
    private final ParlamentarService parlamentarService;

    @GetMapping("/{id}/comportamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Obter comportamento de votação",
            description = "Retorna estatísticas de comportamento de votação do parlamentar"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comportamento retornado",
                    content = @Content(schema = @Schema(implementation = ComportamentoParlamentarDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parlamentar não encontrado", content = @Content)
    })
    public ResponseEntity<ComportamentoParlamentarDTO> obterComportamento(
            @Parameter(description = "ID do parlamentar") @PathVariable String id,
            @Parameter(description = "Tema (opcional)") @RequestParam(required = false) String tema,
            @Parameter(description = "Data início (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data fim (opcional)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        log.debug("Obtendo comportamento: parlamentar={}, tema={}", id, tema);

        ComportamentoParlamentarDTO comportamento = analiseParlamentarService
                .obterComportamento(id, tema, inicio, fim);

        return ResponseEntity.ok(comportamento);
    }

    @PostMapping("/{id}/analisar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Analisar comportamento parlamentar",
            description = "Gera análise de comportamento de votação usando IA (Llama)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Análise gerada",
                    content = @Content(schema = @Schema(implementation = AnaliseParlamentarDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parlamentar não encontrado", content = @Content),
            @ApiResponse(responseCode = "503", description = "Llama indisponível", content = @Content)
    })
    public ResponseEntity<AnaliseParlamentarDTO> analisarComportamento(
            @Parameter(description = "ID do parlamentar") @PathVariable String id,
            @Parameter(description = "Tema a ser analisado", required = true)
            @RequestParam String tema,
            @Parameter(description = "Forçar nova análise (ignorar cache)")
            @RequestParam(defaultValue = "false") boolean forcarNovaAnalise,
            @Parameter(description = "Incluir tendências na análise")
            @RequestParam(defaultValue = "true") boolean incluirTendencias,
            @Parameter(description = "Incluir previsões na análise")
            @RequestParam(defaultValue = "true") boolean incluirPrevisoes) {

        log.info("Solicitando análise parlamentar: parlamentar={}, tema={}", id, tema);

        SolicitarAnaliseParlamentarDTO request = SolicitarAnaliseParlamentarDTO.builder()
                .parlamentarId(id)
                .tema(tema)
                .forcarNovaAnalise(forcarNovaAnalise)
                .incluirTendencias(incluirTendencias)
                .incluirPrevisoes(incluirPrevisoes)
                .build();

        AnaliseParlamentarDTO analise = analiseParlamentarService.analisar(request);

        return ResponseEntity.ok(analise);
    }

    @GetMapping("/{id}/analises")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar análises de um parlamentar",
            description = "Retorna todas as análises geradas para o parlamentar"
    )
    public ResponseEntity<List<AnaliseParlamentarDTO>> listarAnalises(
            @Parameter(description = "ID do parlamentar") @PathVariable String id) {

        log.debug("Listando análises do parlamentar: {}", id);

        List<AnaliseParlamentarDTO> analises = analiseParlamentarService
                .buscarAnalisesPorParlamentar(id);

        return ResponseEntity.ok(analises);
    }

    @GetMapping("/{id}/analise/{tema}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar análise específica",
            description = "Retorna análise de um parlamentar sobre tema específico"
    )
    public ResponseEntity<AnaliseParlamentarDTO> buscarAnalisePorTema(
            @Parameter(description = "ID do parlamentar") @PathVariable String id,
            @Parameter(description = "Tema da análise") @PathVariable String tema) {

        log.debug("Buscando análise: parlamentar={}, tema={}", id, tema);

        AnaliseParlamentarDTO analise = analiseParlamentarService
                .buscarAnalisePorParlamentarETema(id, tema);

        return ResponseEntity.ok(analise);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Listar todos os parlamentares (paginado)")
    public ResponseEntity<Page<Parlamentar>> listarTodos(Pageable pageable) {
        log.debug("Listando parlamentares - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Parlamentar> parlamentares = parlamentarService.getAllParlamentarians(pageable);
        return ResponseEntity.ok(parlamentares);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO')")
    @Operation(summary = "Buscar parlamentar por ID com informações completas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parlamentar encontrado"),
            @ApiResponse(responseCode = "404", description = "Parlamentar não encontrado", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Long id) {
        log.debug("Buscando parlamentar: {}", id);

        try {
            Parlamentar parlamentar = parlamentarService.getParlamentarianInfo(id.toString());
            List<Proposicao> proposicoes = parlamentarService.getPropositionsByParlamentarian(id);
            String posicionamento = parlamentarService.getPositionAboutSpecificThemes(id.toString());

            Map<String, Object> response = new HashMap<>();
            response.put("parlamentar", parlamentar);
            response.put("proposicoes", proposicoes);
            response.put("posicionamento", posicionamento);
            response.put("foto", parlamentar.getUrlFoto());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar parlamentar {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/sincronizar/deputados")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Sincronizar deputados",
            description = "Busca e salva deputados da API da Câmara dos Deputados"
    )
    public ResponseEntity<List<Parlamentar>> sincronizarDeputados() {
        log.info("Sincronizando deputados da Câmara");

        List<Parlamentar> deputados = parlamentarService.fetchAndSaveDeputados();

        log.info("Total de {} deputados sincronizados", deputados.size());
        return ResponseEntity.ok(deputados);
    }

    @PostMapping("/sincronizar/senadores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Sincronizar senadores",
            description = "Busca e salva senadores da API do Senado Federal"
    )
    public ResponseEntity<List<Parlamentar>> sincronizarSenadores() {
        log.info("Sincronizando senadores do Senado");

        List<Parlamentar> senadores = parlamentarService.fetchAndSaveSenadores();

        log.info("Total de {} senadores sincronizados", senadores.size());
        return ResponseEntity.ok(senadores);
    }
}