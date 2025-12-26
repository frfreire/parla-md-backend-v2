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
public class EstatisticasParlamentarDTO {

    private Long totalAnalises;

    private Long analisesComSucesso;

    private Long analisesFalhas;

    private Double taxaSucesso;

    private Map<String, Long> distribuicaoPorPosicionamento;

    private Map<String, Long> distribuicaoPorTema;

    private Long posicionamentosPro;

    private Long posicionamentosContra;

    private Long posicionamentosNeutros;

    private Double confiabilidadeMedia;

    private Long tempoMedioMs;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;
}