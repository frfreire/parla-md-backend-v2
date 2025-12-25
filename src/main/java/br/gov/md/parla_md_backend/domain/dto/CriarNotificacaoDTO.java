package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeNotificacao;
import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CriarNotificacaoDTO(
        @NotNull(message = "Tipo de notificação é obrigatório")
        TipoNotificacao tipo,

        @NotNull(message = "Prioridade é obrigatória")
        PrioridadeNotificacao prioridade,

        @NotBlank(message = "ID do destinatário é obrigatório")
        String destinatarioId,

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem,

        String mensagemDetalhada,
        String entidadeRelacionadaTipo,
        String entidadeRelacionadaId,
        String urlAcao,
        String textoAcao,
        Map<String, Object> dadosAdicionais,
        List<CanalNotificacao> canaisEnvio,
        Boolean agendar,
        LocalDateTime dataAgendamento,
        LocalDateTime dataExpiracao
) {}
