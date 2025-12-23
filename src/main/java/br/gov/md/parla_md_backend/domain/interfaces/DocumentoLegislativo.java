package br.gov.md.parla_md_backend.domain.interfaces;

import java.time.LocalDateTime;

public interface DocumentoLegislativo {

    String getId();
    String getEmenta();
    String getAutor();
    String getPartidoAutor();
    String getEstadoAutor();
    String getTipo();
    LocalDateTime getDataApresentacao();
    String getSituacao();
    Double getProbabilidadeAprovacao();
    String getResultadoVotacao();
    String getTextoCompleto();
}
