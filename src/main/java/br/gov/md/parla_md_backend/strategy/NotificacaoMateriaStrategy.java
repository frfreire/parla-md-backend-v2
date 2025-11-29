package br.gov.md.parla_md_backend.strategy;

import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;
import br.gov.md.parla_md_backend.strategy.interfaces.NotificacaoStrategy;

public class NotificacaoMateriaStrategy implements NotificacaoStrategy {
    @Override
    public String createMessage(DocumentoLegislativo document) {
        return "Nova matéria relevante: " + document.getEmenta();
    }
}
