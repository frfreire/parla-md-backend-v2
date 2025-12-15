package br.gov.md.parla_md_backend.strategy;

import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.service.ProcedimentoProposicaoService;
import br.gov.md.parla_md_backend.service.strategy.IProcedimentoStrategy;
import org.springframework.stereotype.Component;

@Component
public class ProcedimentoProposicaoStrategy implements IProcedimentoStrategy<Proposicao> {

    private final ProcedimentoProposicaoService procedimentoProposicaoService;

    public ProcedimentoProposicaoStrategy(ProcedimentoProposicaoService procedimentoProposicaoService) {
        this.procedimentoProposicaoService = procedimentoProposicaoService;
    }

    @Override
    public void buscarESalvarProcedimentos(Proposicao proposicao) {
        procedimentoProposicaoService.fetchAndSaveProcedures(proposicao);
    }

    @Override
    public String getTipoProcedimento() {
        return "PROPOSICAO";
    }
    
    @Override
    public boolean podeProcessar(Object projeto) {
        return projeto instanceof Proposicao;
    }
}