package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AprovarParecerDTO(
        @NotBlank(message = "ID do parecer é obrigatório")
        String parecerId,

        @NotNull(message = "Aprovação é obrigatória")
        Boolean aprovado,

        String observacoes
) {}