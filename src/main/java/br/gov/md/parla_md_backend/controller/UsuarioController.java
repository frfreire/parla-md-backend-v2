package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import br.gov.md.parla_md_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para consulta de usuários do Keycloak")
@SecurityRequirement(name = "bearer-jwt")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable String id) {
        log.debug("Buscando usuário: {}", id);
        UsuarioDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar usuário por email")
    public ResponseEntity<UsuarioDTO> buscarPorEmail(@PathVariable String email) {
        log.debug("Buscando usuário por email: {}", email);
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar usuário por username")
    public ResponseEntity<UsuarioDTO> buscarPorUsername(@PathVariable String username) {
        log.debug("Buscando usuário por username: {}", username);
        UsuarioDTO usuario = usuarioService.buscarPorUsername(username);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        log.debug("Listando todos os usuários");
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Listar usuários ativos")
    public ResponseEntity<List<UsuarioDTO>> listarAtivos() {
        log.debug("Listando usuários ativos");
        List<UsuarioDTO> usuarios = usuarioService.listarAtivos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Buscar usuários por nome")
    public ResponseEntity<List<UsuarioDTO>> buscarPorNome(@RequestParam String nome) {
        log.debug("Buscando usuários por nome: {}", nome);
        List<UsuarioDTO> usuarios = usuarioService.buscarPorNome(nome);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários por role")
    public ResponseEntity<List<UsuarioDTO>> listarPorRole(@PathVariable String roleName) {
        log.debug("Listando usuários com role: {}", roleName);
        List<UsuarioDTO> usuarios = usuarioService.listarPorRole(roleName);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Obter roles do usuário")
    public ResponseEntity<List<String>> obterRoles(@PathVariable String id) {
        log.debug("Obtendo roles do usuário: {}", id);
        List<String> roles = usuarioService.obterRolesUsuario(id);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}/possui-role/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Verificar se usuário possui role")
    public ResponseEntity<Boolean> possuiRole(
            @PathVariable String id,
            @PathVariable String roleName) {
        log.debug("Verificando se usuário {} possui role: {}", id, roleName);
        boolean possui = usuarioService.usuarioPossuiRole(id, roleName);
        return ResponseEntity.ok(possui);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Contar total de usuários")
    public ResponseEntity<Long> contarUsuarios() {
        log.debug("Contando usuários");
        long total = usuarioService.contarUsuarios();
        return ResponseEntity.ok(total);
    }
}