package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.organizacao.Setor;
import br.gov.md.parla_md_backend.service.organizacao.SetorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo setor")
    public ResponseEntity<Setor> criarSetor(@Valid @RequestBody Setor setor) {
        log.info("Criando novo setor: {}", setor.getNome());

        Setor criado = setorService.criar(setor);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar setor por ID")
    public ResponseEntity<Setor> buscarPorId(@PathVariable String id) {
        log.debug("Buscando setor: {}", id);

        Setor setor = setorService.buscarPorId(id);
        return ResponseEntity.ok(setor);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Listar todos os setores ativos")
    public ResponseEntity<List<Setor>> listarAtivos() {
        log.debug("Listando setores ativos");

        List<Setor> setores = setorService.listarAtivos();
        return ResponseEntity.ok(setores);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar setor")
    public ResponseEntity<Setor> atualizar(
            @PathVariable String id,
            @Valid @RequestBody Setor setor) {

        log.info("Atualizando setor: {}", id);

        Setor atualizado = setorService.atualizar(id, setor);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar setor")
    public ResponseEntity<Void> desativar(@PathVariable String id) {
        log.info("Desativando setor: {}", id);

        setorService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}