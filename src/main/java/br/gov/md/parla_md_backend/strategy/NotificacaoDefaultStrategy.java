package br.gov.md.parla_md_backend.strategy;

import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;
import br.gov.md.parla_md_backend.strategy.interfaces.NotificacaoStrategy;

public class NotificacaoDefaultStrategy implements NotificacaoStrategy {

    @Override
    public String createMessage(DocumentoLegislativo document) {
        return "Novo documento legislativo relevante: " + document.getEmenta();
    }
}
