package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;

public record ResultadoAnaliseParlamentarIA(
        String posicionamento,
        Double confiabilidade,
        String analiseDetalhada,
        String tendencia,
        List<String> padroesIdentificados,
        Double percentualCoerencia,
        String alinhamentoPolitico,
        String previsaoComportamento
) {}