package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.Previsao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisaoDTO {

    private String id;

    private String itemLegislativoId;

    private String tipoPrevisao;

    private Double probabilidadeAprovacao;

    private String classificacao;

    private Double confianca;

    private String nivelConfianca;

    private String justificativa;

    private String fatoresPositivos;

    private String fatoresNegativos;

    private LocalDateTime dataPrevisao;

    private String modeloVersao;

    private Long tempoProcessamentoMs;

    private Boolean sucesso;

    public static PrevisaoDTO from(Previsao previsao) {
        return PrevisaoDTO.builder()
                .id(previsao.getId())
                .itemLegislativoId(previsao.getItemLegislativo() != null ?
                        previsao.getItemLegislativo().getId() : null)
                .tipoPrevisao(previsao.getTipoPrevisao())
                .probabilidadeAprovacao(previsao.getProbabilidadeAprovacao())
                .classificacao(previsao.getClassificacao())
                .confianca(previsao.getConfianca())
                .nivelConfianca(determinarNivelConfianca(previsao))
                .justificativa(previsao.getJustificativa())
                .fatoresPositivos(previsao.getFatoresPositivos())
                .fatoresNegativos(previsao.getFatoresNegativos())
                .dataPrevisao(previsao.getDataPrevisao())
                .modeloVersao(previsao.getModeloVersao())
                .tempoProcessamentoMs(previsao.getTempoProcessamentoMs())
                .sucesso(previsao.getSucesso())
                .build();
    }

    private static String determinarNivelConfianca(Previsao previsao) {
        if (previsao.isAltaConfianca()) return "ALTA";
        if (previsao.isMediaConfianca()) return "MEDIA";
        if (previsao.isBaixaConfianca()) return "BAIXA";
        return "INDEFINIDO";
    }
}