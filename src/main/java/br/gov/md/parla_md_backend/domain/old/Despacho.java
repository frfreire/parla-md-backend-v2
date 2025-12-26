package br.gov.md.parla_md_backend.domain.old;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "parladb.despacho")
public class Despacho {
    @Id
    private String id;
    private String propositionId;
    private String conteudo;
    private String usuarioId;
    private String setorOrigem;
    private String setorDestino;
    private Date dataEmissao;

    public Despacho() {
    }

    public Despacho(String id, String propositionId, String conteudo, String usuarioId, String setorOrigem, String setorDestino, Date dataEmissao) {
        this.id = id;
        this.propositionId = propositionId;
        this.conteudo = conteudo;
        this.usuarioId = usuarioId;
        this.setorOrigem = setorOrigem;
        this.setorDestino = setorDestino;
        this.dataEmissao = dataEmissao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropositionId() {
        return propositionId;
    }

    public void setPropositionId(String propositionId) {
        this.propositionId = propositionId;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getSetorOrigem() {
        return setorOrigem;
    }

    public void setSetorOrigem(String setorOrigem) {
        this.setorOrigem = setorOrigem;
    }

    public String getSetorDestino() {
        return setorDestino;
    }

    public void setSetorDestino(String setorDestino) {
        this.setorDestino = setorDestino;
    }

    public Date getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(Date dataEmissao) {
        this.dataEmissao = dataEmissao;
    }
}
