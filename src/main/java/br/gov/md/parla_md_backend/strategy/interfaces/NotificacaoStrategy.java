package br.gov.md.parla_md_backend.strategy.interfaces;

import br.gov.md.parla_md_backend.domain.interfaces.DocumentoLegislativo;

public interface NotificacaoStrategy {

    String createMessage(DocumentoLegislativo document);
}
