package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "voto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voto {

    @Id
    private String id;
    @DBRef
    private Votacao votacao;
    @DBRef
    private Parlamentar deputado;
    private String voto;
}
