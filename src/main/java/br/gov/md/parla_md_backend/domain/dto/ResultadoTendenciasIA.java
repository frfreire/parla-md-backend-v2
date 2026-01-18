package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;

public record ResultadoTendenciasIA(
        String analiseGeral,
        List<String> temasEmergentes,
        List<String> alertas,
        String previsaoProximoMes
) {}