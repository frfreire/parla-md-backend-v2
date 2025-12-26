package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
//import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.Casa;
import br.gov.md.parla_md_backend.domain.Proposicao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposicaoDTO {

    private String id;
    private Casa casa;
    private Long idCamara;

    private String siglaTipo;
    private String identificadorCompleto;
    private Integer numero;
    private Integer ano;
    private String ementa;
    private String ementaDetalhada;
    private String keywords;
    private String tema;
    private LocalDateTime dataApresentacao;
    private LocalDateTime dataCaptura;
    private LocalDateTime dataUltimaAtualizacao;

    private Long idDeputadoAutor;
    private String nomeDeputadoAutor;
    private String partidoAutor;
    private String estadoAutor;
    private String siglaUFAutor;
    private String uriAutor;

    private String statusProposicao;
    private String situacaoAtual;
    private String descricaoTramitacao;
    private String despacho;
    private String siglaOrgao;
    private String uriOrgaoNumerador;
    private String regime;
    private boolean apreciacao;
    private LocalDateTime dataHoraTramitacao;

    private String uriProposicao;
    private String urlInteiroTeor;
    private String linkPaginaCasa;

    private StatusTriagem statusTriagem;
    private StatusTramitacao statusTramitacao;
    private StatusParecer statusParecer;
    private String observacaoTriagem;
    private String parecer;

    private Double probabilidadeAprovacao;
    private boolean aprovada;

    public static ProposicaoDTO fromEntity(Proposicao proposicao) {
        if (proposicao == null) return null;

        return ProposicaoDTO.builder()
                .id(proposicao.getId())
                .casa(Casa.CAMARA)
                .idCamara(proposicao.getIdCamara())
                .siglaTipo(proposicao.getSiglaTipo())
                .identificadorCompleto(proposicao.getIdentificadorCompleto())
                .numero(Integer.valueOf(proposicao.getNumero()))
                .ano(proposicao.getAno())
                .ementa(proposicao.getEmenta())
                .ementaDetalhada(proposicao.getEmentaDetalhada())
                .keywords(proposicao.getKeywords())
                .tema(proposicao.getTema())
                .dataApresentacao(proposicao.getDataApresentacao().atStartOfDay())
                .dataCaptura(proposicao.getDataCaptura())
                .dataUltimaAtualizacao(proposicao.getDataUltimaAtualizacao())
                .idDeputadoAutor(proposicao.getIdDeputadoAutor())
                .nomeDeputadoAutor(proposicao.getNomeDeputadoAutor())
                .partidoAutor(proposicao.getPartidoAutor())
                .estadoAutor(proposicao.getEstadoAutor())
                .siglaUFAutor(proposicao.getSiglaUFAutor())
                .uriAutor(proposicao.getUriAutor())
                .statusProposicao(proposicao.getStatusProposicao())
                .situacaoAtual(proposicao.getSituacaoAtual())
                .descricaoTramitacao(proposicao.getDescricaoTramitacao())
                .despacho(proposicao.getDespacho())
                .siglaOrgao(proposicao.getSiglaOrgao())
                .uriOrgaoNumerador(proposicao.getUriOrgaoNumerador())
                .regime(proposicao.getRegime())
                .apreciacao(proposicao.isApreciacao())
                .uriProposicao(proposicao.getUriProposicao())
                .urlInteiroTeor(proposicao.getUrlInteiroTeor())
                .linkPaginaCasa(proposicao.getLinkPaginaCasa())
                .statusTriagem(proposicao.getStatusTriagem())
                .aprovada(proposicao.isAprovada())
                .build();
    }

    public Proposicao toEntity() {
        Proposicao proposicao = new Proposicao();
        proposicao.setId(this.id);
        proposicao.setIdCamara(this.idCamara);
        proposicao.setSiglaTipo(this.siglaTipo);
        proposicao.setNumero(String.valueOf(this.numero));
        proposicao.setAno(this.ano);
        proposicao.setEmenta(this.ementa);
        proposicao.setEmentaDetalhada(this.ementaDetalhada);
        proposicao.setKeywords(this.keywords);
        proposicao.setTema(this.tema);
        proposicao.setDataApresentacao(LocalDate.from(this.dataApresentacao));
        proposicao.setDataCaptura(this.dataCaptura);
        proposicao.setDataUltimaAtualizacao(this.dataUltimaAtualizacao);
        proposicao.setIdDeputadoAutor(this.idDeputadoAutor);
        proposicao.setNomeDeputadoAutor(this.nomeDeputadoAutor);
        proposicao.setPartidoAutor(this.partidoAutor);
        proposicao.setEstadoAutor(this.estadoAutor);
        proposicao.setSiglaUFAutor(this.siglaUFAutor);
        proposicao.setUriAutor(this.uriAutor);
        proposicao.setStatusProposicao(this.statusProposicao);
        proposicao.setSituacaoAtual(this.situacaoAtual);
        proposicao.setDescricaoTramitacao(this.descricaoTramitacao);
        proposicao.setDespacho(this.despacho);
        proposicao.setSiglaOrgao(this.siglaOrgao);
        proposicao.setUriOrgaoNumerador(this.uriOrgaoNumerador);
        proposicao.setRegime(this.regime);
        proposicao.setApreciacao(this.apreciacao);
        proposicao.setUriProposicao(this.uriProposicao);
        proposicao.setUrlInteiroTeor(this.urlInteiroTeor);
        proposicao.setLinkPaginaCasa(this.linkPaginaCasa);
        proposicao.setStatusTriagem(this.statusTriagem);
        proposicao.setAprovada(this.aprovada);
        return proposicao;
    }
}