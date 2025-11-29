package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.service.UsuarioService;
import br.gov.md.parla_md_backend.domain.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/users")
public class UsuarioController {

    private UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> getAllUsers() {
        return ResponseEntity.ok(usuarioService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDTO> createUser(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.createUser(usuarioDTO));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UsuarioDTO> updateUser(@PathVariable String userId, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.updateUser(userId, usuarioDTO));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        usuarioService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
