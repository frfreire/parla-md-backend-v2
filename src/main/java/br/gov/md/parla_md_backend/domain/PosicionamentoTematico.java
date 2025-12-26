package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.NivelConfianca;
import br.gov.md.parla_md_backend.domain.enums.TendenciaVoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosicionamentoTematico {

    private String tema;

    private TendenciaVoto tendenciaPredominante;

    private int votacoesAnalisadas;

    private double percentualFavoravel;

    private double percentualContrario;

    private double percentualAbstencao;

    private NivelConfianca nivelConfianca;

    private String observacoes;

}