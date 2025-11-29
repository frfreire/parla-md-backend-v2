package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "parladb.proposition")
@Data
public class Materia implements DocumentoLegislativo {

    @Id
    private String codigo;
    private String sigla;
    private int numero;
    private int ano;
    private String ementa;
    private String autor;
    private String statusMateria;
    private LocalDateTime ultimaAtualizacao;

    public Materia() {
    }

    public Materia(String codigo, String sigla, int numero, int ano, String ementa, String autor, String statusMateria, LocalDateTime ultimaAtualizacao) {
        this.codigo = codigo;
        this.sigla = sigla;
        this.numero = numero;
        this.ano = ano;
        this.ementa = ementa;
        this.autor = autor;
        this.statusMateria = statusMateria;
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getEmenta() {
        return ementa;
    }

    public void setEmenta(String ementa) {
        this.ementa = ementa;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getStatusMateria() {
        return statusMateria;
    }

    public void setStatusMateria(String statusMateria) {
        this.statusMateria = statusMateria;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getPartidoAutor() {
        return null;
    }

    @Override
    public String getEstadoAutor() {
        return null;
    }

    @Override
    public String getTipo() {
        return null;
    }

    @Override
    public LocalDateTime getDataApresentacao() {
        return null;
    }

    @Override
    public String getSituacao() {
        return null;
    }

    @Override
    public Double getProbabilidadeAprovacao() {
        return null;
    }

    @Override
    public String getResultadoVotacao() {
        return null;
    }

    @Override
    public String getTextoCompleto() {
        return null;
    }
}
