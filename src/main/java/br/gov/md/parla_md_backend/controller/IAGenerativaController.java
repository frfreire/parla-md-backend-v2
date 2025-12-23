package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.service.ai.AmendmentSuggestionService;
import br.gov.md.parla_md_backend.service.ai.DashboardService;
import br.gov.md.parla_md_backend.service.ai.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class IAGenerativaController {
    /** TODO Remover esta classe - Ela foi substituída por LegislativeAssistantController*/
    private final AdvancedGenerativeAIService aiService;

    @Autowired
    public IAGenerativaController(AdvancedGenerativeAIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/summary")
    public ResponseEntity<String> generateSummary(@RequestBody String propositionText) {
        try {
            String summary = aiService.generateSummary(propositionText);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar sumário: " + e.getMessage());
        }
    }

    @PostMapping("/impact-analysis")
    public ResponseEntity<String> generateImpactAnalysis(@RequestBody String propositionText) {
        try {
            String analysis = aiService.generateImpactAnalysis(propositionText);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar análise de impacto: " + e.getMessage());
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationService.Notification>> getNotifications() {
        return ResponseEntity.ok(aiService.generateNotifications());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardService.DashboardMetrics> getDashboardMetrics() {
        return ResponseEntity.ok(aiService.getDashboardMetrics());
    }

    @GetMapping("/trends")
    public ResponseEntity<List<String>> getCurrentTrends() {
        return ResponseEntity.ok(aiService.analyzeCurrentTrends());
    }

    @GetMapping("/suggest-amendments/{documentId}")
    public ResponseEntity<List<AmendmentSuggestionService.AmendmentSuggestion>> suggestAmendments(@PathVariable String documentId) {
        try {
            return ResponseEntity.ok(aiService.suggestAmendments(documentId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
