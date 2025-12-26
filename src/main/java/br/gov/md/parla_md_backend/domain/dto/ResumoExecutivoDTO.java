package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoExecutivoDTO {

    private LocalDateTime dataGeracao;

    private Long totalDocumentos;

    private Long documentosNovosHoje;

    private Long documentosNovosEstaSemana;

    private Long documentosNovosEsteMes;

    private Double taxaCrescimentoSemanal;

    private Double taxaCrescimentoMensal;

    private Long pendentesTriagem;

    private Long pareceresPendentes;

    private Long posicionamentosPendentes;

    private Integer prazosVencidos;

    private Integer alertasCriticos;

    private Double eficiencia;

    private String statusGeral;

    private Double probabilidadeAprovacaoMedia;

    private Double taxaAprovacao;
}