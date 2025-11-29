package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Posicionamento;
import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
import br.gov.md.parla_md_backend.service.PosicionamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/posicionamentos")
public class PosicionamentoController {

    private PosicionamentoService posicionamentoService;

    @Autowired
    public PosicionamentoController(PosicionamentoService posicionamentoService) {
        this.posicionamentoService = posicionamentoService;
    }

    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('ANALISTA', 'GESTOR')")
    public ResponseEntity<Posicionamento> solicitarPosicionamento(
            @RequestParam String propositionId,
            @RequestParam String setorId,
            @AuthenticationPrincipal Jwt jwt) {
        String usuarioSolicitanteId = jwt.getSubject();
        return ResponseEntity.ok(posicionamentoService.solicitarPosicionamento(propositionId, setorId, usuarioSolicitanteId));
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('EXTERNO')")
    public ResponseEntity<Posicionamento> responderPosicionamento(
            @PathVariable String id,
            @RequestParam TipoPosicionamento tipo,
            @RequestParam String justificativa,
            @AuthenticationPrincipal Jwt jwt) {
        String usuarioRespondenteId = jwt.getSubject();
        return ResponseEntity.ok(posicionamentoService.responderPosicionamento(id, tipo, justificativa, usuarioRespondenteId));
    }

    @GetMapping("/proposition/{propositionId}")
    @PreAuthorize("hasAnyRole('ANALISTA', 'GESTOR', 'EXTERNO')")
    public ResponseEntity<List<Posicionamento>> getPosicionamentosByProposition(@PathVariable String propositionId) {
        return ResponseEntity.ok(posicionamentoService.getPosicionamentosByProposition(propositionId));
    }
}
