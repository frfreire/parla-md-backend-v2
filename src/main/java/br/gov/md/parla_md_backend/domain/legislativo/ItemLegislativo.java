package br.gov.md.parla_md_backend.domain.legislativo;

import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe abstrata base para itens legislativos (Proposições e Matérias)
 */
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

    /**
     * Retorna identificador único completo (ex: PL 1234/2024, PLS 567/2024)
     */
    public abstract String getIdentificadorCompleto();

    /**
     * Retorna o tipo específico do item (ex: PL, PEC para Câmara; PLS, PEC para Senado)
     */
    public abstract String getTipo();
}
