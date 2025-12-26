package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.TipoParecer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SolicitarParecerDTO(
        @NotBlank(message = "ID do processo é obrigatório")
        String processoId,

        @NotBlank(message = "ID do setor emissor é obrigatório")
        String setorEmissorId,

        @NotBlank(message = "Nome do setor emissor é obrigatório")
        String setorEmissorNome,

        @NotNull(message = "Tipo de parecer é obrigatório")
        TipoParecer tipo,

        @NotBlank(message = "Assunto é obrigatório")
        String assunto,

        LocalDateTime prazo,
        String observacoes
) {}