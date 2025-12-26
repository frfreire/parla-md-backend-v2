package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.HistoricoMetricas;
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
public class HistoricoMetricasDTO {

    private String id;

    private String tipo;

    private LocalDateTime dataRegistro;

    private String metrica;

    private Double valor;

    private String unidade;

    private String categoria;

    private Map<String, Object> detalhes;

    public static HistoricoMetricasDTO from(HistoricoMetricas historico) {
        return HistoricoMetricasDTO.builder()
                .id(historico.getId())
                .tipo(historico.getTipo())
                .dataRegistro(historico.getDataRegistro())
                .metrica(historico.getMetrica())
                .valor(historico.getValor())
                .unidade(historico.getUnidade())
                .categoria(historico.getCategoria())
                .detalhes(historico.getDetalhes())
                .build();
    }
}
