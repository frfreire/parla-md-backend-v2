package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.parlamentar.Parlamentar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "senadores")
public class Senador extends Parlamentar {

    private Long codigoParlamentar;

    private String nomeParlamentar;

    private String formaTratamento;

    private String siglaPartidoAtual;

    private LocalDate dataPosse;

    private String tipoMandato;

    private Integer legislaturaAtual;

    private String primeiraLegislaturaDoMandato;

    private String primeiraLegislaturaDoSenador;

    private String tipoAfastamento;

    private LocalDate inicioAfastamento;

    private LocalDate fimAfastamento;

    private String gabinete;

    private String anexo;

    private String sala;

    public Senador() {
        this.casa = Casa.SENADO;
    }

    @Override
    public Long getCodigoParlamentar() {
        return codigoParlamentar;
    }

    @Override
    public String getLegislaturaAtual() {
        return legislaturaAtual != null ? legislaturaAtual.toString() : null;
    }
}