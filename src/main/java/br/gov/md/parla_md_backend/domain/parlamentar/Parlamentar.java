package br.gov.md.parla_md_backend.domain.parlamentar;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "parlamentares")
public abstract class Parlamentar {

    @Id
    protected String id;

    protected Casa casa;

    protected String nome;

    protected String nomeCivil;

    protected String siglaPartido;

    protected String siglaUF;

    protected String email;

    protected LocalDate dataNascimento;

    protected String municipioNascimento;

    protected String ufNascimento;

    protected String escolaridade;

    protected String urlFoto;

    protected String urlPaginaParlamentar;

    protected boolean emExercicio;

    protected List<String> redeSocial = new ArrayList<>();

    protected String telefone;

    /**
     * Retorna o identificador único da casa legislativa
     */
    public abstract Long getCodigoParlamentar();

    /**
     * Retorna a legislatura atual
     */
    public abstract String getLegislaturaAtual();
}