package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.dto.EncaminhamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.TramitacaoDTO;
import br.gov.md.parla_md_backend.service.TramitacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramitacoes")
@RequiredArgsConstructor
public class TramitacaoController {

    private final TramitacaoService tramitacaoService;

    @PostMapping("/encaminhar")
    public ResponseEntity<Tramitacao> encaminhar(
            @Valid @RequestBody EncaminhamentoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = userDetails.getUsername();
        Tramitacao tramitacao = tramitacaoService.encaminhar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(tramitacao);
    }

    @GetMapping("/recebidas")
    public ResponseEntity<Page<TramitacaoDTO>> buscarRecebidas(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        String usuarioId = userDetails.getUsername();
        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorDestinatario(usuarioId, pageable);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/enviadas")
    public ResponseEntity<Page<TramitacaoDTO>> buscarEnviadas(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        String usuarioId = userDetails.getUsername();
        Page<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorRemetente(usuarioId, pageable);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/processo/{processoId}")
    public ResponseEntity<List<TramitacaoDTO>> buscarPorProcesso(@PathVariable String processoId) {
        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarPorProcesso(processoId);
        return ResponseEntity.ok(tramitacoes);
    }

    @GetMapping("/urgentes")
    public ResponseEntity<List<TramitacaoDTO>> buscarUrgentes(
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = userDetails.getUsername();
        List<TramitacaoDTO> tramitacoes = tramitacaoService.buscarUrgentes(usuarioId);
        return ResponseEntity.ok(tramitacoes);
    }

    @PutMapping("/{tramitacaoId}/receber")
    public ResponseEntity<Tramitacao> receber(
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = userDetails.getUsername();
        Tramitacao tramitacao = tramitacaoService.receber(tramitacaoId, usuarioId);
        return ResponseEntity.ok(tramitacao);
    }

    @PutMapping("/{tramitacaoId}/concluir")
    public ResponseEntity<Tramitacao> concluir(
            @PathVariable String tramitacaoId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String usuarioId = userDetails.getUsername();
        Tramitacao tramitacao = tramitacaoService.concluir(tramitacaoId, usuarioId);
        return ResponseEntity.ok(tramitacao);
    }
}