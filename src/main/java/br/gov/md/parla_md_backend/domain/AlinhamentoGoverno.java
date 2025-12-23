package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.AlinhamentoPolitico;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alinhamento com governo em período específico
 * Embarcado em PerfilParlamentar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlinhamentoGoverno {

    private LocalDate periodoInicio;

    private LocalDate periodoFim;

    private String governo;

    private AlinhamentoPolitico alinhamento;

    private double percentualAlinhamento;

    private int totalVotacoes;

    private int votacoesAlinhadas;

    private String observacoes;
}
