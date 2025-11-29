package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.TipoSetor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parladb.sectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Setor {
    @Id
    private String id;
    private String nome;
    private String sigla;
    private String descricao;
    private TipoSetor tipo;
}
