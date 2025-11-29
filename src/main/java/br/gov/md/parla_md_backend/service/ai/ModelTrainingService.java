package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ModelTrainingService {

    private static final Logger logger = LoggerFactory.getLogger(ModelTrainingService.class);
    private static final String TRAINING_EXCHANGE = "model.training.exchange";
    private static final String TRAINING_ROUTING_KEY = "model.training.start";

    @Value("${rabbitmq.exchanges.model-training}")
    private String trainingExchange;

    @Value("${rabbitmq.routing-keys.model-training}")
    private String trainingRoutingKey;

    private final IProposicaoRepository propositionRepository;
    private final PredictionService predictionService;
    private final RabbitMQProducer rabbitMQProducer;

    @Autowired
    public ModelTrainingService(IProposicaoRepository propositionRepository,
                                PredictionService predictionService,
                                RabbitMQProducer rabbitMQProducer) {
        this.propositionRepository = propositionRepository;
        this.predictionService = predictionService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Executa todos os dias às 2:00 AM
    public void scheduledModelTraining() {
        logger.info("Iniciando treinamento agendado do modelo");
        rabbitMQProducer.sendMessage(TRAINING_EXCHANGE, TRAINING_ROUTING_KEY, "start_training");
    }

    public void performModelTraining() {
        logger.info("Iniciando processo de treinamento do modelo");
        List<Proposicao> allProposicaos = propositionRepository.findAll();
        predictionService.trainModel(allProposicaos);
        logger.info("Treinamento do modelo concluído");
    }
}
