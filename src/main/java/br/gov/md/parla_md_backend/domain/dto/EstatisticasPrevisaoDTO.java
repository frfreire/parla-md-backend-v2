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
public class EstatisticasPrevisaoDTO {

    private Long totalPrevisoes;

    private Long previsoesComSucesso;

    private Long previsoesFalhas;

    private Double taxaSucesso;

    private Double probabilidadeMedia;

    private Double confiancaMedia;

    private Long tempoMedioMs;

    private Long tempoMinimoMs;

    private Long tempoMaximoMs;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;

    private Long previsoesMuitoProvaveis;

    private Long previsoesProvaveis;

    private Long previsoesImprovaveis;

    private Long previsoesMuitoImprovaveis;
}