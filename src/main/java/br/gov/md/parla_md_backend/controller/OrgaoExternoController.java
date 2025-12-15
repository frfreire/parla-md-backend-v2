package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.organizacao.OrgaoExterno;
import br.gov.md.parla_md_backend.service.organizacao.OrgaoExternoService;
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
 * Controller para gerenciamento de órgãos externos
 */
@Slf4j
@RestController
@RequestMapping("/api/orgaos-externos")
@RequiredArgsConstructor
@Tag(name = "Órgãos Externos", description = "Endpoints para gerenciamento de Ministérios e Forças Armadas")
@SecurityRequirement(name = "bearer-jwt")
public class OrgaoExternoController {

    private final OrgaoExternoService orgaoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar novo órgão externo")
    public ResponseEntity<OrgaoExterno> cadastrar(@Valid @RequestBody OrgaoExterno orgao) {
        log.info("Cadastrando novo órgão externo: {}", orgao.getNome());

        OrgaoExterno criado = orgaoService.cadastrar(orgao);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Buscar órgão por ID")
    public ResponseEntity<OrgaoExterno> buscarPorId(@PathVariable String id) {
        log.debug("Buscando órgão: {}", id);

        OrgaoExterno orgao = orgaoService.buscarPorId(id);
        return ResponseEntity.ok(orgao);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'GESTOR')")
    @Operation(summary = "Listar todos os órgãos ativos")
    public ResponseEntity<List<OrgaoExterno>> listarAtivos() {
        log.debug("Listando órgãos ativos");

        List<OrgaoExterno> orgaos = orgaoService.listarAtivos();
        return ResponseEntity.ok(orgaos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar órgão externo")
    public ResponseEntity<OrgaoExterno> atualizar(
            @PathVariable String id,
            @Valid @RequestBody OrgaoExterno orgao) {

        log.info("Atualizando órgão: {}", id);

        OrgaoExterno atualizado = orgaoService.atualizar(id, orgao);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar órgão externo")
    public ResponseEntity<Void> desativar(@PathVariable String id) {
        log.info("Desativando órgão: {}", id);

        orgaoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}