package br.gov.md.parla_md_backend.domain.interfaces;

import java.time.LocalDateTime;

public interface AnaliseIAEntity {

    String getId();

    LocalDateTime getDataAnalise();

    String getModeloVersao();

    String getPromptUtilizado();

    String getRespostaCompleta();

    Long getTempoProcessamentoMs();

    Boolean getSucesso();

    String getMensagemErro();

    LocalDateTime getDataExpiracao();

}
