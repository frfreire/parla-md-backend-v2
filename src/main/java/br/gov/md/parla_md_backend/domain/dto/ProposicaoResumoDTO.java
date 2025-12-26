package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.domain.Proposicao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposicaoResumoDTO {

    private String id;
    private String identificadorCompleto;
    private String ementa;
    private String tema;
    private LocalDateTime dataApresentacao;
    private String nomeDeputadoAutor;
    private String partidoAutor;
    private String situacaoAtual;
    private StatusTriagem statusTriagem;
    private Double probabilidadeAprovacao;

    public static ProposicaoResumoDTO fromEntity(Proposicao proposicao) {
        if (proposicao == null) return null;

        return ProposicaoResumoDTO.builder()
                .id(proposicao.getId())
                .identificadorCompleto(proposicao.getIdentificadorCompleto())
                .ementa(proposicao.getEmenta())
                .tema(proposicao.getTema())
                .dataApresentacao(proposicao.getDataApresentacao().atStartOfDay())
                .nomeDeputadoAutor(proposicao.getNomeDeputadoAutor())
                .partidoAutor(proposicao.getPartidoAutor())
                .situacaoAtual(proposicao.getSituacaoAtual())
                .statusTriagem(proposicao.getStatusTriagem())
                .build();
    }
}