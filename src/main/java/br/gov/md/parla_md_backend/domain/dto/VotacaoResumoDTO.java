package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.Votacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotacaoResumoDTO {

    private String id;

    private LocalDateTime dataHora;

    private String descricao;

    private String proposicaoId;

    private String voto;

    private String resultado;

    public static VotacaoResumoDTO from(Votacao votacao) {
        return VotacaoResumoDTO.builder()
                .id(votacao.getId())
                .dataHora(votacao.getDataHoraInicio())
                .descricao(votacao.getDescricao())
                .proposicaoId(votacao.getProposicaoId())
                .voto(votacao.getVoto())
                .resultado(determinarResultado(votacao))
                .build();
    }

    private static String determinarResultado(Votacao votacao) {
        if (votacao.isVotoFavoravel()) return "FAVORÁVEL";
        if (votacao.isVotoContrario()) return "CONTRÁRIO";
        if (votacao.isAbstencao()) return "ABSTENÇÃO";
        return "OUTRO";
    }
}