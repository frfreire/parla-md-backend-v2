package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.legislativo.Materia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "matter_procedures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoMateria {
    @Id
    private String id;

    @DBRef
    private Materia materia;

    private LocalDateTime dataHora;
    private int sequencia;
    private String siglaOrgao;
    private String uriOrgao;
    private String descricaoTramitacao;
    private String matterId;
    private String despacho;
    private String regime;
    private String idTipoTramitacao;
    private String statusMateria;
    private String uriUltimoRelator;
    private String urlDocumento;

    public void setMateria(Materia materia) {
        this.materia = materia;
        this.matterId = String.valueOf(materia != null ? materia.getCodigoMateria() : null);
    }
}
