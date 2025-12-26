package br.gov.md.parla_md_backend.strategy;

import br.gov.md.parla_md_backend.domain.interfaces.DocumentoLegislativo;
import br.gov.md.parla_md_backend.strategy.interfaces.NotificacaoStrategy;

public class NotificacaoProposicaoStrategy implements NotificacaoStrategy {

    @Override
    public String createMessage(DocumentoLegislativo document) {
        return "Nova proposição relevante: " + document.getEmenta();
    }

}
