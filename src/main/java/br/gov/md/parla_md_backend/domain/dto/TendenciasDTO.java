package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendenciasDTO {

    private List<String> temasEmAlta;

    private List<String> partidosMaisAtivos;

    private List<String> estadosMaisAtivos;

    private String analiseIA;

    private Map<String, Double> crescimentoSemanal;

    private Map<String, Double> crescimentoMensal;

    private List<String> alertas;

    private String previsaoProximoMes;

    @SuppressWarnings("unchecked")
    public static TendenciasDTO from(Map<String, Object> tendencias) {
        if (tendencias == null) {
            return TendenciasDTO.builder().build();
        }

        return TendenciasDTO.builder()
                .temasEmAlta((List<String>) tendencias.get("temasEmAlta"))
                .partidosMaisAtivos((List<String>) tendencias.get("partidosMaisAtivos"))
                .estadosMaisAtivos((List<String>) tendencias.get("estadosMaisAtivos"))
                .analiseIA((String) tendencias.get("analiseIA"))
                .crescimentoSemanal((Map<String, Double>) tendencias.get("crescimentoSemanal"))
                .crescimentoMensal((Map<String, Double>) tendencias.get("crescimentoMensal"))
                .alertas((List<String>) tendencias.get("alertas"))
                .previsaoProximoMes((String) tendencias.get("previsaoProximoMes"))
                .build();
    }
}
