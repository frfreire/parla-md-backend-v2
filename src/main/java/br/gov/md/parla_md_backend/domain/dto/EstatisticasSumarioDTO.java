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
public class EstatisticasSumarioDTO {

    private Long totalSumarios;

    private Long sumariosComSucesso;

    private Long sumariosFalhas;

    private Double taxaSucesso;

    private Double taxaCompressaoMedia;

    private Double taxaCompressaoMinima;

    private Double taxaCompressaoMaxima;

    private Long tempoMedioMs;

    private Long tempoMinimoMs;

    private Long tempoMaximoMs;

    private Integer tamanhoMedioOriginal;

    private Integer tamanhoMedioSumario;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;
}