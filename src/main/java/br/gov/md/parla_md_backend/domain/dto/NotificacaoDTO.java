package br.gov.md.parla_md_backend.domain.dto;


import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeNotificacao;
import br.gov.md.parla_md_backend.domain.enums.StatusNotificacao;
import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record NotificacaoDTO(
        String id,
        TipoNotificacao tipo,
        PrioridadeNotificacao prioridade,
        StatusNotificacao status,
        String destinatarioNome,
        String titulo,
        String mensagem,
        String urlAcao,
        String textoAcao,
        Map<String, Object> dadosAdicionais,
        List<CanalNotificacao> canaisEnvio,
        LocalDateTime dataCriacao,
        LocalDateTime dataLeitura,
        boolean lida
) {
    public NotificacaoDTO {
        lida = status == StatusNotificacao.LIDA;
    }
}