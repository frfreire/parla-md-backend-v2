package br.gov.md.parla_md_backend.domain;


import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.TriagemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "parladb.proposition")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Proposicao implements DocumentoLegislativo {
    @Id
    private String id;

    // Campos da API da Câmara
    private String uri;
    private String siglaTipo;
    private int codTipo;
    private int numero;
    private int ano;
    private String ementa;
    private LocalDateTime dataApresentacao;

    // Campos do statusProposicao
    private String uriUltimoRelator;
    private String descricaoTramitacao;
    private String idTipoTramitacao;
    private String despacho;
    private LocalDateTime dataHora;
    private int sequencia;
    private String siglaOrgao;
    private String uriOrgao;
    private String regime;
    private String descricaoSituacao;
    private int idSituacao;
    private String apreciacao;
    private String url;

    // Campos locais
    private String titulo;
    private String autorId;
    private String autorNome;
    private String partidoAutor;
    private String estadoAutor;
    private String tipoProposicao;
    private String tema;
    private String status;
    private boolean aprovada;
    private double probabilidadeAprovacao;
    private TriagemStatus triagemStatus;
    private String setorAtual;
    private StatusTramitacao statusTramitacao;
    private String observacaoTriagem;
    private String parecer;
    private List<Encaminhamento> encaminhamentos;
    private StatusParecer statusParecer;

    public void setProbabilidadeAprovacao(double probabilidadeAprovacao) {
        if (probabilidadeAprovacao < 0 || probabilidadeAprovacao > 1) {
            throw new IllegalArgumentException("A probabilidade de aprovação deve estar entre 0 e 1");
        }
        this.probabilidadeAprovacao = probabilidadeAprovacao;
    }

    @Override
    public String getAutor() {
        return null;
    }

    @Override
    public String getTipo() {
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
