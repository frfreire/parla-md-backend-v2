package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;

public record ResultadoAnaliseImpactoIA(
        String nivelImpacto,
        String tipoImpacto,
        Double percentualImpacto,
        String analiseDetalhada,
        List<String> consequencias,
        List<String> gruposAfetados,
        List<String> riscos,
        List<String> oportunidades,
        String recomendacoes
) {}