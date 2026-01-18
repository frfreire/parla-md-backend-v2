package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record EstatisticasImpactoDTO(
        Long totalAnalises,
        Long analisesComSucesso,
        Long analisesFalhas,
        Double taxaSucesso,

        Map<String, Long> distribuicaoPorNivel,
        Map<String, Long> distribuicaoPorTipo,
        Map<String, Long> distribuicaoPorArea,

        Long impactosAltos,
        Long impactosMedios,
        Long impactosBaixos,
        Long impactosNegativos,
        Long impactosPositivos,

        Double percentualImpactoMedio,
        Long tempoMedioMs,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime periodoInicio,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime periodoFim
) {}