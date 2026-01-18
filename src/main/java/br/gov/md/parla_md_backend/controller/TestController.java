package br.gov.md.parla_md_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/teste")
@RequiredArgsConstructor
@Tag(name = "Teste", description = "Endpoints para testes de conectividade e autenticação")
@SecurityRequirement(name = "bearer-jwt")
public class TestController {

    @GetMapping("/publico")
    @Operation(
            summary = "Teste de endpoint público",
            description = "Endpoint público para verificar conectividade da API sem autenticação"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API online e funcionando")
    })
    public ResponseEntity<Map<String, Object>> testePublico() {
        log.debug("Endpoint público de teste acessado");

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "online");
        resposta.put("mensagem", "API Parla-MD funcionando corretamente");
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("versao", "1.0.0");

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/protegido")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'EXTERNO', 'VIEWER')")
    @Operation(
            summary = "Teste de endpoint protegido",
            description = "Verifica autenticação JWT e autorização básica para todos os perfis"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação validada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão de acesso", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> testeProtegido(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Endpoint protegido acessado por usuário: {}", userDetails.getUsername());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "autenticado");
        resposta.put("usuario", userDetails.getUsername());
        resposta.put("roles", userDetails.getAuthorities());
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("mensagem", "Autenticação validada com sucesso");

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Teste de endpoint administrativo",
            description = "Verifica autorização de nível administrativo - acesso exclusivo para ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso administrativo validado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Apenas administradores têm acesso", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> testeAdmin(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Endpoint administrativo acessado por: {}", userDetails.getUsername());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "admin");
        resposta.put("usuario", userDetails.getUsername());
        resposta.put("nivel", "ADMINISTRADOR");
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("mensagem", "Acesso administrativo confirmado");

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/gestor")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(
            summary = "Teste de endpoint de gestão",
            description = "Verifica autorização para perfis de gestão - ADMIN e GESTOR"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso de gestão validado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Apenas gestores têm acesso", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> testeGestor(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Endpoint de gestão acessado por: {}", userDetails.getUsername());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "gestor");
        resposta.put("usuario", userDetails.getUsername());
        resposta.put("roles", userDetails.getAuthorities());
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("mensagem", "Acesso de gestão confirmado");

        return ResponseEntity.ok(resposta);
    }
}