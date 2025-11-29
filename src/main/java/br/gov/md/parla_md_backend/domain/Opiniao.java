package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "parladb.opinions")
public class Opiniao {
    @Id
    private String id;
    private String propositionId;
    private String conteudo;
    private String usuarioId;
    private String setorEmissor;
    private Date dataEmissao;

    public Opiniao() {
    }

    public Opiniao(String id, String propositionId, String conteudo, String usuarioId, String setorEmissor, Date dataEmissao) {
        this.id = id;
        this.propositionId = propositionId;
        this.conteudo = conteudo;
        this.usuarioId = usuarioId;
        this.setorEmissor = setorEmissor;
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

    public String getSetorEmissor() {
        return setorEmissor;
    }

    public void setSetorEmissor(String setorEmissor) {
        this.setorEmissor = setorEmissor;
    }

    public Date getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(Date dataEmissao) {
        this.dataEmissao = dataEmissao;
    }
}
