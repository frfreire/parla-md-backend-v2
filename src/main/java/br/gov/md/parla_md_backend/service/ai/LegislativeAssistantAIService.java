package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.domain.legislativo.Materia;
import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.repository.*;
import br.gov.md.parla_md_backend.service.AreaImpactoService;
import br.gov.md.parla_md_backend.util.NLPUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.sentiment.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LegislativeAssistantAIService {

    private final AreaImpactoService areaImpactoService;
    private final IProposicaoRepository propositionRepository;
    private final IMateriaRepository matterRepository;
    private final IUsuarioRepository userRepository;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final AmendmentSuggestionService amendmentSuggestionService;
    private StanfordCoreNLP pipeline;
    private Cache<String, Map<String, List<String>>> impactAreasCache;

    @Autowired
    public LegislativeAssistantAIService(AreaImpactoService areaImpactoService,
                                         IProposicaoRepository propositionRepository,
                                         IMateriaRepository matterRepository,
                                         IUsuarioRepository userRepository,
                                         NotificationService notificationService,
                                         DashboardService dashboardService,
                                         AmendmentSuggestionService amendmentSuggestionService) {
        this.areaImpactoService = areaImpactoService;
        this.propositionRepository = propositionRepository;
        this.matterRepository = matterRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.dashboardService = dashboardService;
        this.amendmentSuggestionService = amendmentSuggestionService;

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,sentiment");
        props.setProperty("ner.useSUTime", "false");
        pipeline = new StanfordCoreNLP(props);

        impactAreasCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(100)
                .build();
    }

    @PostConstruct
    public void initializeImpactAreas() {
        updateImpactAreasCache();
    }

    private void updateImpactAreasCache() {
        List<AreaImpacto> areaImpactos = areaImpactoService.lisarTodasAreasImpacto();
        Map<String, List<String>> impactAreasMap = areaImpactos.stream()
                .collect(Collectors.toMap(AreaImpacto::getName, AreaImpacto::getKeywords));
        impactAreasCache.put("impactAreas", impactAreasMap);
    }

    @EventListener
    public void handleImpactAreaChangeEvent(AfterSaveEvent<AreaImpacto> event) {
        updateImpactAreasCache();
    }

    @EventListener
    public void handleImpactAreaDeleteEvent(AfterDeleteEvent<AreaImpacto> event) {
        updateImpactAreasCache();
    }

    public String generateComprehensiveAnalysis(String documentId, String documentType) {
        DocumentoLegislativo document = findDocument(documentId, documentType);
        if (document == null) {
            return "Documento não encontrado.";
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append(generateSummary(document)).append("\n\n");
        analysis.append(generateImpactAnalysis(document)).append("\n\n");
        analysis.append(generateAdditionalInfo(document));

        return analysis.toString();
    }

    public String generateComparativeAnalysis(String documentId1, String documentId2) {
        DocumentoLegislativo doc1 = findDocument(documentId1, null);
        DocumentoLegislativo doc2 = findDocument(documentId2, null);

        if (doc1 == null || doc2 == null) {
            return "Um ou ambos os documentos não foram encontrados.";
        }

        StringBuilder analysis = new StringBuilder("Análise Comparativa:\n\n");
        analysis.append("1. Comparação de Ementas:\n");
        analysis.append("Documento 1: ").append(doc1.getEmenta()).append("\n");
        analysis.append("Documento 2: ").append(doc2.getEmenta()).append("\n\n");

        analysis.append("2. Autoria:\n");
        analysis.append("Documento 1: ").append(doc1.getAutor()).append(" (").append(doc1.getPartidoAutor()).append(")\n");
        analysis.append("Documento 2: ").append(doc2.getAutor()).append(" (").append(doc2.getPartidoAutor()).append(")\n\n");

        analysis.append("3. Probabilidade de Aprovação:\n");
        analysis.append("Documento 1: ").append(String.format("%.2f%%", doc1.getProbabilidadeAprovacao() * 100)).append("\n");
        analysis.append("Documento 2: ").append(String.format("%.2f%%", doc2.getProbabilidadeAprovacao() * 100)).append("\n\n");

        analysis.append("4. Análise de Similaridade:\n");
        analysis.append(NLPUtil.analyzeSimilarity(doc1.getTextoCompleto(), doc2.getTextoCompleto())).append("\n\n");

        analysis.append("5. Temas Principais:\n");
        analysis.append("Documento 1: ").append(NLPUtil.extractMainTheme(doc1.getTextoCompleto())).append("\n");
        analysis.append("Documento 2: ").append(NLPUtil.extractMainTheme(doc2.getTextoCompleto())).append("\n");

        return analysis.toString();
    }

    public List<DocumentoLegislativo> getPersonalizedRecommendations(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<String> userInterests = usuario.getInterests();
        if (userInterests == null || userInterests.isEmpty()) {
            return new ArrayList<>();
        }

        List<DocumentoLegislativo> allDocuments = new ArrayList<>();
        allDocuments.addAll(propositionRepository.findAll());
        allDocuments.addAll(matterRepository.findAll());

        return allDocuments.stream()
                .filter(doc -> userInterests.stream()
                        .anyMatch(interest -> doc.getEmenta().toLowerCase().contains(interest.toLowerCase())))
                .sorted((d1, d2) -> Double.compare(d2.getProbabilidadeAprovacao(), d1.getProbabilidadeAprovacao()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<NotificationService.Notification> generateNotifications() {
        List<DocumentoLegislativo> recentDocuments = getRecentDocuments(7);
        List<Usuario> allUsuarios = userRepository.findAll();
        return notificationService.generateNotifications(recentDocuments, allUsuarios);
    }

    public DashboardService.DashboardMetrics getDashboardMetrics() {
        return dashboardService.generateMetrics();
    }

    public List<String> analyzeCurrentTrends() {
        return dashboardService.analyzeTrends();
    }

    public List<AmendmentSuggestionService.AmendmentSuggestion> suggestAmendments(String documentId) {
        DocumentoLegislativo document = findDocument(documentId, null);
        if (document == null) {
            throw new IllegalArgumentException("Documento não encontrado");
        }
        return amendmentSuggestionService.generateAmendmentSuggestions(document);
    }

    public List<DocumentoLegislativo> suggestPrioritizedAgenda() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<DocumentoLegislativo> recentDocuments = new ArrayList<>();
        recentDocuments.addAll(propositionRepository.findByDataApresentacaoAfter(oneWeekAgo));
        recentDocuments.addAll(matterRepository.findByDataApresentacaoAfter(oneWeekAgo));

        return recentDocuments.stream()
                .sorted((d1, d2) -> Double.compare(d2.getProbabilidadeAprovacao(), d1.getProbabilidadeAprovacao()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private String generateSummary(DocumentoLegislativo document) {
        String summary = "Sumário do Documento Legislativo:\n";
        summary += "ID: " + document.getId() + "\n";
        summary += "Ementa: " + document.getEmenta() + "\n";
        summary += "Autor: " + document.getAutor() + " (" + document.getPartidoAutor() + " - " + document.getEstadoAutor() + ")\n";
        summary += "Tipo: " + document.getTipo() + "\n";
        summary += "Data de Apresentação: " + document.getDataApresentacao() + "\n";

        Annotation doc = new Annotation(document.getTextoCompleto());
        pipeline.annotate(doc);

        List<String> entities = new ArrayList<>();
        List<String> keyPhrases = new ArrayList<>();
        String overallSentiment = "";

        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (!"O".equals(ner)) {
                    entities.add(token.word() + " (" + ner + ")");
                }
                if ("NN".equals(token.get(CoreAnnotations.PartOfSpeechAnnotation.class))) {
                    keyPhrases.add(token.word());
                }
            }
            overallSentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        }

        summary += "Principais tópicos: " + String.join(", ", keyPhrases.subList(0, Math.min(3, keyPhrases.size()))) + "\n";
        summary += "Entidades relevantes: " + String.join(", ", entities.subList(0, Math.min(5, entities.size()))) + "\n";
        summary += "Tom geral: " + translateSentiment(overallSentiment) + "\n";

        return summary;
    }

    private String generateImpactAnalysis(DocumentoLegislativo document) {
        StringBuilder analysis = new StringBuilder("Análise de Impacto:\n");
        Map<String, Double> impacts = areaImpactoService.analyzeImpact(document.getTextoCompleto());

        for (Map.Entry<String, Double> entry : impacts.entrySet()) {
            analysis.append(entry.getKey()).append(": ")
                    .append(String.format("%.2f%%", entry.getValue() * 100))
                    .append(" de impacto\n");
        }

        return analysis.toString();
    }

    private String generateAdditionalInfo(DocumentoLegislativo document) {
        StringBuilder info = new StringBuilder("Informações Adicionais:\n");
        info.append("Situação atual: ").append(document.getSituacao()).append("\n");

        if (document.getResultadoVotacao() != null) {
            info.append("Resultado da votação: ").append(document.getResultadoVotacao()).append("\n");
        } else if (document.getProbabilidadeAprovacao() != null) {
            info.append("Probabilidade de aprovação: ")
                    .append(String.format("%.2f%%", document.getProbabilidadeAprovacao() * 100)).append("\n");
        }

        return info.toString();
    }

    private DocumentoLegislativo findDocument(String id, String type) {
        if ("proposition".equals(type) || type == null) {
            DocumentoLegislativo doc = propositionRepository.findById(id).orElse(null);
            if (doc != null) return doc;
        }
        if ("senate_matter".equals(type) || type == null) {
            return matterRepository.findById(id).orElse(null);
        }
        return null;
    }

    private List<DocumentoLegislativo> getRecentDocuments(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Proposicao> recentProposicaos = propositionRepository.findByDataApresentacaoAfter(startDate);
        List<Materia> recentMaterias = matterRepository.findByDataApresentacaoAfter(startDate);

        List<DocumentoLegislativo> allDocuments = new ArrayList<>();
        allDocuments.addAll(recentProposicaos);
        allDocuments.addAll(recentMaterias);

        return allDocuments;
    }

    private String translateSentiment(String sentiment) {
        switch (sentiment) {
            case "Very positive": return "muito positivo";
            case "Positive": return "positivo";
            case "Neutral": return "neutro";
            case "Negative": return "negativo";
            case "Very negative": return "muito negativo";
            default: return "incerto";
        }
    }
}