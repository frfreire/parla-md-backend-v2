package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.Sumario;
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
public class SumarioDTO {

    private String id;

    private String itemLegislativoId;

    private String tipoSumario;

    private String sumarioExecutivo;

    private List<String> pontosPrincipais;

    private List<String> entidadesRelevantes;

    private List<String> palavrasChave;

    private String temasPrincipais;

    private String sentimentoGeral;

    private String impactoEstimado;

    private LocalDateTime dataCriacao;

    private String modeloVersao;

    private Long tempoProcessamentoMs;

    private Integer tamanhoTextoOriginal;

    private Integer tamanhoSumario;

    private Double taxaCompressao;

    private Boolean sucesso;

    public static SumarioDTO from(Sumario sumario) {
        return SumarioDTO.builder()
                .id(sumario.getId())
                .itemLegislativoId(sumario.getItemLegislativo() != null ?
                        sumario.getItemLegislativo().getId() : null)
                .tipoSumario(sumario.getTipoSumario())
                .sumarioExecutivo(sumario.getSumarioExecutivo())
                .pontosPrincipais(sumario.getPontosPrincipais())
                .entidadesRelevantes(sumario.getEntidadesRelevantes())
                .palavrasChave(sumario.getPalavrasChave())
                .temasPrincipais(sumario.getTemasPrincipais())
                .sentimentoGeral(sumario.getSentimentoGeral())
                .impactoEstimado(sumario.getImpactoEstimado())
                .dataCriacao(sumario.getDataCriacao())
                .modeloVersao(sumario.getModeloVersao())
                .tempoProcessamentoMs(sumario.getTempoProcessamentoMs())
                .tamanhoTextoOriginal(sumario.getTamanhoTextoOriginal())
                .tamanhoSumario(sumario.getTamanhoSumario())
                .taxaCompressao(sumario.getTaxaCompressao())
                .sucesso(sumario.getSucesso())
                .build();
    }
}