package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.TendenciasIA;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record TendenciasIADTO(
        String id,
        String analiseGeral,
        List<String> temasEmergentes,
        List<String> alertas,
        String previsaoProximoMes,
        Map<String, Object> dadosContexto,
        Integer totalDocumentosAnalisados,
        String periodoReferencia,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataAnalise,
        String modeloVersao,
        Long tempoProcessamentoMs,
        Boolean sucesso,
        String mensagemErro
) {
    public static TendenciasIADTO from(TendenciasIA entidade) {
        return TendenciasIADTO.builder()
                .id(entidade.getId())
                .analiseGeral(entidade.getAnaliseGeral())
                .temasEmergentes(entidade.getTemasEmergentes())
                .alertas(entidade.getAlertas())
                .previsaoProximoMes(entidade.getPrevisaoProximoMes())
                .dadosContexto(entidade.getDadosContexto())
                .totalDocumentosAnalisados(entidade.getTotalDocumentosAnalisados())
                .periodoReferencia(entidade.getPeriodoReferencia())
                .dataAnalise(entidade.getDataAnalise())
                .modeloVersao(entidade.getModeloVersao())
                .tempoProcessamentoMs(entidade.getTempoProcessamentoMs())
                .sucesso(entidade.getSucesso())
                .mensagemErro(entidade.getMensagemErro())
                .build();
    }
}