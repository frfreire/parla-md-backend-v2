package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.service.ai.BehaviorAnalysisService;
import br.gov.md.parla_md_backend.service.ParlamentarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/parlamentares")
public class ParlamentarController {

    private BehaviorAnalysisService behaviorAnalysisService;
    private ParlamentarService parlamentarService;

    @Autowired
    public ParlamentarController(BehaviorAnalysisService behaviorAnalysisService, ParlamentarService parlamentarService) {
        this.behaviorAnalysisService = behaviorAnalysisService;
        this.parlamentarService = parlamentarService;
    }

    @GetMapping("/{nome}/comportamento")
    public double behaviorAnalysis(@PathVariable String nome, @RequestParam String tema) {
        return behaviorAnalysisService.behaviorAnalysis(nome, tema);
    }

    @PostMapping("/{nome}/registrar-voto")
    public void voteRegister(@PathVariable String nome, @RequestParam String tema, @RequestParam boolean votoAFavor) {
        behaviorAnalysisService.registerBehavior(nome, tema, votoAFavor);
    }

    @PostMapping("/{nome}/registrar-proposta")
    public void registrarProposta(@PathVariable String nome, @RequestParam String tema) {
        behaviorAnalysisService.behaviorAnalysis(nome, tema);
    }

    @GetMapping
    public ResponseEntity<Page<Parlamentar>> getAllParlamentarians(Pageable pageable) {
        Page<Parlamentar> parlamentarians = parlamentarService.getAllParlamentarians(pageable);
        return ResponseEntity.ok(parlamentarians);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getParlamentarianInfo(@PathVariable String id) {
        try {
            Parlamentar parlamentar = parlamentarService.getParlamentarianInfo(id);
            List<Proposicao> proposicaos = parlamentarService.getPropositionsByParlamentarian(id);
            String positionAbout = parlamentarService.getPositionAboutSpecificThemes(id);

            Map<String, Object> response = new HashMap<>();
            response.put("parlamentarian", parlamentar);
            response.put("propositions", proposicaos);
            response.put("positionAbout", positionAbout);
            response.put("photo", parlamentar.getUrlFoto());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/fetch-deputados")
    public ResponseEntity<List<Parlamentar>> fetchDeputados() {
        List<Parlamentar> deputados = parlamentarService.fetchAndSaveDeputados();
        return ResponseEntity.ok(deputados);
    }

    @PostMapping("/fetch-senadores")
    public ResponseEntity<List<Parlamentar>> fetchSenadores() {
        List<Parlamentar> senadores = parlamentarService.fetchAndSaveSenadores();
        return ResponseEntity.ok(senadores);
    }
}
