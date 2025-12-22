package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AnalisePreditivaDTO(
        @NotBlank(message = "ID da proposição é obrigatório")
        String proposicaoId,

        String descricaoProposicao,
        String temaPrincipal,

        Double probabilidadeAprovacao,
        String nivelConfiancaPredicao,

        List<PrevisaoVotoDTO> previsoesIndividuais,

        Integer totalParlamentaresFavoraveis,
        Integer totalParlamentaresContrarios,
        Integer totalParlamentaresIndecisos,

        List<String> fatoresCriticos,
        String recomendacaoEstrategica,
        String resumoExecutivo
) {}
