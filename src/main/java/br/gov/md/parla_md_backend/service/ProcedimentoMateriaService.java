package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.ProcedimentoMateria;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IProcedimentoMateriaRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProcedimentoMateriaService {

    private static final Logger logger = LoggerFactory.getLogger(ProcedimentoMateriaService.class);
    private static final String PROCEDURE_EXCHANGE = "procedure.exchange";
    private static final String PROCEDURE_ROUTING_KEY = "procedure.new";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${senado.api.base-url}")
    private String senadoApiBaseUrl;

    private final ApiClient apiClient;
    private final IProcedimentoMateriaRepository procedureRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private SenadoService senadoService;

    @Autowired
    public ProcedimentoMateriaService(ApiClient apiClient,
                                      IProcedimentoMateriaRepository procedureRepository,
                                      RabbitMQProducer rabbitMQProducer,
                                      SenadoService senadoService) {
        this.apiClient = apiClient;
        this.procedureRepository = procedureRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.senadoService = senadoService;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Executa todos os dias às 2:00 AM
    public void scheduledUpdateAllMatterProcedures() {

        logger.info("Iniciando atualização agendada de procedimentos de matérias às {}", LocalDateTime.now());

        AtomicInteger totalMatters = new AtomicInteger(0);
        AtomicInteger updatedMatters = new AtomicInteger(0);
        AtomicInteger errorMatters = new AtomicInteger(0);

        try {
            List<Materia> allMaterias = senadoService.fetchAllMatters();
            totalMatters.set(allMaterias.size());

            allMaterias.parallelStream().forEach(matter -> {
                try {
                    List<ProcedimentoMateria> procedures = fetchAndSaveProcedures(matter);
                    logger.debug("Atualizados {} procedimentos para a matéria {}", procedures.size(), matter.getCodigo());
                    updatedMatters.incrementAndGet();

                    if (!procedures.isEmpty()) {
                        ProcedimentoMateria latestProcedure = procedures.get(procedures.size() - 1);
                        matter.setStatusMateria(latestProcedure.getStatusMateria());
                        matter.setUltimaAtualizacao(LocalDateTime.now());
                        senadoService.updateMatter(matter);
                    }

                    // Publicar evento de atualização no RabbitMQ
                    rabbitMQProducer.sendMessage("matter.exchange", "matter.updated", matter);

                } catch (Exception e) {
                    logger.error("Erro ao atualizar procedimentos para a matéria {}: {}", matter.getCodigo(), e.getMessage());
                    errorMatters.incrementAndGet();
                }
            });

        } catch (Exception e) {
            logger.error("Erro ao buscar matérias: {}", e.getMessage());
        } finally {
            logger.info("Atualização de procedimentos concluída. Total de matérias: {}, Atualizadas: {}, Erros: {}",
                    totalMatters.get(), updatedMatters.get(), errorMatters.get());
        }
    }

    @RabbitListener(queues = "matter.queue")
    public void processMatterMessage(String message) {
        try {
            JSONObject matterJson = new JSONObject(message);
            Materia materia = senadoService.findMatterById(matterJson.getString("codigo"));
            if (materia != null) {
                fetchAndSaveProcedures(materia);
            }
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
        }
    }

    public List<ProcedimentoMateria> fetchAndSaveProcedures(Materia materia) {
        try {
            String jsonData = fetchProceduresJsonData(materia.getCodigo());
            List<ProcedimentoMateria> procedures = parseProceduresFromJson(jsonData, materia);
            return saveProcedures(procedures);
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String fetchProceduresJsonData(String matterId) {
        String endpoint = senadoApiBaseUrl + "materia/" + matterId + "/tramitacoes";
        return apiClient.get(endpoint);
    }

    private List<ProcedimentoMateria> parseProceduresFromJson(String jsonData, Materia materia) {
        List<ProcedimentoMateria> procedures = new ArrayList<>();
        JSONObject json = new JSONObject(jsonData);
        JSONArray dados = json.getJSONArray("dados");

        for (int i = 0; i < dados.length(); i++) {
            JSONObject procedureData = dados.getJSONObject(i);
            ProcedimentoMateria procedure = createProcedureFromJson(procedureData, materia);
            procedures.add(procedure);
        }

        return procedures;
    }

    private ProcedimentoMateria createProcedureFromJson(JSONObject procedureData, Materia materia) {
        ProcedimentoMateria procedure = new ProcedimentoMateria();
        procedure.setMateria(materia);
        procedure.setSequencia(procedureData.getInt("sequencia"));
        procedure.setDataHora(LocalDateTime.parse(procedureData.getString("dataHora"), DATE_TIME_FORMATTER));
        procedure.setSiglaOrgao(procedureData.getString("siglaOrgao"));
        procedure.setUriOrgao(procedureData.getString("uriOrgao"));
        procedure.setDescricaoTramitacao(procedureData.getString("descricaoTramitacao"));
        procedure.setDespacho(procedureData.optString("despacho", null));
        procedure.setRegime(procedureData.optString("regime", null));
        procedure.setIdTipoTramitacao(procedureData.optString("idTipoTramitacao", null));
        procedure.setStatusMateria(procedureData.optString("statusProposicao", null));
        procedure.setUriUltimoRelator(procedureData.optString("uriUltimoRelator", null));
        procedure.setUrlDocumento(procedureData.optString("urlDocumento", null));
        return procedure;
    }

    private List<ProcedimentoMateria> saveProcedures(List<ProcedimentoMateria> procedures) {
        List<ProcedimentoMateria> savedProcedures = procedureRepository.saveAll(procedures);
        savedProcedures.forEach(this::publishProcedure);
        return savedProcedures;
    }

    private void publishProcedure(ProcedimentoMateria procedure) {
        rabbitMQProducer.sendMessage(PROCEDURE_EXCHANGE, PROCEDURE_ROUTING_KEY, procedure);
    }

    private List<ProcedimentoMateria> parseProceduresFromXml(String xmlData, Materia materia) {
        List<ProcedimentoMateria> procedures = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            NodeList tramitacaoNodes = doc.getElementsByTagName("Tramitacao");
            for (int i = 0; i < tramitacaoNodes.getLength(); i++) {
                Element tramitacaoElement = (Element) tramitacaoNodes.item(i);
                ProcedimentoMateria procedure = createProcedureFromXml(tramitacaoElement, materia);
                procedures.add(procedure);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return procedures;
    }

    private ProcedimentoMateria createProcedureFromXml(Element tramitacaoElement, Materia materia) {
        ProcedimentoMateria procedure = new ProcedimentoMateria();

        procedure.setMateria(materia);
        procedure.setSequencia(Integer.parseInt(getElementTextContent(tramitacaoElement, "SequenciaTramitacao", "0")));
        procedure.setDataHora(parseDateTime(getElementTextContent(tramitacaoElement, "DataHoraTramitacao", null)));
        procedure.setSiglaOrgao(getElementTextContent(tramitacaoElement, "SiglaOrgao", ""));
        procedure.setUriOrgao(getElementTextContent(tramitacaoElement, "IdentificacaoOrgao", null));
        procedure.setDescricaoTramitacao(getElementTextContent(tramitacaoElement, "DescricaoTramitacao", ""));
        procedure.setDespacho(getElementTextContent(tramitacaoElement, "TextoTramitacao", ""));
        procedure.setRegime(getElementTextContent(tramitacaoElement, "IndicadorRegimeTramitacao", null));
        procedure.setIdTipoTramitacao(getElementTextContent(tramitacaoElement, "CodigoTipoTramitacao", null));
        procedure.setStatusMateria(getElementTextContent(tramitacaoElement, "IndicadorSituacaoTramitacao", null));
        procedure.setUriUltimoRelator(getElementTextContent(tramitacaoElement, "IdentificacaoRelator", null));
        procedure.setUrlDocumento(getElementTextContent(tramitacaoElement, "UrlDocumento", null));

        return procedure;
    }

    private String getElementTextContent(Element parentElement, String tagName, String defaultValue) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return defaultValue;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}
