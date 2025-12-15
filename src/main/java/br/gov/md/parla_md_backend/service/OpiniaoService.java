package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Encaminhamento;
import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusEncaminhamento;
import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.repository.IEncaminhamentoRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OpiniaoService {


    private IProposicaoRepository propositionRepository;
    private IEncaminhamentoRepository forwardingRepository;

    public OpiniaoService(IProposicaoRepository propositionRepository, IEncaminhamentoRepository forwardingRepository) {
        this.propositionRepository = propositionRepository;
        this.forwardingRepository = forwardingRepository;
    }

    public Proposicao iniciarParecer(String propositionId) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        if (proposicao.getTriagemStatus() != StatusTriagem.INTERESSE) {
            throw new IllegalStateException("Apenas proposições de interesse podem ter pareceres iniciados");
        }

        proposicao.setStatusParecer(StatusParecer.EM_ELABORACAO);
        return propositionRepository.save(proposicao);
    }

    public Proposicao atualizarParecer(String propositionId, String parecer) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        proposicao.setParecer(parecer);
        return propositionRepository.save(proposicao);
    }

    public Encaminhamento encaminharParaSetor(String propositionId, String setorDestino, String solicitacao) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        Encaminhamento encaminhamento = new Encaminhamento();
        encaminhamento.setPropositionId(propositionId);
        encaminhamento.setSetorDestino(setorDestino);
        encaminhamento.setSolicitacao(solicitacao);
        encaminhamento.setDataSolicitacao(new Date());
        encaminhamento.setStatus(StatusEncaminhamento.PENDENTE);

        proposicao.setStatusParecer(StatusParecer.AGUARDANDO_RESPOSTA_SETOR);
        propositionRepository.save(proposicao);

        return forwardingRepository.save(encaminhamento);
    }

    public Encaminhamento responderEncaminhamento(String encaminhamentoId, String resposta) {
        Encaminhamento encaminhamento = forwardingRepository.findById(encaminhamentoId)
                .orElseThrow(() -> new RuntimeException("Encaminhamento não encontrado"));

        encaminhamento.setResposta(resposta);
        encaminhamento.setDataResposta(new Date());
        encaminhamento.setStatus(StatusEncaminhamento.RESPONDIDO);

        Proposicao proposicao = propositionRepository.findById(encaminhamento.getPropositionId())
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));
        proposicao.setStatusParecer(StatusParecer.EM_ELABORACAO);
        propositionRepository.save(proposicao);

        return forwardingRepository.save(encaminhamento);
    }

    public Proposicao concluirParecer(String propositionId) {
        Proposicao proposicao = propositionRepository.findById(propositionId)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        proposicao.setStatusParecer(StatusParecer.CONCLUIDO);
        return propositionRepository.save(proposicao);
    }

    public List<Encaminhamento> getEncaminhamentosByProposition(String propositionId) {
        return forwardingRepository.findByPropositionId(propositionId);
    }
}
