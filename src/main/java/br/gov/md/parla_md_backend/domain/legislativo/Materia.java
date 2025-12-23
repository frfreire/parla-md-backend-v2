package br.gov.md.parla_md_backend.domain.legislativo;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.TipoMateria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Matéria legislativa do Senado Federal
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "materias")
public class Materia extends ItemLegislativo {

    private Long codigoMateria;

    private TipoMateria tipoMateria;

    private String siglaSubtipoMateria;

    private Integer numero;

    private String anoMateria;

    private String descricaoSubtipoMateria;

    private String descricaoIdentificacaoMateria;

    private String indicadorTramitando;

    private Long codigoParlamentarAutor;

    private String nomeParlamentarAutor;

    private String siglaPartidoParlamentar;

    private String siglaUFParlamentar;

    private String descricaoNatureza;

    private String indicadorComplementar;

    private String especificacao;

    private String ementaMateria;

    private String localidade;

    private String apelido;

    private String siglaOrgaoOrigem;

    private String assuntoEspecifico;

    private String assuntoGeral;

    private String indexacao;

    public Materia() {
        this.casa = Casa.SENADO;
    }

    @Override
    public String getIdentificadorCompleto() {
        return String.format("%s %s/%d",
                tipoMateria != null ? tipoMateria.getSigla() : siglaSubtipoMateria,
                numero,
                ano);
    }

    @Override
    public String getTipo() {
        return tipoMateria != null ? tipoMateria.getSigla() : siglaSubtipoMateria;
    }
}