package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.AlinhamentoPolitico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "perfis_parlamentares")
public class PerfilParlamentar {

    @Id
    private String id;

    @Indexed(unique = true)
    private String parlamentarId;

    private String nomeParlamentar;

    private String partido;

    private String uf;

    private String casa;

    // Análise de Alinhamento Político
    private AlinhamentoPolitico alinhamentoGoverno;
    private double percentualAlinhamentoGoverno;

    @Builder.Default
    private List<AlinhamentoGoverno> historicoAlinhamentos = new ArrayList<>();

    @Builder.Default
    private List<PosicionamentoTematico> posicionamentosTematicos = new ArrayList<>();

    @Builder.Default
    private List<ComportamentoVotacao> historicoVotacoes = new ArrayList<>();

    private int totalVotacoes;

    private int votacoesDefesa;

    private MetricasDesempenho metricas;

    private String resumoIA;

    @Builder.Default
    private List<String> pontosFortes = new ArrayList<>();

    @Builder.Default
    private List<String> areasInteresse = new ArrayList<>();

    private String estrategiaAbordagem;

    @Builder.Default
    private LocalDateTime dataUltimaAnalise = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime dataProximaAtualizacao = LocalDateTime.now().plusDays(30);

    private String versaoModeloIA;

    public PosicionamentoTematico buscarPosicionamentoTema(String tema) {
        return posicionamentosTematicos.stream()
                .filter(p -> p.getTema().equalsIgnoreCase(tema))
                .findFirst()
                .orElse(null);
    }

    public boolean precisaAtualizacao() {
        return LocalDateTime.now().isAfter(dataProximaAtualizacao);
    }

    public double calcularTaxaAlinhamentoTema(String tema) {
        PosicionamentoTematico pos = buscarPosicionamentoTema(tema);
        if (pos == null) {
            return 0.0;
        }
        return pos.getPercentualFavoravel();
    }
}