package br.gov.md.parla_md_backend.controller;

import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;
import br.gov.md.parla_md_backend.service.ai.LegislativeAssistantAIService;
import br.gov.md.parla_md_backend.service.ai.NotificationService;
import br.gov.md.parla_md_backend.service.ai.DashboardService;
import br.gov.md.parla_md_backend.service.ai.AmendmentSuggestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistente")
public class AssistenteLegislativoController {

    private final LegislativeAssistantAIService legislativeAssistantAIService;

    public AssistenteLegislativoController(LegislativeAssistantAIService legislativeAssistantAIService) {
        this.legislativeAssistantAIService = legislativeAssistantAIService;
    }

    @GetMapping("/analise/{tipo}/{id}")
    public ResponseEntity<String> obterAnaliseAbrangente(@PathVariable String tipo, @PathVariable String id) {
        try {
            String analise = legislativeAssistantAIService.generateComprehensiveAnalysis(id, tipo);
            return ResponseEntity.ok(analise);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao gerar análise: " + e.getMessage());
        }
    }

    @GetMapping("/comparacao")
    public ResponseEntity<String> obterAnaliseComparativa(@RequestParam String id1, @RequestParam String id2) {
        try {
            String comparacao = legislativeAssistantAIService.generateComparativeAnalysis(id1, id2);
            return ResponseEntity.ok(comparacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao comparar documentos: " + e.getMessage());
        }
    }

    @GetMapping("/recomendacoes/{userId}")
    public ResponseEntity<List<DocumentoLegislativo>> obterRecomendacoesPersonalizadas(@PathVariable String userId) {
        try {
            List<DocumentoLegislativo> recomendacoes = legislativeAssistantAIService.getPersonalizedRecommendations(userId);
            return ResponseEntity.ok(recomendacoes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/notificacoes")
    public ResponseEntity<List<NotificationService.Notification>> obterNotificacoes() {
        try {
            List<NotificationService.Notification> notificacoes = legislativeAssistantAIService.generateNotifications();
            return ResponseEntity.ok(notificacoes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/metricas-dashboard")
    public ResponseEntity<DashboardService.DashboardMetrics> obterMetricasDashboard() {
        try {
            DashboardService.DashboardMetrics metricas = legislativeAssistantAIService.getDashboardMetrics();
            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/tendencias")
    public ResponseEntity<List<String>> obterTendenciasAtuais() {
        try {
            List<String> tendencias = legislativeAssistantAIService.analyzeCurrentTrends();
            return ResponseEntity.ok(tendencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/sugestoes-emendas/{id}")
    public ResponseEntity<List<AmendmentSuggestionService.AmendmentSuggestion>> obterSugestoesEmendas(@PathVariable String id) {
        try {
            List<AmendmentSuggestionService.AmendmentSuggestion> sugestoes = legislativeAssistantAIService.suggestAmendments(id);
            return ResponseEntity.ok(sugestoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/agenda-prioritaria")
    public ResponseEntity<List<DocumentoLegislativo>> obterAgendaPrioritaria() {
        try {
            List<DocumentoLegislativo> agenda = legislativeAssistantAIService.suggestPrioritizedAgenda();
            return ResponseEntity.ok(agenda);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}