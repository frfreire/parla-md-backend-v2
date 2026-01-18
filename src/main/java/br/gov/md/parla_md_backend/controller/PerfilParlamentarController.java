package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.PerfilParlamentar;
import br.gov.md.parla_md_backend.service.PerfilParlamentarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Slf4j
@RestController
@RequestMapping("/api/perfis-parlamentares")
@RequiredArgsConstructor
@Tag(name = "Perfis Parlamentares", description = "Gestão de perfis e análises de parlamentares")
@SecurityRequirement(name = "bearer-jwt")
public class PerfilParlamentarController {

    private final PerfilParlamentarService perfilService;

    @GetMapping("/parlamentar/{parlamentarId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar perfil por ID do parlamentar",
            description = "Retorna o perfil completo de um parlamentar específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil encontrado",
                    content = @Content(schema = @Schema(implementation = PerfilParlamentar.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Perfil não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão de acesso",
                    content = @Content
            )
    })
    public ResponseEntity<PerfilParlamentar> buscarPorParlamentarId(
            @Parameter(description = "ID do parlamentar", required = true)
            @PathVariable String parlamentarId) {

        log.debug("Buscando perfil do parlamentar: {}", parlamentarId);

        PerfilParlamentar perfil = perfilService.buscarPorParlamentarId(parlamentarId);

        log.debug("Perfil encontrado: {}", perfil.getNomeParlamentar());

        return ResponseEntity.ok(perfil);
    }

    @GetMapping("/casa/{casa}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar perfis por casa legislativa",
            description = "Retorna todos os perfis de uma casa específica (CAMARA ou SENADO)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de perfis retornada",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão de acesso",
                    content = @Content
            )
    })
    public ResponseEntity<List<PerfilParlamentar>> buscarPorCasa(
            @Parameter(description = "Casa legislativa (CAMARA ou SENADO)", required = true, example = "CAMARA")
            @PathVariable String casa) {

        log.debug("Buscando perfis da casa: {}", casa);

        List<PerfilParlamentar> perfis = perfilService.buscarPorCasa(casa.toUpperCase());

        log.debug("Encontrados {} perfis da casa {}", perfis.size(), casa);

        return ResponseEntity.ok(perfis);
    }

    @GetMapping("/partido/{partido}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar perfis por partido",
            description = "Retorna todos os perfis de parlamentares de um partido específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de perfis retornada",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão de acesso",
                    content = @Content
            )
    })
    public ResponseEntity<List<PerfilParlamentar>> buscarPorPartido(
            @Parameter(description = "Sigla do partido", required = true, example = "PT")
            @PathVariable String partido) {

        log.debug("Buscando perfis do partido: {}", partido);

        List<PerfilParlamentar> perfis = perfilService.buscarPorPartido(partido.toUpperCase());

        log.debug("Encontrados {} perfis do partido {}", perfis.size(), partido);

        return ResponseEntity.ok(perfis);
    }

    @GetMapping("/uf/{uf}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar perfis por UF",
            description = "Retorna todos os perfis de parlamentares de uma UF específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de perfis retornada",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão de acesso",
                    content = @Content
            )
    })
    public ResponseEntity<List<PerfilParlamentar>> buscarPorUf(
            @Parameter(description = "Sigla da UF", required = true, example = "SP")
            @PathVariable String uf) {

        log.debug("Buscando perfis da UF: {}", uf);

        List<PerfilParlamentar> perfis = perfilService.buscarPorUf(uf.toUpperCase());

        log.debug("Encontrados {} perfis da UF {}", perfis.size(), uf);

        return ResponseEntity.ok(perfis);
    }

    @GetMapping("/pendentes-atualizacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar perfis pendentes de atualização",
            description = "Retorna perfis que precisam ser atualizados com base na data de próxima atualização"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de perfis pendentes retornada",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN e GESTOR podem acessar",
                    content = @Content
            )
    })
    public ResponseEntity<List<PerfilParlamentar>> buscarPendentesAtualizacao() {
        log.debug("Buscando perfis pendentes de atualização");

        List<PerfilParlamentar> perfis = perfilService.buscarPendentesAtualizacao();

        log.info("Encontrados {} perfis pendentes de atualização", perfis.size());

        return ResponseEntity.ok(perfis);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Criar perfil parlamentar",
            description = "Cadastra um novo perfil de análise para um parlamentar"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Perfil criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PerfilParlamentar.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN e GESTOR podem criar perfis",
                    content = @Content
            )
    })
    public ResponseEntity<PerfilParlamentar> criar(
            @Valid @RequestBody PerfilParlamentar perfil) {

        log.info("Criando novo perfil parlamentar: {}", perfil.getNomeParlamentar());

        PerfilParlamentar criado = perfilService.criar(perfil);

        log.info("Perfil parlamentar criado com sucesso - ID: {}", criado.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Atualizar perfil parlamentar",
            description = "Atualiza dados de um perfil parlamentar existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PerfilParlamentar.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN e GESTOR podem atualizar perfis",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Perfil não encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<PerfilParlamentar> atualizar(
            @Valid @RequestBody PerfilParlamentar perfil) {

        log.info("Atualizando perfil parlamentar: {}", perfil.getId());

        PerfilParlamentar atualizado = perfilService.atualizar(perfil);

        log.info("Perfil parlamentar atualizado com sucesso - ID: {}", atualizado.getId());

        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/parlamentar/{parlamentarId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deletar perfil parlamentar",
            description = "Remove permanentemente o perfil de um parlamentar (apenas ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Perfil deletado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN pode deletar perfis",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Perfil não encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do parlamentar", required = true)
            @PathVariable String parlamentarId) {

        log.info("Deletando perfil do parlamentar: {}", parlamentarId);

        perfilService.deletar(parlamentarId);

        log.info("Perfil parlamentar deletado com sucesso - Parlamentar ID: {}", parlamentarId);

        return ResponseEntity.noContent().build();
    }
}