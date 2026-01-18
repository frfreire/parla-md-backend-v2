package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.dto.EncaminhamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.TramitacaoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.service.TramitacaoService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de tramitações internas.
 *
 * <p>Fornece endpoints para:</p>
 * <ul>
 *   <li>Encaminhamento de processos entre setores</li>
 *   <li>Consulta de tramitações recebidas e enviadas</li>
 *   <li>Gestão do ciclo de vida das tramitações</li>
 *   <li>Controle de prazos e urgências</li>
 * </ul>
 *
 * @author Parla-MD
 * @version 1.0
 * @since 2025-01
 */
@Slf4j
@RestController
@RequestMapping("/api/tramitacoes")
@RequiredArgsConstructor
@Tag(name = "Tramitações", description = "Gestão de tramitações internas de processos legislativos")
@SecurityRequirement(name = "bearer-jwt")
public class TramitacaoController {

    private final TramitacaoService tramitacaoService;

    @PostMapping("/encaminhar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Encaminhar processo",
            description = "Encaminha um processo legislativo para outro setor ou usuário"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tramitação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
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
                    description = "Sem permissão para encaminhar processos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Processo ou destinatário não encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> encaminhar(
            @Valid @RequestBody EncaminhamentoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Encaminhando processo {} por usuário: {}",
                dto.getProcessoId(), usuarioId);

        Tramitacao tramitacao = tramitacaoService.encaminhar(dto, usuarioId);

        log.info("Tramitação criada com sucesso - ID: {}, Processo: {}, Destinatário: {}",
                tramitacao.getId(), dto.getProcessoId(), dto.getProcessoId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tramitacaoService.converterParaDTO(tramitacao));
    }

    @GetMapping("/recebidas")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar tramitações recebidas",
            description = "Retorna tramitações recebidas pelo usuário autenticado com paginação"
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
            )
    })
    public ResponseEntity<Page<TramitacaoDTO>> buscarRecebidas(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "dataEncaminhamento", direction = Sort.Direction.DESC)
            Pageable pageable) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.debug("Buscando tramitações recebidas - Usuário: {}", usuarioId);

        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorDestinatario(usuarioId, pageable);

        log.debug("Retornadas {} tramitações recebidas", tramitacoes.getTotalElements());

        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/enviadas")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar tramitações enviadas",
            description = "Retorna tramitações enviadas pelo usuário autenticado com paginação"
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
            )
    })
    public ResponseEntity<Page<TramitacaoDTO>> buscarEnviadas(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "dataEncaminhamento", direction = Sort.Direction.DESC)
            Pageable pageable) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.debug("Buscando tramitações enviadas - Usuário: {}", usuarioId);

        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorRemetente(usuarioId, pageable);

        log.debug("Retornadas {} tramitações enviadas", tramitacoes.getTotalElements());

        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/processo/{processoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Listar tramitações de um processo",
            description = "Retorna histórico completo de tramitações de um processo legislativo"
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
                    description = "Sem permissão para visualizar tramitações",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Processo não encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<TramitacaoDTO>> buscarPorProcesso(
            @Parameter(description = "ID do processo legislativo", required = true)
            @PathVariable String processoId) {

        log.debug("Buscando tramitações do processo: {}", processoId);

        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorProcesso(processoId);

        log.debug("Retornadas {} tramitações do processo {}", tramitacoes.size(), processoId);

        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/urgentes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar tramitações urgentes",
            description = "Retorna tramitações urgentes do usuário autenticado"
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
            )
    })
    public ResponseEntity<List<TramitacaoDTO>> buscarUrgentes(
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.debug("Buscando tramitações urgentes - Usuário: {}", usuarioId);

        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarUrgentes(usuarioId);

        log.debug("Retornadas {} tramitações urgentes", tramitacoes.size());

        return ResponseEntity.ok(tramitacoes);
    }

    @PutMapping("/{tramitacaoId}/receber")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Receber tramitação",
            description = "Marca tramitação como recebida pelo destinatário"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tramitação recebida com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não é o destinatário desta tramitação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Tramitação já foi recebida",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> receber(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Recebendo tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.receber(tramitacaoId, usuarioId);

        log.info("Tramitação recebida com sucesso - ID: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    @PutMapping("/{tramitacaoId}/concluir")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Concluir tramitação",
            description = "Marca tramitação como concluída"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tramitação concluída com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não é o responsável por esta tramitação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Tramitação já foi concluída",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> concluir(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Concluindo tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.concluir(tramitacaoId, usuarioId);

        log.info("Tramitação concluída com sucesso - ID: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    @PutMapping("/{tramitacaoId}/iniciar-analise")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Iniciar análise",
            description = "Marca início da análise de uma tramitação"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Análise iniciada com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não pode iniciar análise desta tramitação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> iniciarAnalise(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Iniciando análise da tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.iniciarAnalise(tramitacaoId, usuarioId);

        log.info("Análise iniciada com sucesso - Tramitação: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    @PutMapping("/{tramitacaoId}/solicitar-parecer")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(
            summary = "Solicitar parecer",
            description = "Solicita parecer técnico para uma tramitação"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Parecer solicitado com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para solicitar parecer",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> solicitarParecer(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Solicitando parecer para tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.solicitarParecer(tramitacaoId, usuarioId);

        log.info("Parecer solicitado com sucesso - Tramitação: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    @PutMapping("/{tramitacaoId}/suspender")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Suspender tramitação",
            description = "Suspende temporariamente uma tramitação (apenas ADMIN/GESTOR)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tramitação suspensa com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN e GESTOR podem suspender tramitações",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> suspender(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Suspendendo tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.suspender(tramitacaoId, usuarioId);

        log.info("Tramitação suspensa com sucesso - ID: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    @PutMapping("/{tramitacaoId}/retomar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Retomar tramitação",
            description = "Retoma uma tramitação suspensa (apenas ADMIN/GESTOR)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tramitação retomada com sucesso",
                    content = @Content(schema = @Schema(implementation = TramitacaoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN e GESTOR podem retomar tramitações",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tramitação não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Tramitação não está suspensa",
                    content = @Content
            )
    })
    public ResponseEntity<TramitacaoDTO> retomar(
            @Parameter(description = "ID da tramitação", required = true)
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Retomando tramitação {} - Usuário: {}", tramitacaoId, usuarioId);

        Tramitacao tramitacao = tramitacaoService.retomar(tramitacaoId, usuarioId);

        log.info("Tramitação retomada com sucesso - ID: {}", tramitacaoId);

        return ResponseEntity.ok(tramitacaoService.converterParaDTO(tramitacao));
    }

    /**
     * Extrai o ID do usuário a partir do UserDetails.
     * Adaptar conforme implementação real do UserDetails no projeto.
     */
    private String extrairUsuarioId(UserDetails userDetails) {
        // Assumindo que o username é o ID do usuário
        // Adaptar conforme a implementação real
        return userDetails.getUsername();
    }
}