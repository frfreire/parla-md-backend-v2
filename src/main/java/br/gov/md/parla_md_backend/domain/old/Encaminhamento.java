package br.gov.md.parla_md_backend.domain.old;

import br.gov.md.parla_md_backend.domain.enums.old.StatusEncaminhamento;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "forwarding")
public class Encaminhamento {
    @Id
    private String id;
    private String propositionId;
    private String setorDestino;
    private String solicitacao;
    private Date dataSolicitacao;
    private String resposta;
    private Date dataResposta;
    private StatusEncaminhamento status;

    public Encaminhamento() {

    }

    public Encaminhamento(String id, String propositionId, String setorDestino, String solicitacao, Date dataSolicitacao, String resposta, Date dataResposta, StatusEncaminhamento status) {
        this.id = id;
        this.propositionId = propositionId;
        this.setorDestino = setorDestino;
        this.solicitacao = solicitacao;
        this.dataSolicitacao = dataSolicitacao;
        this.resposta = resposta;
        this.dataResposta = dataResposta;
        this.status = status;
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

    public String getSetorDestino() {
        return setorDestino;
    }

    public void setSetorDestino(String setorDestino) {
        this.setorDestino = setorDestino;
    }

    public String getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(String solicitacao) {
        this.solicitacao = solicitacao;
    }

    public Date getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(Date dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Date getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(Date dataResposta) {
        this.dataResposta = dataResposta;
    }

    public StatusEncaminhamento getStatus() {
        return status;
    }

    public void setStatus(StatusEncaminhamento status) {
        this.status = status;
    }
}
