package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Registro de comportamento de votação
 * Embarcado em PerfilParlamentar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComportamentoVotacao {

    private String proposicaoId;

    private String descricao;

    private String tema;

    /**
     * Como votou (SIM, NAO, ABSTENCAO, OBSTRUCAO, AUSENTE)
     */
    private String voto;

    private LocalDate dataVotacao;

    private Boolean votouComGoverno;

    @Builder.Default
    private boolean relacionadoDefesa = false;

    private String observacoes;
}