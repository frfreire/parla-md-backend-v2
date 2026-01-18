package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.AreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.AtualizarAreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.CriarAreaImpactoDTO;
import br.gov.md.parla_md_backend.service.AreaImpactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/areas-impacto")
@RequiredArgsConstructor
@Tag(name = "Áreas de Impacto", description = "Gerenciamento de áreas de impacto para análise legislativa")
@SecurityRequirement(name = "bearer-jwt")
public class AreaImpactoController {

    private final AreaImpactoService areaImpactoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Listar todas as áreas de impacto",
            description = "Retorna todas as áreas de impacto cadastradas com paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas com sucesso")
    })
    public ResponseEntity<Page<AreaImpactoDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Listando todas as áreas de impacto");

        Page<AreaImpactoDTO> areas = areaImpactoService.listarTodas(pageable);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Listar áreas ativas",
            description = "Retorna apenas áreas de impacto ativas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarAtivas() {

        log.debug("Listando áreas de impacto ativas");

        List<AreaImpactoDTO> areas = areaImpactoService.listarAtivas();

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/inativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Listar áreas inativas",
            description = "Retorna apenas áreas de impacto inativas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarInativas() {

        log.debug("Listando áreas de impacto inativas");

        List<AreaImpactoDTO> areas = areaImpactoService.listarInativas();

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/ativas/paginadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Listar áreas ativas com paginação",
            description = "Retorna áreas ativas com suporte a paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<Page<AreaImpactoDTO>> listarAtivasPaginadas(
            @PageableDefault(size = 20, sort = "ordem", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Listando áreas ativas paginadas");

        Page<AreaImpactoDTO> areas = areaImpactoService.listarAtivasPaginadas(pageable);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/inativas/paginadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Listar áreas inativas com paginação",
            description = "Retorna áreas inativas com suporte a paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<Page<AreaImpactoDTO>> listarInativasPaginadas(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Listando áreas inativas paginadas");

        Page<AreaImpactoDTO> areas = areaImpactoService.listarInativasPaginadas(pageable);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/ordenadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Listar áreas ordenadas",
            description = "Retorna todas as áreas ordenadas por campo 'ordem'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarTodasOrdenadas() {

        log.debug("Listando áreas ordenadas");

        List<AreaImpactoDTO> areas = areaImpactoService.listarTodasOrdenadas();

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/status/{ativa}/ordenadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Listar áreas por status ordenadas",
            description = "Retorna áreas filtradas por status e ordenadas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas retornadas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarPorStatusOrdenadas(
            @Parameter(description = "Status ativo (true/false)") @PathVariable Boolean ativa) {

        log.debug("Listando áreas por status: {} ordenadas", ativa);

        List<AreaImpactoDTO> areas = areaImpactoService.listarPorStatusOrdenadas(ativa);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar área por ID",
            description = "Retorna detalhes de uma área de impacto específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área encontrada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> buscarPorId(@PathVariable String id) {

        log.debug("Buscando área de impacto: {}", id);

        AreaImpactoDTO area = areaImpactoService.buscarPorId(id);

        return ResponseEntity.ok(area);
    }

    @GetMapping("/nome/{nome}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar área por nome",
            description = "Retorna área de impacto pelo nome exato"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área encontrada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> buscarPorNome(@PathVariable String nome) {

        log.debug("Buscando área por nome: {}", nome);

        AreaImpactoDTO area = areaImpactoService.buscarPorNome(nome);

        return ResponseEntity.ok(area);
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por termo",
            description = "Busca áreas que contenham o termo no nome"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorTermo(
            @Parameter(description = "Termo de busca") @RequestParam String termo) {

        log.debug("Buscando áreas contendo termo: {}", termo);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorTermoNoNome(termo);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por categoria",
            description = "Retorna áreas de uma categoria específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorCategoria(
            @Parameter(description = "Categoria") @PathVariable String categoria) {

        log.debug("Buscando áreas por categoria: {}", categoria);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorCategoria(categoria);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/categoria/{categoria}/paginadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por categoria com paginação",
            description = "Retorna áreas de uma categoria com paginação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<Page<AreaImpactoDTO>> buscarPorCategoriaPaginadas(
            @Parameter(description = "Categoria") @PathVariable String categoria,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        log.debug("Buscando áreas por categoria: {} (paginado)", categoria);

        Page<AreaImpactoDTO> areas = areaImpactoService.buscarPorCategoriaPaginadas(categoria, pageable);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/categoria/{categoria}/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas ativas por categoria",
            description = "Retorna apenas áreas ativas de uma categoria"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarAtivasPorCategoria(
            @Parameter(description = "Categoria") @PathVariable String categoria) {

        log.debug("Buscando áreas ativas por categoria: {}", categoria);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarAtivasPorCategoria(categoria);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/categoria/{categoria}/ordenadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por categoria ordenadas",
            description = "Retorna áreas de uma categoria ordenadas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorCategoriaOrdenadas(
            @Parameter(description = "Categoria") @PathVariable String categoria) {

        log.debug("Buscando áreas por categoria: {} ordenadas", categoria);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorCategoriaOrdenadas(categoria);

        return ResponseEntity.ok(areas);
    }

    @PostMapping("/buscar-por-keywords")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por keywords",
            description = "Retorna áreas que contenham qualquer uma das keywords especificadas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorKeywords(
            @RequestBody List<String> keywords) {

        log.debug("Buscando áreas por keywords: {}", keywords);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorKeywords(keywords);

        return ResponseEntity.ok(areas);
    }

    @PostMapping("/buscar-por-todas-keywords")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por todas as keywords",
            description = "Retorna áreas que contenham TODAS as keywords especificadas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorTodasKeywords(
            @RequestBody List<String> keywords) {

        log.debug("Buscando áreas por todas as keywords: {}", keywords);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorTodasKeywords(keywords);

        return ResponseEntity.ok(areas);
    }

    @PostMapping("/buscar-por-grupos-afetados")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por grupos afetados",
            description = "Retorna áreas que afetam qualquer um dos grupos especificados"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorGruposAfetados(
            @RequestBody List<String> grupos) {

        log.debug("Buscando áreas por grupos afetados: {}", grupos);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorGruposAfetados(grupos);

        return ResponseEntity.ok(areas);
    }

    @PostMapping("/buscar-por-todos-grupos-afetados")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por todos os grupos afetados",
            description = "Retorna áreas que afetam TODOS os grupos especificados"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorTodosGruposAfetados(
            @RequestBody List<String> grupos) {

        log.debug("Buscando áreas por todos os grupos afetados: {}", grupos);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorTodosGruposAfetados(grupos);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/criadas-apos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar áreas criadas após data",
            description = "Retorna áreas criadas após uma data específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarCriadasApos(
            @Parameter(description = "Data limite")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {

        log.debug("Buscando áreas criadas após: {}", data);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarCriadasApos(data);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/atualizadas-apos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar áreas atualizadas após data",
            description = "Retorna áreas atualizadas após uma data específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarAtualizadasApos(
            @Parameter(description = "Data limite")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {

        log.debug("Buscando áreas atualizadas após: {}", data);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarAtualizadasApos(data);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/criadas-entre")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar áreas criadas entre datas",
            description = "Retorna áreas criadas em um período específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarCriadasEntre(
            @Parameter(description = "Data inicial")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data final")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        log.debug("Buscando áreas criadas entre {} e {}", inicio, fim);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarCriadasEntre(inicio, fim);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/busca-livre")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas por texto livre",
            description = "Busca áreas por texto em nome e descrição"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<List<AreaImpactoDTO>> buscarPorTextoLivre(
            @Parameter(description = "Termo de busca") @RequestParam String termo) {

        log.debug("Buscando áreas por texto livre: {}", termo);

        List<AreaImpactoDTO> areas = areaImpactoService.buscarPorTextoLivre(termo);

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/busca-livre/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'VIEWER')")
    @Operation(
            summary = "Buscar áreas ativas por texto livre",
            description = "Busca áreas ativas por texto em nome e descrição"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas encontradas")
    })
    public ResponseEntity<Page<AreaImpactoDTO>> buscarAtivasPorTextoLivre(
            @Parameter(description = "Termo de busca") @RequestParam String termo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        log.debug("Buscando áreas ativas por texto livre: {}", termo);

        Page<AreaImpactoDTO> areas = areaImpactoService.buscarAtivasPorTextoLivre(termo, pageable);

        return ResponseEntity.ok(areas);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar área de impacto",
            description = "Cria nova área de impacto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Área criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> criar(@Valid @RequestBody CriarAreaImpactoDTO dto) {

        log.info("Criando área de impacto: {}", dto.nome());

        AreaImpactoDTO area = areaImpactoService.criar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(area);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar área de impacto",
            description = "Atualiza área de impacto existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área atualizada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AtualizarAreaImpactoDTO dto) {

        log.info("Atualizando área de impacto: {}", id);

        AreaImpactoDTO area = areaImpactoService.atualizar(id, dto);

        return ResponseEntity.ok(area);
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Ativar área de impacto",
            description = "Ativa uma área de impacto inativa"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área ativada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> ativar(@PathVariable String id) {

        log.info("Ativando área de impacto: {}", id);

        AreaImpactoDTO area = areaImpactoService.ativar(id);

        return ResponseEntity.ok(area);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar área de impacto",
            description = "Desativa uma área de impacto ativa"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Área desativada"),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<AreaImpactoDTO> desativar(@PathVariable String id) {

        log.info("Desativando área de impacto: {}", id);

        AreaImpactoDTO area = areaImpactoService.desativar(id);

        return ResponseEntity.ok(area);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deletar área de impacto",
            description = "Remove permanentemente uma área de impacto (apenas se não houver análises associadas)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Área deletada"),
            @ApiResponse(responseCode = "400", description = "Área possui análises associadas", content = @Content),
            @ApiResponse(responseCode = "404", description = "Área não encontrada", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable String id) {

        log.warn("Deletando área de impacto: {}", id);

        areaImpactoService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contar/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Contar áreas ativas",
            description = "Retorna o total de áreas de impacto ativas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarAtivas() {

        log.debug("Contando áreas ativas");

        long total = areaImpactoService.contarAtivas();

        return ResponseEntity.ok(total);
    }

    @GetMapping("/contar/inativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Contar áreas inativas",
            description = "Retorna o total de áreas de impacto inativas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarInativas() {

        log.debug("Contando áreas inativas");

        long total = areaImpactoService.contarInativas();

        return ResponseEntity.ok(total);
    }

    @GetMapping("/contar/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Contar áreas por categoria",
            description = "Retorna o total de áreas em uma categoria"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado")
    })
    public ResponseEntity<Long> contarPorCategoria(@PathVariable String categoria) {

        log.debug("Contando áreas da categoria: {}", categoria);

        long total = areaImpactoService.contarPorCategoria(categoria);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/existe/nome/{nome}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Verificar se nome existe",
            description = "Verifica se já existe área com o nome especificado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado da verificação")
    })
    public ResponseEntity<Boolean> existePorNome(@PathVariable String nome) {

        log.debug("Verificando existência do nome: {}", nome);

        boolean existe = areaImpactoService.existePorNome(nome);

        return ResponseEntity.ok(existe);
    }
}