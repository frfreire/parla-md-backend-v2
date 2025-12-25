package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.RecomendacaoParecer;
import br.gov.md.parla_md_backend.domain.enums.TipoParecer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EmitirParecerDTO(
        @NotBlank(message = "ID do parecer é obrigatório")
        String parecerId,

        @NotNull(message = "Tipo de parecer é obrigatório")
        TipoParecer tipo,

        String contexto,

        @NotBlank(message = "Análise é obrigatória")
        String analise,

        @NotNull(message = "Recomendação é obrigatória")
        RecomendacaoParecer recomendacao,

        @NotBlank(message = "Justificativa é obrigatória")
        String justificativa,

        List<String> fundamentacaoLegal,
        List<String> impactosIdentificados,

        @NotBlank(message = "Conclusão é obrigatória")
        String conclusao
) {}