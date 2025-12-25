package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SolicitacaoPosicionamentoDTO(
        @NotBlank(message = "ID do processo é obrigatório")
        String processoId,

        @NotBlank(message = "ID do órgão externo é obrigatório")
        String orgaoExternoId,

        @NotBlank(message = "Assunto é obrigatório")
        String assunto,

        LocalDateTime prazo,
        String observacoes
) {}