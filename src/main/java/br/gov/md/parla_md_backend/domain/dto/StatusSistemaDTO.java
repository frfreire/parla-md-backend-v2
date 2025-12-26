package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusSistemaDTO {

    private LocalDateTime timestamp;

    private String statusGeral;

    private Integer alertasCriticos;

    private Double eficiencia;

    private Long documentosAtivos;

    private Long pendentesTriagem;

    private Long pareceresPendentes;

    private Long posicionamentosPendentes;

    private Integer prazosVencidos;

    private Double tempoMedioTramitacao;

    public boolean isOperacional() {
        return "OPERACIONAL".equals(statusGeral);
    }

    public boolean isCritico() {
        return "CRITICO".equals(statusGeral);
    }
}