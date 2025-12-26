package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AnaliseImpacto;
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
public class AnaliseImpactoDTO {

        private String id;

        private String itemLegislativoId;

        private String areaImpactoId;

        private String areaImpactoNome;

        private String nivelImpacto;

        private String tipoImpacto;

        private Double percentualImpacto;

        private String analiseDetalhada;

        private List<String> consequencias;

        private List<String> gruposAfetados;

        private List<String> riscos;

        private List<String> oportunidades;

        private String recomendacoes;

        private LocalDateTime dataAnalise;

        private String modeloVersao;

        private Long tempoProcessamentoMs;

        private Boolean sucesso;

        public static AnaliseImpactoDTO from(AnaliseImpacto analise) {
                return AnaliseImpactoDTO.builder()
                        .id(analise.getId())
                        .itemLegislativoId(analise.getItemLegislativo() != null ?
                                analise.getItemLegislativo().getId() : null)
                        .areaImpactoId(analise.getAreaImpacto() != null ?
                                analise.getAreaImpacto().getId() : null)
                        .areaImpactoNome(analise.getAreaImpacto() != null ?
                                analise.getAreaImpacto().getNome() : null)
                        .nivelImpacto(analise.getNivelImpacto())
                        .tipoImpacto(analise.getTipoImpacto())
                        .percentualImpacto(analise.getPercentualImpacto())
                        .analiseDetalhada(analise.getAnaliseDetalhada())
                        .consequencias(analise.getConsequencias())
                        .gruposAfetados(analise.getGruposAfetados())
                        .riscos(analise.getRiscos())
                        .oportunidades(analise.getOportunidades())
                        .recomendacoes(analise.getRecomendacoes())
                        .dataAnalise(analise.getDataAnalise())
                        .modeloVersao(analise.getModeloVersao())
                        .tempoProcessamentoMs(analise.getTempoProcessamentoMs())
                        .sucesso(analise.getSucesso())
                        .build();
        }
}