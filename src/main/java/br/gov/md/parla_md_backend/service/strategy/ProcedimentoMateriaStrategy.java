package br.gov.md.parla_md_backend.service.strategy;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.service.ProcedimentoMateriaService;
import br.gov.md.parla_md_backend.exception.DominioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcedimentoMateriaStrategy implements IProcedimentoStrategy<Materia> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcedimentoMateriaStrategy.class);
    private final ProcedimentoMateriaService procedimentoMateriaService;

    public ProcedimentoMateriaStrategy(ProcedimentoMateriaService procedimentoMateriaService) {
        this.procedimentoMateriaService = procedimentoMateriaService;
    }

    @Override
    public void buscarESalvarProcedimentos(Materia materia) {
        try {
            logger.debug("Processando procedimentos para matéria ID={}", materia.getId());
            procedimentoMateriaService.fetchAndSaveProcedures(materia);
            logger.debug("Procedimentos salvos com sucesso para matéria ID={}", materia.getId());
        } catch (Exception e) {
            logger.error("Erro ao processar procedimentos da matéria {}: {}", 
                materia.getId(), e.getMessage());
            throw new DominioException("Falha ao processar procedimentos da matéria: " + e.getMessage(), e);
        }
    }

    @Override
    public String getTipoProcedimento() {
        return "MATERIA";
    }

    public boolean podeProcessar(Object projeto) {
        return projeto instanceof Materia;
    }
}