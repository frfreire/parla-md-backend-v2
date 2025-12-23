package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.TipoProposicao;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Proposição legislativa da Câmara dos Deputados
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "proposicoes")
public class Proposicao extends ItemLegislativo {

    private Long idCamara;

    private TipoProposicao tipoProposicao;

    private String siglaTipo;

    private String uriProposicao;

    private Long idDeputadoAutor;

    private String nomeDeputadoAutor;

    private String partidoAutor;

    private String estadoAutor;

    private String siglaUFAutor;

    private String uriAutor;

    private String uriOrgaoNumerador;

    private String statusProposicao;

    private String despacho;

    private String descricaoTramitacao;

    private String siglaOrgao;

    private String regime;

    private boolean apreciacao;

    public Proposicao() {
        this.casa = Casa.CAMARA;
    }

    @Override
    public String getIdentificadorCompleto() {
        return String.format("%s %s/%d", siglaTipo, numero, ano);
    }

    @Override
    public String getTipo() {
        return siglaTipo;
    }
}