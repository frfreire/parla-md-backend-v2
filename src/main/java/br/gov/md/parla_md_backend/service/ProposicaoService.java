package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.exception.DominioException;
import br.gov.md.parla_md_backend.exception.ValidacaoException;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.service.ai.PredictionService;
import br.gov.md.parla_md_backend.exception.EntidadeNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ProposicaoService {

    private static final Logger logger = LoggerFactory.getLogger(ProposicaoService.class);
    private static final String EXCHANGE_PROPOSICAO = "proposicao.exchange";
    private static final String ROTA_NOVA_PROPOSICAO = "proposicao.nova";
    private static final double PROBABILIDADE_PADRAO = 0.3;
    private static final Set<String> TIPOS_PROPOSICAO_VALIDOS = Set.of(
            "PL", "PEC", "MPV", "PDL", "PLP", "PFC", "REQ"
    );

    private final IProposicaoRepository proposicaoRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final PredictionService predictionService;
    private final ProcedimentoService procedureService;

    public ProposicaoService(
            IProposicaoRepository proposicaoRepository,
            RabbitMQProducer rabbitMQProducer,
            PredictionService predictionService,
            ProcedimentoService procedureService) {
        this.proposicaoRepository = proposicaoRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.predictionService = predictionService;
        this.procedureService = procedureService;
    }

    @Transactional
    public Proposicao salvarProposicao(Proposicao proposicao) {
        if (proposicao == null) {
            throw new ValidacaoException("Proposição não pode ser nula");
        }

        validarProposicao(proposicao);
        validarRegrasNegocio(proposicao);

        try {
            Proposicao proposicaoEnriquecida = enriquecerComProbabilidadeAprovacao(proposicao);
            Proposicao proposicaoSalva = persistirProposicao(proposicaoEnriquecida);
            publicarProposicao(proposicaoSalva);

            procedureService.buscarESalvarTramitacoes(proposicaoSalva);
            logger.info("Proposição salva com sucesso: ID={}", proposicaoSalva.getId());

            return proposicaoSalva;
        } catch (Exception e) {
            logger.error("Erro ao salvar proposição: {}", e.getMessage(), e);
            throw new DominioException("Falha ao salvar proposição: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Proposicao buscarProposicaoPorId(String id) {
        return proposicaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNotFoundException("Proposição", id));
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarTodasProposicoes() {
        return proposicaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarProposicoesPorTema(String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            throw new ValidacaoException("Tema não pode ser vazio");
        }
        return proposicaoRepository.findByTema(tema);
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarProposicoesPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new ValidacaoException("Datas de início e fim são obrigatórias");
        }
        if (inicio.isAfter(fim)) {
            throw new ValidacaoException("Data inicial não pode ser posterior à data final");
        }
        return proposicaoRepository.findByDataApresentacaoBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarProposicoesPorAutor(String autorId) {
        if (autorId == null || autorId.trim().isEmpty()) {
            throw new ValidacaoException("ID do autor é obrigatório");
        }
        return proposicaoRepository.findByAutorId(autorId);
    }

    @Transactional
    public Proposicao atualizarProposicao(Proposicao proposicao) {
        if (!proposicaoRepository.existsById(proposicao.getId())) {
            throw new EntidadeNotFoundException("Proposição", proposicao.getId());
        }
        return salvarProposicao(proposicao);
    }

    @Transactional
    public void excluirProposicao(String id) {
        if (!proposicaoRepository.existsById(id)) {
            throw new EntidadeNotFoundException("Proposição", id);
        }
        proposicaoRepository.deleteById(id);
        logger.info("Proposição excluída com sucesso: ID={}", id);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void treinarModeloComTodasProposicoes() {
        List<Proposicao> todasProposicoes = buscarTodasProposicoes();
        treinarModelo(todasProposicoes);
        logger.info("Treinamento do modelo concluído com {} proposições", todasProposicoes.size());
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void reprocessarProbabilidadesProposicoes() {
        List<Proposicao> todasProposicoes = buscarTodasProposicoes();
        int processadas = 0;
        int falhas = 0;

        for (Proposicao proposicao : todasProposicoes) {
            try {
                Proposicao proposicaoAtualizada = enriquecerComProbabilidadeAprovacao(proposicao);
                persistirProposicao(proposicaoAtualizada);
                processadas++;
            } catch (Exception e) {
                logger.error("Erro ao reprocessar probabilidade da proposição {}: {}",
                        proposicao.getId(), e.getMessage(), e);
                falhas++;
            }
        }

        logger.info("Reprocessamento concluído. Processadas: {}, Falhas: {}", processadas, falhas);
    }

    private void validarProposicao(Proposicao proposicao) {
        if (proposicao.getSiglaTipo() == null || proposicao.getSiglaTipo().isEmpty()) {
            throw new ValidacaoException("Sigla do tipo da proposição é obrigatória");
        }

        if (proposicao.getAno() <= 0) {
            throw new ValidacaoException("Ano da proposição deve ser maior que zero");
        }

        if (proposicao.getNumero() <= 0) {
            throw new ValidacaoException("Número da proposição deve ser maior que zero");
        }

        if (proposicao.getEmenta() == null || proposicao.getEmenta().trim().isEmpty()) {
            throw new ValidacaoException("Ementa da proposição é obrigatória");
        }
    }

    private void validarRegrasNegocio(Proposicao proposicao) {
        if (proposicao.getAno() < 1988) {
            throw new ValidacaoException("Ano da proposição não pode ser anterior à Constituição de 1988");
        }

        if (!isTipoProposicaoValido(proposicao.getSiglaTipo())) {
            throw new ValidacaoException("Tipo de proposição inválido: " + proposicao.getSiglaTipo());
        }
    }

    private boolean isTipoProposicaoValido(String siglaTipo) {
        return TIPOS_PROPOSICAO_VALIDOS.contains(siglaTipo);
    }

    private void treinarModelo(List<Proposicao> proposicoes) {
        predictionService.trainModel(proposicoes);
    }

    private Proposicao enriquecerComProbabilidadeAprovacao(Proposicao proposicao) {
        try {
            double probabilidadeAprovacao = predictionService.predictApprovalProbability(proposicao);
            proposicao.setProbabilidadeAprovacao(probabilidadeAprovacao);
            return proposicao;
        } catch (Exception e) {
            logger.warn("Não foi possível calcular probabilidade de aprovação: {}", e.getMessage());
            proposicao.setProbabilidadeAprovacao(PROBABILIDADE_PADRAO);
            return proposicao;
        }
    }

    private Proposicao persistirProposicao(Proposicao proposicao) {
        return proposicaoRepository.save(proposicao);
    }

    private void publicarProposicao(Proposicao proposicao) {
        try {
            rabbitMQProducer.sendMessage(EXCHANGE_PROPOSICAO, ROTA_NOVA_PROPOSICAO, proposicao);
            logger.debug("Mensagem publicada com sucesso para proposição ID={}", proposicao.getId());
        } catch (Exception e) {
            logger.error("Erro ao publicar mensagem da proposição: {}", e.getMessage());
            // Não lançamos exceção aqui para não impedir o salvamento da proposição
        }
    }
}