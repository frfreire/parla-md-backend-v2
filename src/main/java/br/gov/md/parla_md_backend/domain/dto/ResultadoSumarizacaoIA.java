package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;

public record ResultadoSumarizacaoIA(
        String sumarioExecutivo,
        List<String> pontosPrincipais,
        List<String> entidadesRelevantes,
        List<String> palavrasChave,
        String temasPrincipais,
        String sentimentoGeral,
        String impactoEstimado
) {}