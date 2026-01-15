package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.AutenticacaoConfigDTO;
import br.gov.md.parla_md_backend.domain.dto.PermissaoDTO;
import br.gov.md.parla_md_backend.domain.dto.TokenValidationDTO;
import br.gov.md.parla_md_backend.domain.dto.UsuarioInfoDTO;
import br.gov.md.parla_md_backend.service.AutenticacaoApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para gerenciamento de autenticação e autorização")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AutenticacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoController.class);

    private final AutenticacaoApiService autenticacaoApiService;

    public AutenticacaoController(AutenticacaoApiService autenticacaoApiService) {
        this.autenticacaoApiService = autenticacaoApiService;
    }

    @GetMapping("/config")
    @Operation(summary = "Obter configurações públicas do Keycloak",
            description = "Retorna as configurações necessárias para que o frontend possa se integrar com o Keycloak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configurações obtidas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AutenticacaoConfigDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<AutenticacaoConfigDTO> obterConfiguracao() {
        logger.debug("Solicitação de configuração pública do Keycloak");

        try {
            AutenticacaoConfigDTO config = autenticacaoApiService.obterConfiguracaoPublica();
            logger.info("Configuração pública retornada com sucesso. Realm: {}, Client ID: {}",
                    config.getRealm(), config.getClientId());

            return ResponseEntity.ok(config);

        } catch (Exception e) {
            logger.error("Erro ao obter configuração pública: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar saúde do sistema de autenticação",
            description = "Verifica se o sistema de autenticação está funcionando corretamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sistema saudável"),
            @ApiResponse(responseCode = "503", description = "Sistema com problemas")
    })
    public ResponseEntity<Map<String, Object>> verificarSaude() {
        logger.debug("Verificação de saúde do sistema de autenticação");

        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "autenticacao");

        try {
            boolean saudavel = autenticacaoApiService.verificarSaudeAutenticacao();

            health.put("status", saudavel ? "UP" : "DOWN");
            health.put("keycloak", saudavel ? "CONNECTED" : "DISCONNECTED");

            HttpStatus status = saudavel ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

            logger.info("Health check concluído. Status: {}", saudavel ? "UP" : "DOWN");
            return ResponseEntity.status(status).body(health);

        } catch (Exception e) {
            logger.error("Erro durante health check: {}", e.getMessage(), e);

            health.put("status", "DOWN");
            health.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    @GetMapping("/user-info")
    @Operation(summary = "Obter informações do usuário autenticado",
            description = "Retorna as informações completas do usuário atualmente logado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações do usuário obtidas com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioInfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioInfoDTO> obterInformacoesUsuario() {
        logger.debug("Solicitação de informações do usuário autenticado");

        try {
            UsuarioInfoDTO usuarioInfo = autenticacaoApiService.obterInformacoesUsuarioAtual();

            logger.info("Informações do usuário '{}' retornadas com sucesso", usuarioInfo.getUsername());
            return ResponseEntity.ok(usuarioInfo);

        } catch (Exception e) {
            logger.error("Erro ao obter informações do usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/permissions")
    @Operation(summary = "Obter permissões do usuário atual",
            description = "Retorna todas as permissões atribuídas ao usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissões obtidas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Set<String>> obterPermissoes() {
        logger.debug("Solicitação de permissões do usuário autenticado");

        try {
            Set<String> permissoes = autenticacaoApiService.extrairPermissoesDoUsuario();

            logger.debug("Permissões do usuário retornadas: {}", permissoes);
            return ResponseEntity.ok(permissoes);

        } catch (Exception e) {
            logger.error("Erro ao obter permissões do usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/check-permission")
    @Operation(summary = "Verificar se usuário possui permissão específica",
            description = "Verifica se o usuário autenticado possui uma permissão específica",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> verificarPermissao(
            @Valid @RequestBody
            @Parameter(description = "Dados da permissão a ser verificada")
            PermissaoDTO permissao) {

        logger.debug("Verificação de permissão: {}", permissao.getPermissao());

        try {
            Boolean possuiPermissao = autenticacaoApiService.verificarPermissao(permissao.getPermissao());

            logger.debug("Resultado da verificação de permissão '{}': {}",
                    permissao.getPermissao(), possuiPermissao);

            return ResponseEntity.ok(possuiPermissao);

        } catch (Exception e) {
            logger.error("Erro ao verificar permissão '{}': {}", permissao.getPermissao(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validar token JWT",
            description = "Valida se um token JWT específico é válido",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou malformado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> validarToken(
            @Valid @RequestBody
            @Parameter(description = "Dados do token a ser validado")
            TokenValidationDTO tokenValidation) {

        logger.debug("Validação de token solicitada");

        try {
            Boolean tokenValido = autenticacaoApiService.validarToken(tokenValidation.getToken());

            logger.debug("Resultado da validação do token: {}", tokenValido);
            return ResponseEntity.ok(tokenValido);

        } catch (Exception e) {
            logger.error("Erro ao validar token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Realizar logout do usuário",
            description = "Gera a URL de logout do Keycloak para o usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL de logout gerada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(
            @RequestParam(required = false)
            @Parameter(description = "URI para redirecionamento após logout")
            String redirectUri) {

        logger.debug("Solicitação de logout do usuário");

        try {
            String logoutUrl = redirectUri != null ?
                    autenticacaoApiService.obterUrlLogoutCompleto(redirectUri) :
                    autenticacaoApiService.obterUrlLogout();

            Map<String, String> response = new HashMap<>();
            response.put("logout_url", logoutUrl);
            response.put("message", "URL de logout gerada com sucesso");

            logger.info("URL de logout gerada com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro ao gerar URL de logout: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro ao gerar URL de logout");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/roles")
    @Operation(summary = "Obter roles do usuário atual",
            description = "Retorna todas as roles atribuídas ao usuário autenticado",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles obtidas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Set<String>> obterRoles() {
        logger.debug("Solicitação de roles do usuário autenticado");

        try {
            Set<String> roles = autenticacaoApiService.extrairRolesDoUsuario();

            logger.debug("Roles do usuário retornadas: {}", roles);
            return ResponseEntity.ok(roles);

        } catch (Exception e) {
            logger.error("Erro ao obter roles do usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Obter perfil completo do usuário",
            description = "Retorna o perfil completo do usuário autenticado (alias para /user-info)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtido com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioInfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioInfoDTO> obterPerfil() {
        logger.debug("Solicitação de perfil do usuário autenticado");
        return obterInformacoesUsuario(); // Reutilizar implementação existente
    }

    @GetMapping("/token-claims")
    @Operation(summary = "Obter claims do token JWT",
            description = "Retorna todos os claims presentes no token JWT do usuário",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Claims obtidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> obterClaimsToken() {
        logger.debug("Solicitação de claims do token JWT");

        try {
            Map<String, Object> claims = autenticacaoApiService.extrairClaimsDoToken();

            logger.debug("Claims do token retornados. Total: {}", claims.size());
            return ResponseEntity.ok(claims);

        } catch (Exception e) {
            logger.error("Erro ao obter claims do token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}