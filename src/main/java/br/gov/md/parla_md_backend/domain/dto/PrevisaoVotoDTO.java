package br.gov.md.parla_md_backend.domain.dto;


import br.gov.md.parla_md_backend.domain.enums.NivelConfianca;
import br.gov.md.parla_md_backend.domain.enums.TendenciaVoto;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PrevisaoVotoDTO(
        @NotBlank(message = "ID do parlamentar é obrigatório")
        String parlamentarId,

        String nomeParlamentar,
        String partido,
        String uf,

        @NotBlank(message = "Tema é obrigatório")
        String tema,

        TendenciaVoto tendenciaVoto,
        NivelConfianca nivelConfianca,
        Double probabilidadeFavoravel,
        Double probabilidadeContrario,
        Double probabilidadeAbstencao,
        String justificativa,
        List<String> fatoresInfluentes,
        Integer votacoesSimilaresAnalisadas,
        String recomendacaoEstrategica
) {}