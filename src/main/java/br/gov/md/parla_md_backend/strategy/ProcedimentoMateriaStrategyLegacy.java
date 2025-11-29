package br.gov.md.parla_md_backend.strategy;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.service.ProcedimentoMateriaService;
import br.gov.md.parla_md_backend.service.strategy.IProcedimentoStrategy;
import org.springframework.stereotype.Component;

@Component("procedimentoMateriaStrategyLegacy")
public class ProcedimentoMateriaStrategyLegacy implements IProcedimentoStrategy<Materia> {

    private final ProcedimentoMateriaService procedimentoMateriaService;

    public ProcedimentoMateriaStrategyLegacy(ProcedimentoMateriaService procedimentoMateriaService) {
        this.procedimentoMateriaService = procedimentoMateriaService;
    }

    @Override
    public void buscarESalvarProcedimentos(Materia materia) {
        procedimentoMateriaService.fetchAndSaveProcedures(materia);
    }

    @Override
    public String getTipoProcedimento() {
        return "MATERIA_LEGACY"; // Tipo diferente para evitar conflito
    }

    @Override
    public boolean podeProcessar(Object projeto) {
        return false; // Desabilitada - versao legacy
    }
}
