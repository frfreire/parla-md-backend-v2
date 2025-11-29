package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.TriagemStatus;
import br.gov.md.parla_md_backend.service.TriagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/triagem")
public class TriagemController {

    private TriagemService triagemService;

    @Autowired
    public TriagemController(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    @GetMapping("/nao-avaliadas")
    public ResponseEntity<Page<Proposicao>> getProposicoesNaoAvaliadas(Pageable pageable) {
        return ResponseEntity.ok(triagemService.getProposicoesNaoAvaliadas(pageable));
    }

    @GetMapping("/interesse")
    public ResponseEntity<Page<Proposicao>> getProposicoesInteresse(Pageable pageable) {
        return ResponseEntity.ok(triagemService.getProposicoesInteresse(pageable));
    }

    @GetMapping("/descartadas")
    public ResponseEntity<Page<Proposicao>> getProposicoesDescartadas(Pageable pageable) {
        return ResponseEntity.ok(triagemService.getProposicoesDescartadas(pageable));
    }

    @PostMapping("/{id}/avaliar")
    public ResponseEntity<Proposicao> avaliarProposicao(
            @PathVariable String id,
            @RequestParam TriagemStatus novoStatus,
            @RequestParam(required = false) String observacao) {
        Proposicao proposicaoAvaliada = triagemService.avaliarProposicao(id, novoStatus, observacao);
        return ResponseEntity.ok(proposicaoAvaliada);
    }
}
