package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "procedimentos_materias")
@CompoundIndexes({
        @CompoundIndex(name = "idx_materia_data", def = "{'codigoMateria': 1, 'dataTramitacao': -1}"),
        @CompoundIndex(name = "idx_materia_numero", def = "{'codigoMateria': 1, 'numeroProcedimento': 1}", unique = true)
})
public class ProcedimentoMateria {

    @Id
    private String id;

    @Indexed
    private Long codigoMateria;

    private Integer numeroProcedimento;

    @Indexed
    private LocalDateTime dataTramitacao;

    private String descricaoTramitacao;

    private String situacaoDescricao;

    private String localTramitacao;

    private String siglaOrgao;

    private String tipoTramitacao;

    private String despacho;

    private String relator;

    private Long codigoRelator;

    private String situacaoMateria;

    private boolean urgente;

    private boolean houveVotacao;

    private String resultadoVotacao;

   private Integer votosFavor;

    private Integer votosContra;

    private Integer abstencoes;

    private String textoIntegral;

    private String urlDocumento;

    private String observacoes;

    private LocalDateTime dataCaptura;

    private LocalDateTime dataUltimaAtualizacao;

    public ProcedimentoMateria(Long codigoMateria, LocalDateTime dataTramitacao, String descricaoTramitacao) {
        this.codigoMateria = codigoMateria;
        this.dataTramitacao = dataTramitacao;
        this.descricaoTramitacao = descricaoTramitacao;
        this.dataCaptura = LocalDateTime.now();
    }

    public boolean isVotacao() {
        return houveVotacao || "VOTACAO".equalsIgnoreCase(tipoTramitacao);
    }

    public boolean isAprovado() {
        return "APROVADO".equalsIgnoreCase(resultadoVotacao) ||
                "APROVADA".equalsIgnoreCase(resultadoVotacao);
    }

    public String getResumo() {
        StringBuilder sb = new StringBuilder();

        if (dataTramitacao != null) {
            sb.append(dataTramitacao.toLocalDate()).append(" - ");
        }

        if (localTramitacao != null) {
            sb.append(localTramitacao).append(": ");
        }

        if (descricaoTramitacao != null) {
            sb.append(descricaoTramitacao.length() > 100
                    ? descricaoTramitacao.substring(0, 100) + "..."
                    : descricaoTramitacao);
        }

        return sb.toString();
    }
}