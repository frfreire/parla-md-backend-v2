package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record AnaliseImpactoDTO(
        String id,
        String itemLegislativoId,
        String itemLegislativoTipo,
        String itemLegislativoEmenta,
        String areaImpactoId,
        String areaImpactoNome,
        String nivelImpacto,
        String tipoImpacto,
        Double percentualImpacto,
        String analiseDetalhada,
        List<String> consequencias,
        List<String> gruposAfetados,
        List<String> riscos,
        List<String> oportunidades,
        String recomendacoes,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataAnalise,

        String modeloVersao,
        Long tempoProcessamentoMs,
        Boolean sucesso,
        String mensagemErro
) {
        public static AnaliseImpactoDTO from(AnaliseImpacto analise) {
                if (analise == null) {
                        return null;
                }

                return new AnaliseImpactoDTO(
                        analise.getId(),
                        analise.getItemLegislativo() != null ? analise.getItemLegislativo().getId() : null,
                        analise.getItemLegislativo() != null ? analise.getItemLegislativo().getTipo() : null,
                        analise.getItemLegislativo() != null ? analise.getItemLegislativo().getEmenta() : null,
                        analise.getAreaImpacto() != null ? analise.getAreaImpacto().getId() : null,
                        analise.getAreaImpacto() != null ? analise.getAreaImpacto().getNome() : null,
                        analise.getNivelImpacto(),
                        analise.getTipoImpacto(),
                        analise.getPercentualImpacto(),
                        analise.getAnaliseDetalhada(),
                        analise.getConsequencias(),
                        analise.getGruposAfetados(),
                        analise.getRiscos(),
                        analise.getOportunidades(),
                        analise.getRecomendacoes(),
                        analise.getDataAnalise(),
                        analise.getModeloVersao(),
                        analise.getTempoProcessamentoMs(),
                        analise.getSucesso(),
                        analise.getMensagemErro()
                );
        }
}