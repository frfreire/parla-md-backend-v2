package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.old.Despacho;
import br.gov.md.parla_md_backend.domain.old.Opiniao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.repository.IDespachoRepository;
import br.gov.md.parla_md_backend.repository.IOpiniaoRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProcessamentoService {

    private IProposicaoRepository propositionRepository;
    private IOpiniaoRepository opinionRepository;
    private IDespachoRepository dispachRepository;

    public ProcessamentoService(IProposicaoRepository propositionRepository, IOpiniaoRepository opinionRepository, IDespachoRepository dispachRepository) {
        this.propositionRepository = propositionRepository;
        this.opinionRepository = opinionRepository;
        this.dispachRepository = dispachRepository;
    }

    public Opiniao emitirParecer(String propositionId, String conteudo, String usuarioId, String setorEmissor) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        if (!setorEmissor.equals(proposicao.getSetorAtual())) {
            throw new IllegalStateException("Apenas o setor atual pode emitir parecer");
        }

        Opiniao opiniao = new Opiniao();
        opiniao.setPropositionId(propositionId);
        opiniao.setConteudo(conteudo);
        opiniao.setUsuarioId(usuarioId);
        opiniao.setSetorEmissor(setorEmissor);
        opiniao.setDataEmissao(new Date());

        return opinionRepository.save(opiniao);
    }

    public Despacho emitirDespacho(String propositionId, String conteudo, String usuarioId,
                                   String setorOrigem, String setorDestino) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        if (!setorOrigem.equals(proposicao.getSetorAtual())) {
            throw new IllegalStateException("Apenas o setor atual pode emitir despacho");
        }

        Despacho dispatch = new Despacho();
        dispatch.setPropositionId(propositionId);
        dispatch.setConteudo(conteudo);
        dispatch.setUsuarioId(usuarioId);
        dispatch.setSetorOrigem(setorOrigem);
        dispatch.setSetorDestino(setorDestino);
        dispatch.setDataEmissao(new Date());

        proposicao.setSetorAtual(setorDestino);
        proposicao.setStatusTramitacao(StatusTramitacao.AGUARDANDO_PARECER);
        propositionRepository.save(proposicao);

        return dispachRepository.save(dispatch);
    }

    public List<Opiniao> getPareceresByProposition(String propositionId) {
        return opinionRepository.findByPropositionId(propositionId);
    }

    public List<Despacho> getDespachosByProposition(String propositionId) {
        return dispachRepository.findByPropositionId(propositionId);
    }

    public Proposicao getProposition(String propositionId, String setorUsuario) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        if (!setorUsuario.equals(proposicao.getSetorAtual())) {
            // Se não for o setor atual, retorna uma versão somente leitura
            proposicao.setStatusTramitacao(null);  // Oculta informações sensíveis
            // Ocultar outros campos conforme necessário
        }

        return proposicao;
    }
}
