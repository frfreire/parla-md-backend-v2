package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.Posicionamento;
import br.gov.md.parla_md_backend.domain.enums.TipoParlamentar;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "parladb.parlamentar")
public class Parlamentar {

    @Id
    private String id;
    private String nome;
    private String partido;
    private String estado;
    private String urlFoto;
    private byte[] fotoBytes;
    private List<String> idPropostas;
    private Posicionamento posicionamentoForcasArmadas;
    private TipoParlamentar tipo;

    private Map<String, TemaComportamento> comportamentos;

    // Campos específicos para Deputados
    private String idDeputado;
    private String gabinete;

    // Campos específicos para Senadores
    private String idSenador;
    private String legislatura;

    public Parlamentar(String id, String nome, String partido, String estado, String urlFoto, byte[] fotoBytes, List<String> idPropostas, Posicionamento posicionamentoForcasArmadas, TipoParlamentar tipo, String idDeputado, String gabinete, String idSenador, String legislatura) {
        this.id = id;
        this.nome = nome;
        this.partido = partido;
        this.estado = estado;
        this.urlFoto = urlFoto;
        this.fotoBytes = fotoBytes;
        this.idPropostas = idPropostas;
        this.posicionamentoForcasArmadas = posicionamentoForcasArmadas;
        this.tipo = tipo;
        this.comportamentos = new HashMap<>();
        this.idDeputado = idDeputado;
        this.gabinete = gabinete;
        this.idSenador = idSenador;
        this.legislatura = legislatura;
    }

    public Parlamentar(String id, String nome, String partido, String estado) {
        this.id = id;
        this.nome = nome;
        this.partido = partido;
        this.estado = estado;
        this.comportamentos = new HashMap<>();
        this.idPropostas = new ArrayList<>();
    }

    public Parlamentar() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPartido() {
        return partido;
    }

    public void setPartido(String partido) {
        this.partido = partido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public byte[] getFotoBytes() {
        return fotoBytes;
    }

    public void setFotoBytes(byte[] fotoBytes) {
        this.fotoBytes = fotoBytes;
    }

    public List<String> getIdPropostas() {
        return idPropostas;
    }

    public void setIdPropostas(List<String> idPropostas) {
        this.idPropostas = idPropostas;
    }

    public Posicionamento getPosicionamentoForcasArmadas() {
        return posicionamentoForcasArmadas;
    }

    public void setPosicionamentoForcasArmadas(Posicionamento posicionamentoForcasArmadas) {
        this.posicionamentoForcasArmadas = posicionamentoForcasArmadas;
    }

    public TipoParlamentar getTipo() {
        return tipo;
    }

    public void setTipo(TipoParlamentar tipo) {
        this.tipo = tipo;
    }

    public Map<String, TemaComportamento> getComportamentos() {
        return comportamentos;
    }

    public void setComportamentos(Map<String, TemaComportamento> comportamentos) {
        this.comportamentos = comportamentos;
    }

    public String getIdDeputado() {
        return idDeputado;
    }

    public void setIdDeputado(String idDeputado) {
        this.idDeputado = idDeputado;
    }

    public String getGabinete() {
        return gabinete;
    }

    public void setGabinete(String gabinete) {
        this.gabinete = gabinete;
    }

    public String getIdSenador() {
        return idSenador;
    }

    public void setIdSenador(String idSenador) {
        this.idSenador = idSenador;
    }

    public String getLegislatura() {
        return legislatura;
    }

    public void setLegislatura(String legislatura) {
        this.legislatura = legislatura;
    }

    public void addComportamento(String tema, TemaComportamento comportamento) {
        this.comportamentos.put(tema, comportamento);
    }

    public TemaComportamento getComportamento(String tema) {
        return this.comportamentos.get(tema);
    }
}
