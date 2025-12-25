package br.gov.md.parla_md_backend.domain.dto;


public record MetricasDesempenhoDTO(
        Double taxaPresenca,
        Integer proposicoesApresentadas,
        Integer proposicoesAprovadas,
        Double taxaAprovacao,
        Integer discursosRealizados,
        Integer participacaoComissoes,
        Double indiceAtividade,
        Double indiceProdutividade,
        Integer rankingGeral,
        Integer rankingPartido,
        Integer rankingEstado
) {}
