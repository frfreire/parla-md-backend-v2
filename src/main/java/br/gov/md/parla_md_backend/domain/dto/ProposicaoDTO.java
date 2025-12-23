package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.Encaminhamento;
import br.gov.md.parla_md_backend.domain.enums.TipoProposicao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposicaoDTO {

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
    private TipoProposicao tipoProposicao;
    private String tema;
    private String status;
    private boolean aprovada;
    private double approvalProbability;
    private StatusTriagem statusTriagem;
    private String setorAtual;
    private StatusTramitacao statusTramitacao;
    private String observacaoTriagem;
    private String parecer;
    private List<Encaminhamento> encaminhamentos;
    private StatusParecer statusParecer;

    public Proposicao paraProposicao() {
        Proposicao proposicao = new Proposicao();
        proposicao.setTema(this.tema);
        proposicao.setPartidoAutor(this.partidoAutor);
        proposicao.setEstadoAutor(this.estadoAutor);
        proposicao.setTipoProposicao(this.tipoProposicao);
        // isso é apenas para previsão
        return proposicao;
    }
}
