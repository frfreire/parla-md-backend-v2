package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerieTemporalDTO {

    private String metrica;

    private String tipo;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;

    private List<PontoTemporalDTO> pontos;

    private Double valorMinimo;

    private Double valorMaximo;

    private Double valorMedio;

    private String tendencia;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PontoTemporalDTO {
        private LocalDateTime data;
        private Double valor;
        private String categoria;
    }
}