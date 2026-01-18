package br.gov.md.parla_md_backend.domain.dto;

public record ResultadoPrevisaoIA(
        Double probabilidade,
        Double confianca,
        String justificativa,
        String fatoresPositivos,
        String fatoresNegativos
) {}