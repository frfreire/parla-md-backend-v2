package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para avaliação de triagem de proposição")
public record AvaliarTriagemDTO(

        @NotNull(message = "Status de triagem é obrigatório")
        @Schema(
                description = "Novo status de triagem da proposição",
                example = "INTERESSE",
                required = true
        )
        StatusTriagem novoStatus,

        @Size(max = 1000, message = "Observação não pode exceder 1000 caracteres")
        @Schema(
                description = "Observações sobre a avaliação realizada",
                example = "Proposição relevante para área de defesa cibernética",
                maxLength = 1000
        )
        String observacao
) {}