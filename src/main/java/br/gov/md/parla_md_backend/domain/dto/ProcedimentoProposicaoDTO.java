package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoProposicaoDTO {

    private String id;
    private String proposicaoId;
    private LocalDateTime dataHora;
    private Integer sequencia;
    private String siglaOrgao;
    private String uriOrgao;
    private String descricaoTramitacao;
    private String despacho;
    private String regime;
    private String idTipoTramitacao;
    private String statusProposicao;
    private String uriUltimoRelator;
    private String urlDocumento;

    public static ProcedimentoProposicaoDTO from(ProcedimentoProposicao procedimento) {
        if (procedimento == null) {
            return null;
        }

        ProcedimentoProposicaoDTO dto = new ProcedimentoProposicaoDTO();
        dto.setId(procedimento.getId());
        dto.setProposicaoId(procedimento.getProposicao() != null ?
                procedimento.getProposicao().getId() : null);
        dto.setDataHora(procedimento.getDataHora());
        dto.setSequencia(procedimento.getSequencia());
        dto.setSiglaOrgao(procedimento.getSiglaOrgao());
        dto.setUriOrgao(procedimento.getUriOrgao());
        dto.setDescricaoTramitacao(procedimento.getDescricaoTramitacao());
        dto.setDespacho(procedimento.getDespacho());
        dto.setRegime(procedimento.getRegime());
        dto.setIdTipoTramitacao(procedimento.getIdTipoTramitacao());
        dto.setStatusProposicao(procedimento.getStatusProposicao());
        dto.setUriUltimoRelator(procedimento.getUriUltimoRelator());
        dto.setUrlDocumento(procedimento.getUrlDocumento());

        return dto;
    }

    public ProcedimentoProposicao toEntity() {
        ProcedimentoProposicao procedimento = new ProcedimentoProposicao();
        procedimento.setId(this.id);
        procedimento.setDataHora(this.dataHora);
        procedimento.setSequencia(this.sequencia);
        procedimento.setSiglaOrgao(this.siglaOrgao);
        procedimento.setUriOrgao(this.uriOrgao);
        procedimento.setDescricaoTramitacao(this.descricaoTramitacao);
        procedimento.setDespacho(this.despacho);
        procedimento.setRegime(this.regime);
        procedimento.setIdTipoTramitacao(this.idTipoTramitacao);
        procedimento.setStatusProposicao(this.statusProposicao);
        procedimento.setUriUltimoRelator(this.uriUltimoRelator);
        procedimento.setUrlDocumento(this.urlDocumento);

        return procedimento;
    }
}