package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.PreferenciasNotificacao;
import br.gov.md.parla_md_backend.service.PreferenciasNotificacaoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/preferencias-notificacao")
@RequiredArgsConstructor
@Tag(name = "Preferências de Notificação", description = "Gestão de preferências de notificação dos usuários")
@SecurityRequirement(name = "bearer-jwt")
public class PreferenciasNotificacaoController {

    private final PreferenciasNotificacaoService preferenciasService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Buscar preferências do usuário autenticado",
            description = "Retorna as preferências de notificação do usuário logado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = PreferenciasNotificacao.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            )
    })
    public ResponseEntity<PreferenciasNotificacao> buscarMinhasPreferencias(
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.debug("Buscando preferências de notificação do usuário: {}", usuarioId);

        PreferenciasNotificacao preferencias = preferenciasService.buscarPorUsuario(usuarioId);

        return ResponseEntity.ok(preferencias);
    }

    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Buscar preferências por ID do usuário",
            description = "Retorna preferências de notificação de um usuário específico (apenas ADMIN/GESTOR)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = PreferenciasNotificacao.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar preferências de outros usuários",
                    content = @Content
            )
    })
    public ResponseEntity<PreferenciasNotificacao> buscarPorUsuario(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String usuarioId) {

        log.debug("Buscando preferências de notificação do usuário: {}", usuarioId);

        PreferenciasNotificacao preferencias = preferenciasService.buscarPorUsuario(usuarioId);

        return ResponseEntity.ok(preferencias);
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Atualizar preferências do usuário autenticado",
            description = "Atualiza as preferências de notificação do usuário logado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = PreferenciasNotificacao.class))
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
                    description = "ID do usuário nas preferências não corresponde ao usuário autenticado",
                    content = @Content
            )
    })
    public ResponseEntity<PreferenciasNotificacao> atualizarMinhasPreferencias(
            @Valid @RequestBody PreferenciasNotificacao preferencias,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        if (!preferencias.getUsuarioId().equals(usuarioId)) {
            log.warn("Usuário {} tentou atualizar preferências de outro usuário: {}",
                    usuarioId, preferencias.getUsuarioId());
            return ResponseEntity.status(403).build();
        }

        log.info("Atualizando preferências de notificação do usuário: {}", usuarioId);

        PreferenciasNotificacao atualizadas = preferenciasService.atualizar(preferencias);

        log.info("Preferências atualizadas com sucesso - Usuário: {}", usuarioId);

        return ResponseEntity.ok(atualizadas);
    }

    @PutMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar preferências de um usuário específico",
            description = "Atualiza preferências de notificação de qualquer usuário (apenas ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = PreferenciasNotificacao.class))
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
                    description = "Apenas ADMIN pode atualizar preferências de outros usuários",
                    content = @Content
            )
    })
    public ResponseEntity<PreferenciasNotificacao> atualizarPreferencias(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String usuarioId,
            @Valid @RequestBody PreferenciasNotificacao preferencias) {

        preferencias.setUsuarioId(usuarioId);

        log.info("ADMIN atualizando preferências de notificação do usuário: {}", usuarioId);

        PreferenciasNotificacao atualizadas = preferenciasService.atualizar(preferencias);

        log.info("Preferências atualizadas com sucesso - Usuário: {}", usuarioId);

        return ResponseEntity.ok(atualizadas);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Deletar preferências do usuário autenticado",
            description = "Remove as preferências de notificação do usuário logado (volta ao padrão)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Preferências deletadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deletarMinhasPreferencias(
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = extrairUsuarioId(userDetails);

        log.info("Deletando preferências de notificação do usuário: {}", usuarioId);

        preferenciasService.deletarPorUsuario(usuarioId);

        log.info("Preferências deletadas com sucesso - Usuário: {}", usuarioId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Deletar preferências de um usuário específico",
            description = "Remove preferências de notificação de qualquer usuário (apenas ADMIN)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Preferências deletadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Apenas ADMIN pode deletar preferências de outros usuários",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deletarPreferencias(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable String usuarioId) {

        log.info("ADMIN deletando preferências de notificação do usuário: {}", usuarioId);

        preferenciasService.deletarPorUsuario(usuarioId);

        log.info("Preferências deletadas com sucesso - Usuário: {}", usuarioId);

        return ResponseEntity.noContent().build();
    }

    private String extrairUsuarioId(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}