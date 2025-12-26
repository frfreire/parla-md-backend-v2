package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.Parecer;
import br.gov.md.parla_md_backend.domain.enums.RecomendacaoParecer;
import br.gov.md.parla_md_backend.domain.enums.TipoParecer;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ParecerDTO(
        String id,
        String numero,
        String processoId,
        String setorEmissorId,
        String setorEmissorNome,
        String analistaResponsavelId,
        String analistaResponsavelNome,
        TipoParecer tipo,
        String assunto,
        String contexto,
        String analise,
        RecomendacaoParecer recomendacao,
        String justificativaRecomendacao,
        List<String> fundamentacaoLegal,
        List<String> impactosIdentificados,
        String conclusao,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataEmissao,
        LocalDateTime prazo,
        boolean atendidoPrazo,
        String aprovadoPorId,
        String aprovadoPorNome,
        LocalDateTime dataAprovacao,
        List<String> anexos,
        String observacoes,
        ControleVisibilidadeDTO controleVisibilidade
) {
    public static ParecerDTO fromEntity(Parecer parecer) {
        ControleVisibilidadeDTO visibilidadeDTO = null;

        if (parecer.getControleVisibilidade() != null) {
            var cv = parecer.getControleVisibilidade();
            visibilidadeDTO = new ControleVisibilidadeDTO(
                    cv.getNivelVisibilidade(),
                    cv.getSetoresAutorizados(),
                    cv.getPermissoesIndividuais() != null ?
                            cv.getPermissoesIndividuais().stream()
                                    .filter(p -> p.isValida())
                                    .map(p -> p.getUsuarioId())
                                    .toList() :
                            null,
                    cv.getJustificativaRestricao(),
                    cv.isPermitirVisualizacaoSuperior(),
                    cv.getDataExpiracao()
            );
        }

        return ParecerDTO.builder()
                .id(parecer.getId())
                .numero(parecer.getNumero())
                .processoId(parecer.getProcessoId())
                .setorEmissorId(parecer.getSetorEmissorId())
                .setorEmissorNome(parecer.getSetorEmissorNome())
                .analistaResponsavelId(parecer.getAnalistaResponsavelId())
                .analistaResponsavelNome(parecer.getAnalistaResponsavelNome())
                .tipo(parecer.getTipo())
                .assunto(parecer.getAssunto())
                .contexto(parecer.getContexto())
                .analise(parecer.getAnalise())
                .recomendacao(parecer.getRecomendacao())
                .justificativaRecomendacao(parecer.getJustificativaRecomendacao())
                .fundamentacaoLegal(parecer.getFundamentacaoLegal())
                .impactosIdentificados(parecer.getImpactosIdentificados())
                .conclusao(parecer.getConclusao())
                .dataSolicitacao(parecer.getDataSolicitacao())
                .dataEmissao(parecer.getDataEmissao())
                .prazo(parecer.getPrazo())
                .atendidoPrazo(parecer.isAtendidoPrazo())
                .aprovadoPorId(parecer.getAprovadoPorId())
                .aprovadoPorNome(parecer.getAprovadoPorNome())
                .dataAprovacao(parecer.getDataAprovacao())
                .anexos(parecer.getAnexos())
                .observacoes(parecer.getObservacoes())
                .controleVisibilidade(visibilidadeDTO)
                .build();
    }
}