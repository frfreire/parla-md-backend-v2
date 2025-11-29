package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class PredictionService {
    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);
    private static final String MODEL_COLLECTION = "trained_models";
    private static final String CURRENT_MODEL_ID = "current_model";
    private static final String UNKNOWN_VALUE = "UNKNOWN";
    private static final int NUM_INPUTS = 4;
    private static final int NUM_OUTPUTS = 1;
    private static final int NUM_HIDDEN_NODES = 50;
    private static final int NUM_EPOCHS = 1000;

    private final IProposicaoRepository propositionRepository;
    private final MongoTemplate mongoTemplate;
    private MultiLayerNetwork model;
    private Map<String, Integer> featureEncodings;

    @Autowired
    public PredictionService(IProposicaoRepository propositionRepository, MongoTemplate mongoTemplate) {
        this.propositionRepository = propositionRepository;
        this.mongoTemplate = mongoTemplate;
        this.featureEncodings = new HashMap<>();
        loadModel();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledModelTraining() {
        logger.info("Iniciando treinamento agendado do modelo");
        onDemandTraining();
    }

    public void trainAndEvaluateModel(List<Proposicao> proposicaos) {
        trainModel(proposicaos);
        Evaluation evaluation = evaluateModel(proposicaos);
        logger.info("Avaliação do modelo após treinamento: {}", evaluation.stats());
    }

    public void trainModel(List<Proposicao> proposicaos) {
        prepareFeatureEncodings(proposicaos);
        DataSet trainingData = prepareTrainingData(proposicaos);

        model = new MultiLayerNetwork(createNetworkConfiguration());
        model.init();

        for (int i = 0; i < NUM_EPOCHS; i++) {
            model.fit(trainingData);
        }

        logger.info("Treinamento do modelo concluído");
    }

    private MultiLayerConfiguration createNetworkConfiguration() {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(NUM_INPUTS).nOut(NUM_HIDDEN_NODES)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(NUM_HIDDEN_NODES).nOut(NUM_HIDDEN_NODES)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .activation(Activation.SIGMOID)
                        .nIn(NUM_HIDDEN_NODES).nOut(NUM_OUTPUTS).build())
                .build();
    }

    public double predictApprovalProbability(Proposicao proposicao) {
        if (model == null) {
            throw new IllegalStateException("Modelo não treinado ainda.");
        }
        INDArray features = createFeatures(proposicao);
        INDArray output = model.output(features);
        return output.getDouble(0);
    }

    public Evaluation onDemandEvaluation() {
        if (model == null) {
            throw new IllegalStateException("Modelo não treinado");
        }
        List<Proposicao> proposicaos = propositionRepository.findAll();
        return evaluateModel(proposicaos);
    }

    public void onDemandCrossValidation(int numFolds) {
        List<Proposicao> proposicaos = propositionRepository.findAll();
        Collections.shuffle(proposicaos, new Random(123));

        int foldSize = proposicaos.size() / numFolds;
        List<Evaluation> evaluations = new ArrayList<>();

        for (int i = 0; i < numFolds; i++) {
            int validationStart = i * foldSize;
            int validationEnd = (i == numFolds - 1) ? proposicaos.size() : (i + 1) * foldSize;

            List<Proposicao> trainingSet = new ArrayList<>(proposicaos);
            List<Proposicao> validationSet = new ArrayList<>(trainingSet.subList(validationStart, validationEnd));
            trainingSet.removeAll(validationSet);

            trainModel(trainingSet);
            Evaluation evaluation = evaluateModel(validationSet);
            evaluations.add(evaluation);

            logger.info("Fold {}: {}", i + 1, evaluation.stats());
        }

        logAveragePerformance(evaluations);
    }

    private void logAveragePerformance(List<Evaluation> evaluations) {
        double avgAccuracy = evaluations.stream().mapToDouble(Evaluation::accuracy).average().orElse(0);
        double avgPrecision = evaluations.stream().mapToDouble(Evaluation::precision).average().orElse(0);
        double avgRecall = evaluations.stream().mapToDouble(Evaluation::recall).average().orElse(0);
        double avgF1 = evaluations.stream().mapToDouble(Evaluation::f1).average().orElse(0);

        logger.info("Cross-Validation Average Performance:");
        logger.info("Accuracy: {}", avgAccuracy);
        logger.info("Precision: {}", avgPrecision);
        logger.info("Recall: {}", avgRecall);
        logger.info("F1 Score: {}", avgF1);
    }

    private Evaluation evaluateModel(List<Proposicao> proposicaos) {
        DataSet dataSet = prepareTrainingData(proposicaos);
        INDArray output = model.output(dataSet.getFeatures());
        Evaluation eval = new Evaluation(2);
        eval.eval(dataSet.getLabels(), output);
        return eval;
    }

    public void saveModel() {
        if (model == null) {
            logger.warn("Nenhum modelo para salvar");
            return;
        }
        try {
            ByteArrayOutputStream modelStream = new ByteArrayOutputStream();
            ModelSerializer.writeModel(model, modelStream, true);
            byte[] modelBytes = modelStream.toByteArray();

            mongoTemplate.save(Map.of(
                    "_id", CURRENT_MODEL_ID,
                    "model", modelBytes,
                    "timestamp", new Date()
            ), MODEL_COLLECTION);
            logger.info("Modelo salvo com sucesso no MongoDB");
        } catch (IOException e) {
            logger.error("Erro ao salvar o modelo no MongoDB", e);
        }
    }

    public void loadModel() {
        Map modelData = mongoTemplate.findById(CURRENT_MODEL_ID, Map.class, MODEL_COLLECTION);

        if (modelData == null || !modelData.containsKey("model")) {
            logger.info("Nenhum modelo salvo encontrado no MongoDB");
            return;
        }

        try {
            byte[] modelBytes = (byte[]) modelData.get("model");
            ByteArrayInputStream modelStream = new ByteArrayInputStream(modelBytes);
            model = ModelSerializer.restoreMultiLayerNetwork(modelStream, true);
            logger.info("Modelo carregado com sucesso do MongoDB");
        } catch (IOException e) {
            logger.error("Erro ao carregar o modelo do MongoDB", e);
        }
    }

    private void prepareFeatureEncodings(List<Proposicao> proposicaos) {
        Set<String> themes = new HashSet<>();
        Set<String> parties = new HashSet<>();
        Set<String> states = new HashSet<>();
        Set<String> types = new HashSet<>();

        for (Proposicao prop : proposicaos) {
            themes.add(getOrUnknown(prop.getTema()));
            parties.add(getOrUnknown(prop.getPartidoAutor()));
            states.add(getOrUnknown(prop.getEstadoAutor()));
            types.add(getOrUnknown(prop.getTipoProposicao()));
        }

        int index = 0;
        for (String theme : themes) featureEncodings.put("THEME_" + theme, index++);
        for (String party : parties) featureEncodings.put("PARTY_" + party, index++);
        for (String state : states) featureEncodings.put("STATE_" + state, index++);
        for (String type : types) featureEncodings.put("TYPE_" + type, index++);
    }

    private DataSet prepareTrainingData(List<Proposicao> proposicaos) {
        int numFeatures = featureEncodings.size();
        INDArray features = Nd4j.zeros(proposicaos.size(), numFeatures);
        INDArray labels = Nd4j.zeros(proposicaos.size(), 1);

        for (int i = 0; i < proposicaos.size(); i++) {
            Proposicao prop = proposicaos.get(i);
            features.putRow(i, createFeatures(prop));
            labels.putScalar(i, 0, prop.isAprovada() ? 1.0 : 0.0);
        }

        return new DataSet(features, labels);
    }

    private INDArray createFeatures(Proposicao proposicao) {
        INDArray features = Nd4j.zeros(1, featureEncodings.size());

        setFeature(features, "THEME_", proposicao.getTema());
        setFeature(features, "PARTY_", proposicao.getPartidoAutor());
        setFeature(features, "STATE_", proposicao.getEstadoAutor());
        setFeature(features, "TYPE_", proposicao.getTipoProposicao());

        return features;
    }

    private void setFeature(INDArray features, String prefix, String value) {
        String key = prefix + getOrUnknown(value);
        Integer index = featureEncodings.get(key);
        if (index != null) {
            features.putScalar(0, index, 1.0);
        } else {
            logger.warn("Característica não encontrada: {}", key);
        }
    }

    private String getOrUnknown(String value) {
        return (value == null || value.trim().isEmpty()) ? UNKNOWN_VALUE : value.trim();
    }

    public void onDemandTraining() {
        logger.info("Iniciando treinamento sob demanda do modelo");
        List<Proposicao> proposicaos = propositionRepository.findAll();
        trainAndEvaluateModel(proposicaos);
        saveModel();
        logger.info("Treinamento sob demanda concluído e modelo salvo");
    }

    public boolean isModelTrained() {
        if (model == null) {
            return false;
        }
        // Verifica se o modelo tem pesos inicializados
        return model.params() != null && model.params().length() > 0;
    }
}