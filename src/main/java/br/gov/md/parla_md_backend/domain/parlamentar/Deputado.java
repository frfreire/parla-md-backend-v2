package br.gov.md.parla_md_backend.domain.parlamentar;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Deputado Federal da Câmara dos Deputados
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "deputados")
public class Deputado extends Parlamentar {

    private Long idDeputado;

    private String uriDeputado;

    private Integer idLegislaturaAtual;

    private String condicaoEleitoral;

    private List<Gabinete> gabinetes = new ArrayList<>();

    public Deputado() {
        this.casa = Casa.CAMARA;
    }

    @Override
    public Long getCodigoParlamentar() {
        return idDeputado;
    }

    @Override
    public String getLegislaturaAtual() {
        return idLegislaturaAtual != null ? idLegislaturaAtual.toString() : null;
    }

    @Data
    public static class Gabinete {
        private String nome;
        private String predio;
        private String sala;
        private String andar;
        private String telefone;
        private String email;
    }
}