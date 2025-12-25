package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasDesempenho {

    private double taxaPresenca;

    private int proposicoesApresentadas;

    private int proposicoesAprovadas;

    private double taxaAprovacao;

    private int discursosRealizados;

    private int participacaoComissoes;

    private double indiceAtividade;

    private double indiceProdutividade;

    private Integer rankingGeral;

    private Integer rankingPartido;

    private Integer rankingEstado;

}