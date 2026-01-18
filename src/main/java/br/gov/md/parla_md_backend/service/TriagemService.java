package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TriagemService {

    private final IProposicaoRepository propositionRepository;

    @Autowired
    public TriagemService(IProposicaoRepository propositionRepository){
        this.propositionRepository = propositionRepository;
    }

    public Page<Proposicao> getProposicoesNaoAvaliadas(Pageable pageable){
        return propositionRepository.findByStatusTriagem(StatusTriagem.NAO_AVALIADO, pageable);
    }

    public Page<Proposicao> getProposicoesInteresse(Pageable pageable) {
        return propositionRepository.findByStatusTriagem(StatusTriagem.INTERESSE, pageable);
    }

    public Page<Proposicao> getProposicoesDescartadas(Pageable pageable) {
        return propositionRepository.findByStatusTriagem(StatusTriagem.DESCARTADO, pageable);
    }

    public Proposicao avaliarProposicao(String id, StatusTriagem novoStatus, String observacao) {
        Proposicao proposicao = propositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposição não encontrada"));

        proposicao.setStatusProposicao(novoStatus.toString());
        proposicao.setObservacao(observacao);

        return propositionRepository.save(proposicao);
    }
}
