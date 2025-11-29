package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.service.strategy.IProcedimentoStrategy;
import br.gov.md.parla_md_backend.exception.DominioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProcedimentoService {

    private static final Logger logger = LoggerFactory.getLogger(ProcedimentoService.class);
    private final Map<String, IProcedimentoStrategy<?>> strategies;

    public ProcedimentoService(List<IProcedimentoStrategy<?>> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        IProcedimentoStrategy::getTipoProcedimento,
                        Function.identity()
                ));
        logger.info("ProcedimentoService inicializado com {} estratégias", strategies.size());
    }

    @Transactional
    public void buscarESalvarTramitacoes(Object projeto) {
        if (projeto == null) {
            throw new DominioException("Projeto legislativo não pode ser nulo");
        }

        String tipo = determinarTipoProcedimento(projeto);
        IProcedimentoStrategy<?> strategy = strategies.get(tipo);

        if (strategy == null) {
            logger.error("Tipo de procedimento não suportado: {}", tipo);
            throw new DominioException("Tipo de procedimento não suportado: " + tipo);
        }

        try {
            logger.debug("Iniciando busca de tramitações para projeto usando estratégia {}", tipo);

            executarEstrategia(strategy, projeto);
            logger.info("Tramitações salvas com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao processar tramitações: {}", e.getMessage(), e);
            throw new DominioException("Falha ao processar tramitações: " + e.getMessage(), e);
        }
    }

    /**
     * CORREÇÃO: Novo método auxiliar genérico para executar a estratégia de forma segura.
     * Este método "captura" o tipo do wildcard (?) da estratégia e o utiliza para
     * fazer um cast seguro no objeto do projeto.
     */
    @SuppressWarnings("unchecked")
    private <T> void executarEstrategia(IProcedimentoStrategy<T> strategy, Object projeto) {

        strategy.buscarESalvarProcedimentos((T) projeto);
    }

    private String determinarTipoProcedimento(Object projeto) {
        if (projeto == null) {
            throw new DominioException("Projeto legislativo não pode ser nulo");
        }

        if (projeto.getClass().getSimpleName().equals("Materia")) {
            logger.debug("Projeto identificado como MATERIA do Senado");
            return "MATERIA";
        } else if (projeto.getClass().getSimpleName().equals("Proposicao")) {
            logger.debug("Projeto identificado como PROPOSICAO da Câmara");
            return "PROPOSICAO";
        }

        throw new DominioException("Tipo de projeto legislativo não reconhecido: " + projeto.getClass().getSimpleName());
    }
}