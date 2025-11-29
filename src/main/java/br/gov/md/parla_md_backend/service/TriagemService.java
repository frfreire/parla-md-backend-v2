package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.TriagemStatus;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TriagemService {

    private IProposicaoRepository propositionRepository;

    @Autowired
    public TriagemService(IProposicaoRepository propositionRepository){
        this.propositionRepository = propositionRepository;
    }

    public Page<Proposicao> getProposicoesNaoAvaliadas(Pageable pageable){
        return propositionRepository.findByTriagemStatus(TriagemStatus.NAO_AVALIADO, pageable);
    }

    public Page<Proposicao> getProposicoesInteresse(Pageable pageable) {
        return propositionRepository.findByTriagemStatus(TriagemStatus.INTERESSE, pageable);
    }

    public Page<Proposicao> getProposicoesDescartadas(Pageable pageable) {
        return propositionRepository.findByTriagemStatus(TriagemStatus.DESCARTADO, pageable);
    }

    public Proposicao avaliarProposicao(String id, TriagemStatus novoStatus, String observacao) {
        Proposicao proposicao = propositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        proposicao.setTriagemStatus(novoStatus);
        proposicao.setObservacaoTriagem(observacao);

        return propositionRepository.save(proposicao);
    }
}
