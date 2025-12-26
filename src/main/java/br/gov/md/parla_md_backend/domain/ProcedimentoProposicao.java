package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.Proposicao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "procedimentos_proposicao")
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_proposicao_sequencia",
                def = "{'proposicao': 1, 'sequencia': -1}",
                unique = true
        ),
        @CompoundIndex(
                name = "idx_proposicao_data",
                def = "{'proposicao': 1, 'dataHora': -1}"
        ),
        @CompoundIndex(
                name = "idx_orgao_data",
                def = "{'siglaOrgao': 1, 'dataHora': -1}"
        ),
        @CompoundIndex(
                name = "idx_tipo_tramitacao",
                def = "{'idTipoTramitacao': 1, 'dataHora': -1}"
        )
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoProposicao {

    @Id
    private String id;

    @DBRef
    @Indexed
    private Proposicao proposicao;

    @Indexed
    private LocalDateTime dataHora;

    private Integer sequencia;

    @Indexed
    private String siglaOrgao;

    private String uriOrgao;

    private String descricaoTramitacao;

    private String despacho;

    private String regime;

    @Indexed
    private String idTipoTramitacao;

    private String tipoTramitacao;

    private String statusProposicao;

    private Integer codStatusProposicao;

    private String uriUltimoRelator;

    private String nomeRelator;

    private String urlDocumento;

    private String textoDocumento;

    private String apreciacao;

    private Boolean urgente;

    private String observacoes;

    private LocalDateTime dataCaptura;

    private LocalDateTime dataUltimaAtualizacao;

    private String origemDados;

    public String getIdentificacaoResumida() {
        return String.format("Seq %d - %s - %s",
                sequencia != null ? sequencia : 0,
                siglaOrgao != null ? siglaOrgao : "N/A",
                dataHora != null ? dataHora.toLocalDate() : "N/A"
        );
    }

    public boolean isRecente() {
        if (dataHora == null) return false;
        return dataHora.isAfter(LocalDateTime.now().minusDays(30));
    }

    public boolean isVotacao() {
        if (descricaoTramitacao == null) return false;
        String desc = descricaoTramitacao.toLowerCase();
        return desc.contains("votação") || desc.contains("votacao")
                || desc.contains("aprovado") || desc.contains("rejeitado");
    }

    public boolean isDesignacaoRelator() {
        if (descricaoTramitacao == null) return false;
        String desc = descricaoTramitacao.toLowerCase();
        return desc.contains("relator") || desc.contains("designado");
    }
}
