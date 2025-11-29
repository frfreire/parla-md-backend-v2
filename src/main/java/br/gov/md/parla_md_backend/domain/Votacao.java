package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "parladb.votacao")
public class Votacao {

    @Id
    private String id;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String siglaOrgao;
    private String uriProposicaoPrincipal;
    private String descricao;
    private String parlamentarId;
    private String proposicaoId;
    private String materiaId;
    private String voto;
    private LocalDateTime votoData;

    public Votacao() {
    }

    public Votacao(String id, LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String siglaOrgao, String uriProposicaoPrincipal, String descricao, String parlamentarId, String proposicaoId, String materiaId, String voto, LocalDateTime votoData) {
        this.id = id;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.siglaOrgao = siglaOrgao;
        this.uriProposicaoPrincipal = uriProposicaoPrincipal;
        this.descricao = descricao;
        this.parlamentarId = parlamentarId;
        this.proposicaoId = proposicaoId;
        this.materiaId = materiaId;
        this.voto = voto;
        this.votoData = votoData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public String getSiglaOrgao() {
        return siglaOrgao;
    }

    public void setSiglaOrgao(String siglaOrgao) {
        this.siglaOrgao = siglaOrgao;
    }

    public String getUriProposicaoPrincipal() {
        return uriProposicaoPrincipal;
    }

    public void setUriProposicaoPrincipal(String uriProposicaoPrincipal) {
        this.uriProposicaoPrincipal = uriProposicaoPrincipal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getParlamentarId() {
        return parlamentarId;
    }

    public void setParlamentarId(String parlamentarId) {
        this.parlamentarId = parlamentarId;
    }

    public String getProposicaoId() {
        return proposicaoId;
    }

    public void setProposicaoId(String proposicaoId) {
        this.proposicaoId = proposicaoId;
    }

    public String getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(String materiaId) {
        this.materiaId = materiaId;
    }

    public String getVoto() {
        return voto;
    }

    public void setVoto(String voto) {
        this.voto = voto;
    }

    public LocalDateTime getVotoData() {
        return votoData;
    }

    public void setVotoData(LocalDateTime votoData) {
        this.votoData = votoData;
    }
}
