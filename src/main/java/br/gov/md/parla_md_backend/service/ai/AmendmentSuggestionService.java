package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AmendmentSuggestionService {

    public record AmendmentSuggestion(String originalText, String suggestedText, String justification) {}

    public List<AmendmentSuggestion> generateAmendmentSuggestions(DocumentoLegislativo document) {
        List<AmendmentSuggestion> suggestions = new ArrayList<>();

        // Aqui você implementaria a lógica real de geração de sugestões de emendas
        // Este é um exemplo simplificado
        String texto = document.getTextoCompleto();
        if (texto.contains("prazo de 30 dias")) {
            suggestions.add(new AmendmentSuggestion(
                    "prazo de 30 dias",
                    "prazo de 45 dias",
                    "Aumentar o prazo para permitir uma análise mais completa."
            ));
        }

        if (texto.contains("multa de 10%")) {
            suggestions.add(new AmendmentSuggestion(
                    "multa de 10%",
                    "multa de 5%",
                    "Reduzir a multa para tornar a penalidade mais proporcional."
            ));
        }

        return suggestions;
    }
}
