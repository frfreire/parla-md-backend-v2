package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.AreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.AtualizarAreaImpactoDTO;
import br.gov.md.parla_md_backend.domain.dto.CriarAreaImpactoDTO;
import br.gov.md.parla_md_backend.service.AreaImpactoService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/areas-impacto")
@RequiredArgsConstructor
@Tag(name = "Áreas de Impacto", description = "Gestão de áreas de impacto para análises legislativas")
@SecurityRequirement(name = "bearer-jwt")
public class AreaImpactoController {

    private final AreaImpactoService areaImpactoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar todas as áreas de impacto",
            description = "Retorna lista completa de áreas de impacto configuradas no sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar áreas de impacto",
                    content = @Content
            )
    })
    public ResponseEntity<Page<AreaImpactoDTO>> listarTodas(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.debug("Listando todas as áreas de impacto");

        Page<AreaImpactoDTO> areas = areaImpactoService.listarTodas(pageable);

        log.debug("Retornadas {} áreas de impacto", areas.getTotalElements());

        return ResponseEntity.ok(areas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Buscar área de impacto por ID",
            description = "Retorna detalhes completos de uma área de impacto específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Área encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = AreaImpactoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar áreas de impacto",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Área de impacto não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<AreaImpactoDTO> buscarPorId(
            @Parameter(description = "ID da área de impacto", required = true)
            @PathVariable String id) {

        log.debug("Buscando área de impacto por ID: {}", id);

        AreaImpactoDTO area = areaImpactoService.buscarPorId(id);

        log.debug("Área de impacto encontrada: {}", area.nome());

        return ResponseEntity.ok(area);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar área de impacto",
            description = "Cadastra nova área de impacto para análises legislativas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Área criada com sucesso",
                    content = @Content(schema = @Schema(implementation = AreaImpactoDTO.class))
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
                    description = "Apenas administradores podem criar áreas",
                    content = @Content
            )
    })
    public ResponseEntity<AreaImpactoDTO> criar(
            @Valid @RequestBody CriarAreaImpactoDTO dto) {

        log.info("Criando nova área de impacto: {}", dto.nome());

        AreaImpactoDTO criada = areaImpactoService.criar(dto);

        log.info("Área de impacto criada com sucesso - ID: {}, Nome: {}",
                criada.id(), criada.nome());

        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar área de impacto",
            description = "Atualiza dados de uma área de impacto existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Área atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = AreaImpactoDTO.class))
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
                    description = "Apenas administradores podem atualizar áreas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Área de impacto não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<AreaImpactoDTO> atualizar(
            @Parameter(description = "ID da área de impacto", required = true)
            @PathVariable String id,
            @Valid @RequestBody AtualizarAreaImpactoDTO dto) {

        log.info("Atualizando área de impacto: {}", id);

        AreaImpactoDTO atualizada = areaImpactoService.atualizar(id, dto);

        log.info("Área de impacto atualizada com sucesso - ID: {}, Nome: {}",
                atualizada.id(), atualizada.nome());

        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deletar área de impacto",
            description = "Remove permanentemente uma área de impacto do sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Área deletada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas administradores podem deletar áreas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Área de impacto não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Área possui análises vinculadas e não pode ser deletada",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da área de impacto", required = true)
            @PathVariable String id) {

        log.info("Deletando área de impacto: {}", id);

        areaImpactoService.deletar(id);

        log.info("Área de impacto deletada com sucesso - ID: {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ativas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar áreas ativas",
            description = "Retorna apenas áreas de impacto ativas no sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar áreas de impacto",
                    content = @Content
            )
    })
    public ResponseEntity<List<AreaImpactoDTO>> listarAtivas() {
        log.debug("Listando áreas de impacto ativas");

        List<AreaImpactoDTO> areas = areaImpactoService.listarAtivas();

        log.debug("Retornadas {} áreas de impacto ativas", areas.size());

        return ResponseEntity.ok(areas);
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Ativar área de impacto",
            description = "Ativa uma área de impacto previamente desativada"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Área ativada com sucesso",
                    content = @Content(schema = @Schema(implementation = AreaImpactoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas administradores podem ativar áreas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Área de impacto não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<AreaImpactoDTO> ativar(
            @Parameter(description = "ID da área de impacto", required = true)
            @PathVariable String id) {

        log.info("Ativando área de impacto: {}", id);

        AreaImpactoDTO ativada = areaImpactoService.ativar(id);

        log.info("Área de impacto ativada com sucesso - ID: {}, Nome: {}",
                ativada.id(), ativada.nome());

        return ResponseEntity.ok(ativada);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar área de impacto",
            description = "Desativa uma área de impacto ativa"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Área desativada com sucesso",
                    content = @Content(schema = @Schema(implementation = AreaImpactoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas administradores podem desativar áreas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Área de impacto não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<AreaImpactoDTO> desativar(
            @Parameter(description = "ID da área de impacto", required = true)
            @PathVariable String id) {

        log.info("Desativando área de impacto: {}", id);

        AreaImpactoDTO desativada = areaImpactoService.desativar(id);

        log.info("Área de impacto desativada com sucesso - ID: {}, Nome: {}",
                desativada.id(), desativada.nome());

        return ResponseEntity.ok(desativada);
    }
}