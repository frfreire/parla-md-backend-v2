package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.MetricaDashboard;
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
public class DashboardMetricasDTO {

    private String id;

    private LocalDateTime dataCalculo;

    private String periodo;

    private Long totalProposicoes;

    private Long totalMaterias;

    private Long totalDocumentos;

    private Long documentosUltimaSemana;

    private Long documentosUltimoMes;

    private Map<String, Long> distribuicaoPorTipo;

    private Map<String, Long> distribuicaoPorPartido;

    private Map<String, Long> distribuicaoPorEstado;

    private Map<String, Long> distribuicaoPorTema;

    private Map<String, Long> distribuicaoPorStatus;

    private Double probabilidadeAprovacaoMedia;

    private Double probabilidadeAprovacaoMediana;

    private Long documentosAprovados;

    private Long documentosRejeitados;

    private Long documentosEmTramitacao;

    private Double taxaAprovacao;

    private Double taxaRejeicao;

    private Map<String, Object> kpis;

    private TendenciasDTO tendencias;

    private LocalDateTime proximaAtualizacao;

    public static DashboardMetricasDTO from(MetricaDashboard metrica) {
        return DashboardMetricasDTO.builder()
                .id(metrica.getId())
                .dataCalculo(metrica.getDataCalculo())
                .periodo(metrica.getPeriodo())
                .totalProposicoes(metrica.getTotalProposicoes())
                .totalMaterias(metrica.getTotalMaterias())
                .totalDocumentos(metrica.getTotalDocumentos())
                .documentosUltimaSemana(metrica.getDocumentosUltimaSemana())
                .documentosUltimoMes(metrica.getDocumentosUltimoMes())
                .distribuicaoPorTipo(metrica.getPorTipo())
                .distribuicaoPorPartido(metrica.getPorPartido())
                .distribuicaoPorEstado(metrica.getPorEstado())
                .distribuicaoPorTema(metrica.getPorTema())
                .distribuicaoPorStatus(metrica.getPorStatus())
                .probabilidadeAprovacaoMedia(metrica.getProbabilidadeAprovacaoMedia())
                .probabilidadeAprovacaoMediana(metrica.getProbabilidadeAprovacaoMediana())
                .documentosAprovados(metrica.getDocumentosAprovados())
                .documentosRejeitados(metrica.getDocumentosRejeitados())
                .documentosEmTramitacao(metrica.getDocumentosEmTramitacao())
                .taxaAprovacao(metrica.getTaxaAprovacao())
                .taxaRejeicao(metrica.getTaxaRejeicao())
                .kpis(metrica.getKpis())
                .tendencias(TendenciasDTO.from(metrica.getTendencias()))
                .proximaAtualizacao(metrica.getProximaAtualizacao())
                .build();
    }
}
