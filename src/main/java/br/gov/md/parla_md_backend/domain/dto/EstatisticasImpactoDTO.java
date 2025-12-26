package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasImpactoDTO {

    private Long totalAnalises;

    private Long analisesComSucesso;

    private Long analisesFalhas;

    private Double taxaSucesso;

    private Map<String, Long> distribuicaoPorNivel;

    private Map<String, Long> distribuicaoPorTipo;

    private Map<String, Long> distribuicaoPorArea;

    private Long impactosAltos;

    private Long impactosMedios;

    private Long impactosBaixos;

    private Long impactosNegativos;

    private Long impactosPositivos;

    private Double percentualImpactoMedio;

    private Long tempoMedioMs;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;
}