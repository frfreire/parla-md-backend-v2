package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.repository.IMateriaRepository;
import br.gov.md.parla_md_backend.util.NLPUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final IProposicaoRepository propositionRepository;
    private final IMateriaRepository matterRepository;

    public DashboardService(IProposicaoRepository propositionRepository, IMateriaRepository matterRepository) {
        this.propositionRepository = propositionRepository;
        this.matterRepository = matterRepository;
    }

    public record DashboardMetrics(
            long totalDocuments,
            Map<String, Long> documentsByType,
            Map<String, Long> documentsByAuthorParty,
            double averageApprovalProbability
    ) {}

    public DashboardMetrics generateMetrics() {
        List<DocumentoLegislativo> allDocuments = getAllDocuments();

        long total = allDocuments.size();
        Map<String, Long> byType = allDocuments.stream()
                .collect(Collectors.groupingBy(DocumentoLegislativo::getTipo, Collectors.counting()));
        Map<String, Long> byParty = allDocuments.stream()
                .collect(Collectors.groupingBy(DocumentoLegislativo::getPartidoAutor, Collectors.counting()));
        double avgProbability = allDocuments.stream()
                .mapToDouble(DocumentoLegislativo::getProbabilidadeAprovacao)
                .average()
                .orElse(0.0);

        return new DashboardMetrics(total, byType, byParty, avgProbability);
    }

    private List<DocumentoLegislativo> getAllDocuments() {
        return Stream.concat(
                propositionRepository.findAll().stream(),
                matterRepository.findAll().stream()
        ).toList();
    }

    public List<DocumentoLegislativo> getRecentDocuments(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return Stream.concat(
                propositionRepository.findByDataApresentacaoAfter(startDate).stream(),
                matterRepository.findByDataApresentacaoAfter(startDate).stream()
        ).toList();
    }

    private String extractMainTheme(String ementa) {
        // Implementar lógica para extrair o tema principal da ementa
        // Esta é uma implementação simplificada
        return ementa.split(" ")[0];
    }

    public List<String> analyzeTrends() {
        List<DocumentoLegislativo> recentDocuments = getRecentDocuments(30);

        Map<String, Long> trendingThemes = recentDocuments.stream()
                .collect(Collectors.groupingBy(
                        doc -> NLPUtil.extractMainTheme(doc.getEmenta()),
                        Collectors.counting()
                ));

        return trendingThemes.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> "Tema em alta: " + e.getKey() + " (Mencionado em " + e.getValue() + " documentos)")
                .toList();
    }
}
