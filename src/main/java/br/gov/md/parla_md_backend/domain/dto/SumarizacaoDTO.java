package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SumarizacaoDTO(
        @NotBlank(message = "ID da proposição é obrigatório")
        String proposicaoId,

        String tituloOriginal,
        String ementaOriginal,

        String resumoCurto,
        String resumoDetalhado,

        List<String> pontosChave,
        List<String> impactosIdentificados,
        List<String> areasAfetadas,

        String analiseRelevanciaDefesa,
        String recomendacao,

        String modeloUtilizado,
        Double confiancaSumarizacao
) {}
