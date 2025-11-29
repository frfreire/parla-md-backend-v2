package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Encaminhamento;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.service.OpiniaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/pareceres")
public class ParecerController {

    private OpiniaoService opiniaoService;

    @PostMapping("/{propositionId}/iniciar")
    public ResponseEntity<Proposicao> iniciarParecer(@PathVariable String propositionId) {
        return ResponseEntity.ok(opiniaoService.iniciarParecer(propositionId));
    }

    @PutMapping("/{propositionId}/atualizar")
    public ResponseEntity<Proposicao> atualizarParecer(@PathVariable String propositionId, @RequestBody String parecer) {
        return ResponseEntity.ok(opiniaoService.atualizarParecer(propositionId, parecer));
    }

    @PostMapping("/{propositionId}/encaminhar")
    public ResponseEntity<Encaminhamento> encaminharParaSetor(
            @PathVariable String propositionId,
            @RequestParam String setorDestino,
            @RequestBody String solicitacao) {
        return ResponseEntity.ok(opiniaoService.encaminharParaSetor(propositionId, setorDestino, solicitacao));
    }

    @PutMapping("/encaminhamentos/{encaminhamentoId}/responder")
    public ResponseEntity<Encaminhamento> responderEncaminhamento(
            @PathVariable String encaminhamentoId,
            @RequestBody String resposta) {
        return ResponseEntity.ok(opiniaoService.responderEncaminhamento(encaminhamentoId, resposta));
    }

    @PostMapping("/{propositionId}/concluir")
    public ResponseEntity<Proposicao> concluirParecer(@PathVariable String propositionId) {
        return ResponseEntity.ok(opiniaoService.concluirParecer(propositionId));
    }

    @GetMapping("/{propositionId}/encaminhamentos")
    public ResponseEntity<List<Encaminhamento>> getEncaminhamentos(@PathVariable String propositionId) {
        return ResponseEntity.ok(opiniaoService.getEncaminhamentosByProposition(propositionId));
    }
}
