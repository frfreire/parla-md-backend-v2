package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "itens_legislativos")
public abstract class ItemLegislativo {

    @Id
    protected String id;

    protected Casa casa;

    protected String numero;

    protected Integer ano;

    protected String ementa;

    protected String ementaDetalhada;

    protected String keywords;

    protected LocalDate dataApresentacao;

    protected String tema;

    protected StatusTriagem statusTriagem;

    protected String urlInteiroTeor;

    protected LocalDateTime dataCaptura;

    protected LocalDateTime dataUltimaAtualizacao;

    protected boolean aprovada;

    protected String situacaoAtual;

    protected String linkPaginaCasa;

    public abstract String getIdentificadorCompleto();

    public abstract String getTipo();
}
