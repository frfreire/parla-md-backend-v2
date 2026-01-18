package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.dto.AvaliarTriagemDTO;
import br.gov.md.parla_md_backend.service.TriagemService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/triagem")
@RequiredArgsConstructor
@Tag(name = "Triagem", description = "Gestão de triagem de proposições legislativas")
@SecurityRequirement(name = "bearer-jwt")
public class TriagemController {

    private final TriagemService triagemService;

    @GetMapping("/nao-avaliadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar proposições não avaliadas",
            description = "Retorna proposições que ainda não passaram por processo de triagem"
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
                    description = "Sem permissão para acessar triagem",
                    content = @Content
            )
    })
    public ResponseEntity<Page<Proposicao>> listarNaoAvaliadas(
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando proposições não avaliadas - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Proposicao> proposicoes = triagemService.getProposicoesNaoAvaliadas(pageable);

        log.debug("Retornadas {} proposições não avaliadas de um total de {}",
                proposicoes.getNumberOfElements(), proposicoes.getTotalElements());

        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/interesse")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar proposições de interesse",
            description = "Retorna proposições classificadas como de interesse do Ministério da Defesa"
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
                    description = "Sem permissão para acessar triagem",
                    content = @Content
            )
    })
    public ResponseEntity<Page<Proposicao>> listarInteresse(
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando proposições de interesse - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Proposicao> proposicoes = triagemService.getProposicoesInteresse(pageable);

        log.debug("Retornadas {} proposições de interesse de um total de {}",
                proposicoes.getNumberOfElements(), proposicoes.getTotalElements());

        return ResponseEntity.ok(proposicoes);
    }

    @GetMapping("/descartadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar proposições descartadas",
            description = "Retorna proposições classificadas como não relevantes para o MD"
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
                    description = "Sem permissão para acessar triagem",
                    content = @Content
            )
    })
    public ResponseEntity<Page<Proposicao>> listarDescartadas(
            @PageableDefault(size = 20, sort = "dataApresentacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.debug("Buscando proposições descartadas - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Proposicao> proposicoes = triagemService.getProposicoesDescartadas(pageable);

        log.debug("Retornadas {} proposições descartadas de um total de {}",
                proposicoes.getNumberOfElements(), proposicoes.getTotalElements());

        return ResponseEntity.ok(proposicoes);
    }

    @PostMapping("/{id}/avaliar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Avaliar proposição",
            description = "Classifica proposição após análise de triagem, definindo se é de interesse ou deve ser descartada"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Proposição avaliada com sucesso",
                    content = @Content(schema = @Schema(implementation = Proposicao.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de avaliação inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para avaliar proposições",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proposição não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<Proposicao> avaliar(
            @Parameter(description = "ID da proposição a ser avaliada", required = true)
            @PathVariable String id,
            @Valid @RequestBody AvaliarTriagemDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Avaliando proposição {} - novo status: {} - usuário: {}",
                id, dto.novoStatus(), userDetails.getUsername());

        if (dto.observacao() != null && !dto.observacao().isBlank()) {
            log.debug("Observação registrada para proposição {}: {}", id, dto.observacao());
        }

        Proposicao avaliada = triagemService.avaliarProposicao(
                id,
                dto.novoStatus(),
                dto.observacao()
        );

        log.info("Proposição {} avaliada com sucesso - status: {}", id, avaliada.getStatusTriagem());

        return ResponseEntity.ok(avaliada);
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Estatísticas de triagem",
            description = "Retorna estatísticas gerais do processo de triagem de proposições"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas calculadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas gestores podem acessar estatísticas",
                    content = @Content
            )
    })
    public ResponseEntity<Map<String, Long>> obterEstatisticas() {
        log.debug("Calculando estatísticas de triagem");

        long naoAvaliadas = triagemService.getProposicoesNaoAvaliadas(Pageable.unpaged()).getTotalElements();
        long interesse = triagemService.getProposicoesInteresse(Pageable.unpaged()).getTotalElements();
        long descartadas = triagemService.getProposicoesDescartadas(Pageable.unpaged()).getTotalElements();
        long total = naoAvaliadas + interesse + descartadas;

        Map<String, Long> estatisticas = new HashMap<>();
        estatisticas.put("total", total);
        estatisticas.put("naoAvaliadas", naoAvaliadas);
        estatisticas.put("interesse", interesse);
        estatisticas.put("descartadas", descartadas);

        log.debug("Estatísticas calculadas - Total: {}, Não avaliadas: {}, Interesse: {}, Descartadas: {}",
                total, naoAvaliadas, interesse, descartadas);

        return ResponseEntity.ok(estatisticas);
    }
}