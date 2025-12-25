package br.gov.md.parla_md_backend.domain.dto;


public record RankingParlamentarDTO(
        Integer posicao,
        String parlamentarId,
        String nomeParlamentar,
        String partido,
        String uf,
        String casa,
        Double score,
        String criterio,
        MetricasDesempenhoDTO metricas
) {}
