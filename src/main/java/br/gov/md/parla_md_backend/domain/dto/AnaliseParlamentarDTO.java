package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AnaliseParlamentar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseParlamentarDTO {

    private String id;

    private String parlamentarId;

    private String parlamentarNome;

    private String parlamentarPartido;

    private String parlamentarEstado;

    private String tema;

    private String posicionamento;

    private String nivelConfiabilidade;

    private Double confiabilidade;

    private String analiseDetalhada;

    private String tendencia;

    private List<String> padroesIdentificados;

    private Map<String, Object> estatisticas;

    private Integer totalVotacoes;

    private Integer votosAFavor;

    private Integer votosContra;

    private Integer abstencoes;

    private Double percentualCoerencia;

    private List<String> votacoesChave;

    private String alinhamentoPolitico;

    private String previsaoComportamento;

    private LocalDateTime dataAnalise;

    private String modeloVersao;

    private Long tempoProcessamentoMs;

    private Boolean sucesso;

    public static AnaliseParlamentarDTO from(AnaliseParlamentar analise) {
        return AnaliseParlamentarDTO.builder()
                .id(analise.getId())
                .parlamentarId(analise.getParlamentar() != null ?
                        analise.getParlamentar().getId() : null)
                .parlamentarNome(analise.getParlamentar() != null ?
                        analise.getParlamentar().getNome() : null)
                .parlamentarPartido(analise.getParlamentar() != null ?
                        analise.getParlamentar().getSiglaPartido() : null)
                .parlamentarEstado(analise.getParlamentar() != null ?
                        analise.getParlamentar().getSiglaUF() : null)
                .tema(analise.getTema())
                .posicionamento(analise.getPosicionamento())
                .nivelConfiabilidade(determinarNivelConfiabilidade(analise))
                .confiabilidade(analise.getConfiabilidade())
                .analiseDetalhada(analise.getAnaliseDetalhada())
                .tendencia(analise.getTendencia())
                .padroesIdentificados(analise.getPadroesIdentificados())
                .estatisticas(analise.getEstatisticas())
                .totalVotacoes(analise.getTotalVotacoes())
                .votosAFavor(analise.getVotosAFavor())
                .votosContra(analise.getVotosContra())
                .abstencoes(analise.getAbstencoes())
                .percentualCoerencia(analise.getPercentualCoerencia())
                .votacoesChave(analise.getVotacoesChave())
                .alinhamentoPolitico(analise.getAlinhamentoPolitico())
                .previsaoComportamento(analise.getPrevisaoComportamento())
                .dataAnalise(analise.getDataAnalise())
                .modeloVersao(analise.getModeloVersao())
                .tempoProcessamentoMs(analise.getTempoProcessamentoMs())
                .sucesso(analise.getSucesso())
                .build();
    }

    private static String determinarNivelConfiabilidade(AnaliseParlamentar analise) {
        if (analise.isAltaConfiabilidade()) return "ALTA";
        if (analise.isMediaConfiabilidade()) return "MEDIA";
        if (analise.isBaixaConfiabilidade()) return "BAIXA";
        return "INDEFINIDO";
    }
}