package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.domain.legislativo.Materia;
import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.repository.IMateriaRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
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
public class AdvancedGenerativeAIService {
/** TODO Remover esta classe - Ela foi substituída por Legislative AssistantAIService*/
    private final AreaImpactoService areaImpactoService;
    private StanfordCoreNLP pipeline;
    private Cache<String, Map<String, List<String>>> impactAreasCache;
    private final IProposicaoRepository propositionRepository;
    private final IMateriaRepository matterRepository;
    private final IUsuarioRepository userRepository;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final AmendmentSuggestionService amendmentSuggestionService;


    @Autowired
    public AdvancedGenerativeAIService(AreaImpactoService areaImpactoService,
                                       IProposicaoRepository propositionRepository,
                                       IMateriaRepository matterRepository,
                                       NotificationService notificationService,
                                       IUsuarioRepository userRepository,
                                       DashboardService dashboardService,
                                       AmendmentSuggestionService amendmentSuggestionService) {

        this.areaImpactoService = areaImpactoService;
        this.propositionRepository = propositionRepository;
        this.matterRepository = matterRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
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

    /**
     * Inicializa o cache de áreas de impacto após a construção do bean.
     */
    @PostConstruct
    public void initializeImpactAreas() {
        updateImpactAreasCache();
    }

    /**
     * Atualiza o cache com as áreas de impacto mais recentes do banco de dados.
     */
    private void updateImpactAreasCache() {
        List<AreaImpacto> areaImpactos = areaImpactoService.lisarTodasAreasImpacto();
        Map<String, List<String>> impactAreasMap = areaImpactos.stream()
                .collect(Collectors.toMap(AreaImpacto::getName, AreaImpacto::getKeywords));
        impactAreasCache.put("impactAreas", impactAreasMap);
    }

    /**
     * Listener para eventos de salvamento de área de impacto.
     * Atualiza o cache quando uma área de impacto é salva ou atualizada.
     *
     * @param event Evento de salvamento.
     */
    @EventListener
    public void handleImpactAreaChangeEvent(AfterSaveEvent<AreaImpacto> event) {
        updateImpactAreasCache();
    }

    /**
     * Listener para eventos de exclusão de área de impacto.
     * Atualiza o cache quando uma área de impacto é excluída.
     *
     * @param event Evento de exclusão.
     */
    @EventListener
    public void handleImpactAreaDeleteEvent(AfterDeleteEvent<AreaImpacto> event) {
        updateImpactAreasCache();
    }

    /**
     * Obtém as áreas de impacto do cache, atualizando-o se necessário.
     *
     * @return Mapa de áreas de impacto e suas palavras-chave.
     */
    private Map<String, List<String>> getImpactAreas() {
        return impactAreasCache.get("impactAreas", k -> {
            updateImpactAreasCache();
            return impactAreasCache.getIfPresent("impactAreas");
        });
    }

    /**
     * Gera um sumário da proposição baseado em análise de NLP.
     *
     * @param propositionText Texto da proposição.
     * @return Sumário gerado.
     */
    public String generateSummary(String propositionText) {
        Annotation document = new Annotation(propositionText);
        pipeline.annotate(document);

        List<String> entities = new ArrayList<>();
        List<String> keyPhrases = new ArrayList<>();
        String overallSentiment = "";

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
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

        String summary = "Sumário da Proposição:\n";
        summary += "Esta proposição trata principalmente de " + String.join(", ", keyPhrases.subList(0, Math.min(3, keyPhrases.size()))) + ".\n";
        summary += "Entidades relevantes mencionadas: " + String.join(", ", entities.subList(0, Math.min(5, entities.size()))) + ".\n";
        summary += "O tom geral da proposição parece ser " + translateSentiment(overallSentiment) + ".\n";

        return summary;
    }

    /**
     * Gera uma análise de impacto da proposição baseada nas áreas de impacto definidas.
     *
     * @param propositionText Texto da proposição.
     * @return Análise de impacto gerada.
     */
    public String generateImpactAnalysis(String propositionText) {
        Map<String, List<String>> impactAreas = getImpactAreas();
        Map<String, Integer> areaMatches = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : impactAreas.entrySet()) {
            int count = countMatches(propositionText.toLowerCase(), entry.getValue());
            if (count > 0) {
                areaMatches.put(entry.getKey(), count);
            }
        }

        String analysis = "Análise de Impacto:\n";
        for (Map.Entry<String, Integer> entry : areaMatches.entrySet()) {
            analysis += "Impacto na área de " + entry.getKey() + ":\n";
            analysis += generateImpactStatement(entry.getKey(), entry.getValue()) + "\n";
        }

        return analysis;
    }

    /**
     * Gera uma declaração de impacto para uma área específica.
     *
     * @param area Área de impacto.
     * @param relevance Nível de relevância (baseado no número de matches).
     * @return Declaração de impacto gerada.
     */
    private String generateImpactStatement(String area, int relevance) {
        String[] lowImpact = {"pode ter um impacto limitado", "pode afetar marginalmente", "pode ter consequências menores"};
        String[] mediumImpact = {"provavelmente terá um impacto significativo", "pode afetar consideravelmente", "pode trazer mudanças notáveis"};
        String[] highImpact = {"terá um impacto substancial", "afetará profundamente", "trará mudanças significativas"};

        String[] impactStatements = relevance > 3 ? highImpact : (relevance > 1 ? mediumImpact : lowImpact);
        String statement = impactStatements[new Random().nextInt(impactStatements.length)];

        return "Esta proposição " + statement + " na área de " + area.toLowerCase() + ".";
    }

    /**
     * Conta o número de matches de palavras-chave no texto.
     *
     * @param text Texto a ser analisado.
     * @param keywords Lista de palavras-chave.
     * @return Número de matches encontrados.
     */
    private int countMatches(String text, List<String> keywords) {
        return (int) keywords.stream().filter(text::contains).count();
    }

    /**
     * Traduz o sentimento do inglês para o português.
     *
     * @param sentiment Sentimento em inglês.
     * @return Sentimento traduzido para o português.
     */
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

    private DocumentoLegislativo findDocument(String id) {

        Optional<Proposicao> proposition = propositionRepository.findById(id);
        if (proposition.isPresent()) {
            return proposition.get();
        }

        Optional<Materia> matter = matterRepository.findById(id);
        if (matter.isPresent()) {
            return matter.get();
        }

        return null;
    }

    public List<NotificationService.Notification> generateNotifications() {
        List<DocumentoLegislativo> recentDocuments = dashboardService.getRecentDocuments(7);
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
        DocumentoLegislativo document = findDocument(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Documento não encontrado");
        }
        return amendmentSuggestionService.generateAmendmentSuggestions(document);
    }

    public String generateComparativeAnalysis(String documentId1, String documentId2) {
        DocumentoLegislativo doc1 = findDocument(documentId1);
        DocumentoLegislativo doc2 = findDocument(documentId2);

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
        String similarityResult = NLPUtil.analyzeSimilarity(doc1.getTextoCompleto(), doc2.getTextoCompleto());
        analysis.append(similarityResult).append("\n\n");

        analysis.append("5. Temas Principais:\n");
        analysis.append("Documento 1: ").append(NLPUtil.extractMainTheme(doc1.getTextoCompleto())).append("\n");
        analysis.append("Documento 2: ").append(NLPUtil.extractMainTheme(doc2.getTextoCompleto())).append("\n");

        return analysis.toString();
    }
}
