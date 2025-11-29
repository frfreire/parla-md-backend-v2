package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.service.ProcessamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/tramitacao")
public class TramitacaoController {

    private ProcessamentoService processamentoService;

    @Autowired
    public TramitacaoController(ProcessamentoService processamentoService) {
        this.processamentoService = processamentoService;
    }

    @PostMapping("/{propositionId}/parecer")
    public ResponseEntity<Opiniao> emitirParecer(
            @PathVariable String propositionId,
            @RequestBody String conteudo,
            @RequestHeader("Usuario-Id") String usuarioId,
            @RequestHeader("Setor") String setorEmissor) {
        return ResponseEntity.ok(processamentoService.emitirParecer(propositionId, conteudo, usuarioId, setorEmissor));
    }

    @PostMapping("/{propositionId}/despacho")
    public ResponseEntity<Despacho> emitirDespacho(
            @PathVariable String propositionId,
            @RequestBody String conteudo,
            @RequestHeader("Usuario-Id") String usuarioId,
            @RequestHeader("Setor") String setorOrigem,
            @RequestParam String setorDestino) {
        return ResponseEntity.ok(processamentoService.emitirDespacho(propositionId, conteudo, usuarioId, setorOrigem, setorDestino));
    }

    @GetMapping("/{propositionId}/pareceres")
    public ResponseEntity<List<Opiniao>> getPareceres(@PathVariable String propositionId) {
        return ResponseEntity.ok(processamentoService.getPareceresByProposition(propositionId));
    }

    @GetMapping("/{propositionId}/despachos")
    public ResponseEntity<List<Despacho>> getDespachos(@PathVariable String propositionId) {
        return ResponseEntity.ok(processamentoService.getDespachosByProposition(propositionId));
    }

    @GetMapping("/{propositionId}")
    public ResponseEntity<Proposicao> getProposition(
            @PathVariable String propositionId,
            @RequestHeader("Setor") String setorUsuario) {
        return ResponseEntity.ok(processamentoService.getProposition(propositionId, setorUsuario));
    }
}
