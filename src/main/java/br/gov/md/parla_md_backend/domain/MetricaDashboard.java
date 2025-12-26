package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "metricas_dashboard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaDashboard {

    @Id
    private String id;

    @Indexed
    private LocalDateTime dataCalculo;

    @Indexed
    private String tipoMetrica;

    private String periodo;

    private Long totalProposicoes;

    private Long totalMaterias;

    private Long totalDocumentos;

    private Long documentosUltimaSemana;

    private Long documentosUltimoMes;

    private Map<String, Long> porTipo;

    private Map<String, Long> porPartido;

    private Map<String, Long> porEstado;

    private Map<String, Long> porTema;

    private Map<String, Long> porStatus;

    private Double probabilidadeAprovacaoMedia;

    private Double probabilidadeAprovacaoMediana;

    private Long documentosAprovados;

    private Long documentosRejeitados;

    private Long documentosEmTramitacao;

    private Double taxaAprovacao;

    private Double taxaRejeicao;

    private Map<String, Object> kpis;

    private Map<String, Object> tendencias;

    private LocalDateTime proximaAtualizacao;

    public boolean isPrecisaAtualizar() {
        if (proximaAtualizacao == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(proximaAtualizacao);
    }
}