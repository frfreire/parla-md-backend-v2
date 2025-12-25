package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.AtualizarSetorDTO;
import br.gov.md.parla_md_backend.domain.dto.CriarSetorDTO;
import br.gov.md.parla_md_backend.domain.dto.SetorDTO;
import br.gov.md.parla_md_backend.service.SetorService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de setores internos do MD
 */
@Slf4j
@RestController
@RequestMapping("/api/setores")
@RequiredArgsConstructor
@Tag(name = "Setores", description = "Endpoints para gerenciamento de setores internos do MD")
@SecurityRequirement(name = "bearer-jwt")
public class SetorController {

    private final SetorService setorService;

    /**
     * Cria novo setor
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo setor",
            description = "Cadastra novo setor interno do MD")
    public ResponseEntity<SetorDTO> criar(@Valid @RequestBody CriarSetorDTO dto) {
        log.info("Criando novo setor: {}", dto.sigla());

        SetorDTO setor = setorService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(setor);
    }

    /**
     * Atualiza setor
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar setor",
            description = "Atualiza dados do setor")
    public ResponseEntity<SetorDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AtualizarSetorDTO dto) {

        log.info("Atualizando setor: {}", id);

        SetorDTO setor = setorService.atualizar(id, dto);
        return ResponseEntity.ok(setor);
    }

    /**
     * Busca setor por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar setor por ID")
    public ResponseEntity<SetorDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando setor: {}", id);

        SetorDTO setor = setorService.buscarPorId(id);
        return ResponseEntity.ok(setor);
    }

    /**
     * Busca setor por sigla
     */
    @GetMapping("/sigla/{sigla}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar setor por sigla")
    public ResponseEntity<SetorDTO> buscarPorSigla(@PathVariable String sigla) {
        log.debug("Buscando setor por sigla: {}", sigla);

        SetorDTO setor = setorService.buscarPorSigla(sigla);
        return ResponseEntity.ok(setor);
    }

    /**
     * Lista todos os setores com paginação
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar todos os setores",
            description = "Lista todos os setores cadastrados com paginação")
    public ResponseEntity<Page<SetorDTO>> listar(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.debug("Listando setores - página: {}", pageable.getPageNumber());

        Page<SetorDTO> setores = setorService.listar(pageable);
        return ResponseEntity.ok(setores);
    }

    /**
     * Lista apenas setores ativos
     */
    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar setores ativos",
            description = "Lista apenas os setores com status ativo")
    public ResponseEntity<List<SetorDTO>> listarAtivos() {
        log.debug("Listando setores ativos");

        List<SetorDTO> setores = setorService.listarAtivos();
        return ResponseEntity.ok(setores);
    }

    /**
     * Lista setores por nível hierárquico
     */
    @GetMapping("/nivel/{nivel}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar setores por nível",
            description = "Lista setores de um nível hierárquico específico")
    public ResponseEntity<List<SetorDTO>> listarPorNivel(@PathVariable Integer nivel) {
        log.debug("Listando setores de nível: {}", nivel);

        List<SetorDTO> setores = setorService.listarPorNivel(nivel);
        return ResponseEntity.ok(setores);
    }

    /**
     * Lista setores raiz (nível 1)
     */
    @GetMapping("/raiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar setores raiz",
            description = "Lista setores de primeiro nível (sem setor pai)")
    public ResponseEntity<List<SetorDTO>> listarSetoresRaiz() {
        log.debug("Listando setores raiz");

        List<SetorDTO> setores = setorService.listarSetoresRaiz();
        return ResponseEntity.ok(setores);
    }

    /**
     * Lista subsetores de um setor
     */
    @GetMapping("/{id}/subsetores")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar subsetores",
            description = "Lista todos os subsetores diretos de um setor")
    public ResponseEntity<List<SetorDTO>> listarSubsetores(@PathVariable String id) {
        log.debug("Listando subsetores do setor: {}", id);

        List<SetorDTO> setores = setorService.listarSubsetores(id);
        return ResponseEntity.ok(setores);
    }

    /**
     * Busca setores por nome
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar setores por nome",
            description = "Busca setores por nome (parcial, case-insensitive)")
    public ResponseEntity<List<SetorDTO>> buscarPorNome(@RequestParam String nome) {
        log.debug("Buscando setores por nome: {}", nome);

        List<SetorDTO> setores = setorService.buscarPorNome(nome);
        return ResponseEntity.ok(setores);
    }

    /**
     * Ativa setor
     */
    @PutMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar setor",
            description = "Ativa setor previamente desativado")
    public ResponseEntity<SetorDTO> ativar(@PathVariable String id) {
        log.info("Ativando setor: {}", id);

        SetorDTO setor = setorService.ativar(id);
        return ResponseEntity.ok(setor);
    }

    /**
     * Desativa setor
     */
    @PutMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar setor",
            description = "Desativa setor (não poderá receber tramitações)")
    public ResponseEntity<SetorDTO> desativar(@PathVariable String id) {
        log.info("Desativando setor: {}", id);

        SetorDTO setor = setorService.desativar(id);
        return ResponseEntity.ok(setor);
    }

    /**
     * Define responsável do setor
     */
    @PutMapping("/{setorId}/responsavel/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Definir responsável do setor",
            description = "Define ou altera o responsável pelo setor")
    public ResponseEntity<SetorDTO> definirResponsavel(
            @PathVariable String setorId,
            @PathVariable String usuarioId) {

        log.info("Definindo responsável do setor: {}", setorId);

        SetorDTO setor = setorService.definirResponsavel(setorId, usuarioId);
        return ResponseEntity.ok(setor);
    }

    /**
     * Deleta setor
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar setor",
            description = "Remove permanentemente setor do sistema (apenas se não tiver subsetores)")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        log.info("Deletando setor: {}", id);

        setorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca organograma hierárquico
     */
    @GetMapping("/organograma")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Organograma completo",
            description = "Retorna estrutura hierárquica completa dos setores")
    public ResponseEntity<List<SetorHierarquicoDTO>> buscarOrganograma() {
        log.debug("Buscando organograma completo");

        List<SetorDTO> setoresRaiz = setorService.listarSetoresRaiz();

        List<SetorHierarquicoDTO> organograma = setoresRaiz.stream()
                .map(this::construirHierarquia)
                .toList();

        return ResponseEntity.ok(organograma);
    }

    /**
     * Constrói hierarquia recursivamente
     */
    private SetorHierarquicoDTO construirHierarquia(SetorDTO setor) {
        List<SetorDTO> subsetores = setorService.listarSubsetores(setor.id());

        List<SetorHierarquicoDTO> subsetoresHierarquicos = subsetores.stream()
                .map(this::construirHierarquia)
                .toList();

        return new SetorHierarquicoDTO(
                setor.id(),
                setor.nome(),
                setor.sigla(),
                setor.nivel(),
                setor.responsavelNome(),
                setor.ativo(),
                subsetoresHierarquicos
        );
    }

    /**
     * DTO para estrutura hierárquica
     */
    public record SetorHierarquicoDTO(
            String id,
            String nome,
            String sigla,
            Integer nivel,
            String responsavel,
            boolean ativo,
            List<SetorHierarquicoDTO> subsetores
    ) {}
}