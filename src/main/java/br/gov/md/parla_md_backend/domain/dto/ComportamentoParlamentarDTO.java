package br.gov.md.parla_md_backend.domain.dto;

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
public class ComportamentoParlamentarDTO {

    private String parlamentarId;

    private String parlamentarNome;

    private String tema;

    private Integer totalVotacoes;

    private Integer votosAFavor;

    private Integer votosContra;

    private Integer abstencoes;

    private Double percentualAFavor;

    private Double percentualContra;

    private Double percentualAbstencao;

    private String posicionamento;

    private List<VotacaoResumoDTO> votacoesRecentes;

    private LocalDateTime periodoInicio;

    private LocalDateTime periodoFim;
}