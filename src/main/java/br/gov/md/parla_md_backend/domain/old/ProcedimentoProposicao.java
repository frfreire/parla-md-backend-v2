package br.gov.md.parla_md_backend.domain.old;

import br.gov.md.parla_md_backend.domain.Proposicao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "proposition_procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoProposicao {
    @Id
    private String id;

    @DBRef
    private Proposicao proposicao;

    private String propositionId;
    private LocalDateTime dataHora;
    private int sequencia;
    private String siglaOrgao;
    private String uriOrgao;
    private String descricaoTramitacao;
    private String despacho;
    private String regime;
    private String idTipoTramitacao;
    private String statusProposicao;
    private String uriUltimoRelator;
    private String urlDocumento;
}
