package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPIsDTO {

    private Long totalDocumentosAtivos;

    private Long documentosNovosHoje;

    private Long documentosNovosEstaSemana;

    private Long documentosNovosEsteMes;

    private Double taxaCrescimentoSemanal;

    private Double taxaCrescimentoMensal;

    private Long documentosPendentesTriagem;

    private Long pareceresPendentes;

    private Long posicionamentosPendentes;

    private Double tempoMedioTramitacao;

    private Integer documentosComPrazoVencido;

    private Integer alertasCriticos;

    private Double eficienciaProcessamento;

    private String statusGeral;
}
