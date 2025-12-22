package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.NivelConfianca;
import br.gov.md.parla_md_backend.domain.enums.TendenciaVoto;

public record PosicionamentoTematicoDTO(
        String tema,
        TendenciaVoto tendenciaPredominante,
        Integer votacoesAnalisadas,
        Double percentualFavoravel,
        Double percentualContrario,
        Double percentualAbstencao,
        NivelConfianca nivelConfianca,
        String observacoes
) {}
