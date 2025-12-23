package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.repository.IMateriaRepository;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IProcedimentoProposicaoRepository;
import br.gov.md.parla_md_backend.repository.ISenadoRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
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

@Service
public class SenadoService {

    private static final String MATTER_EXCHANGE = "matter.exchange";
    private static final String MATTER_ROUTING_KEY = "matter.new";
    private static final String PROCEDURE_EXCHANGE = "procedure.exchange";
    private static final String PROCEDURE_ROUTING_KEY = "procedure.new";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${senado.api.base-url}")
    private String senadoApiBaseUrl;

    @Value("${senado.api.default-year:#{T(java.time.Year).now().getValue()}}")
    private int defaultYear;

    @Value("${senado.api.default-items:100}")
    private int defaultItems;

    private final ApiClient apiClient;
    private final IMateriaRepository matterRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final IProcedimentoProposicaoRepository procedureRepository;
    private LocalDateTime lastSuccessfulUpdate;
    private final ISenadoRepository senateRepository;

    public SenadoService(ApiClient apiClient, IMateriaRepository matterRepository,
                         RabbitMQProducer rabbitMQProducer, ISenadoRepository senateRepository,
                         IProcedimentoProposicaoRepository procedureRepository) {
        this.apiClient = apiClient;
        this.matterRepository = matterRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.procedureRepository = procedureRepository;
        this.senateRepository = senateRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Runs at 2 AM every day
    public void scheduledFetchAndSaveMatters() {
        List<Materia> materias = fetchAndSaveMatters(defaultYear, defaultItems);
        materias.forEach(this::publishMatter);
    }

    public List<Materia> fetchAndSaveMatters(int year, int items) {
        String xmlData = fetchMattersXmlData(year, items);
        List<Materia> materias = parseMattersFromXml(xmlData);
        return saveMatters(materias);
    }

    public List<Materia> fetchAndSaveMatters() {
        String xmlData = fetchMattersXmlData();
        List<Materia> materias = parseMattersFromXml(xmlData);
        return saveMatters(materias);
    }

    public List<ProcedimentoProposicao> fetchProcedures(int matterId) {
        String xmlData = fetchProceduresXmlData(matterId);
        List<ProcedimentoProposicao> procedimentoProposicaos = parseProceduresFromXml(xmlData);
        procedimentoProposicaos.forEach(this::publishProcedure);
        return procedimentoProposicaos;
    }

    private String fetchMattersXmlData(int year, int items) {
        String endpoint = String.format("%smateria/pesquisa/lista?ano=%d&itens=%d",
                senadoApiBaseUrl, year, items);
        return apiClient.get(endpoint);
    }

    public String fetchMattersXmlData() {
        return fetchMattersXmlData(defaultYear, defaultItems);
    }

    private List<Materia> parseMattersFromXml(String xmlData) {
        List<Materia> materias = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            NodeList matterNodes = doc.getElementsByTagName("Materia");
            for (int i = 0; i < matterNodes.getLength(); i++) {
                Element matterElement = (Element) matterNodes.item(i);
                Materia materia = createMatterFromElement(matterElement);
                materias.add(materia);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing matters XML data", e);
        }
        return materias;
    }

    private Materia createMatterFromElement(Element matterElement) {
        Materia materia = new Materia();
        materia.setCodigo(getElementTextContent(matterElement, "CodigoMateria"));
        materia.setSigla(getElementTextContent(matterElement, "SiglaSubtipoMateria"));
        materia.setNumero(Integer.parseInt(getElementTextContent(matterElement, "NumeroMateria")));
        materia.setAno(Integer.parseInt(getElementTextContent(matterElement, "AnoMateria")));
        materia.setEmenta(getElementTextContent(matterElement, "EmentaMateria"));
        materia.setAutor(getElementTextContent(matterElement, "NomeAutor"));
        return materia;
    }

    private List<Materia> saveMatters(List<Materia> materias) {
        return matterRepository.saveAll(materias);
    }

    private void publishMatter(Materia materia) {
        rabbitMQProducer.sendMessage(MATTER_EXCHANGE, MATTER_ROUTING_KEY, materia);
    }

    private String fetchProceduresXmlData(int matterId) {
        String endpoint = senadoApiBaseUrl + "materia/" + matterId + "/tramitacoes";
        return apiClient.get(endpoint);
    }

    private List<ProcedimentoProposicao> parseProceduresFromXml(String xmlData) {
        List<ProcedimentoProposicao> procedimentoProposicaos = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            NodeList procedureNodes = doc.getElementsByTagName("Tramitacao");
            for (int i = 0; i < procedureNodes.getLength(); i++) {
                Element procedureElement = (Element) procedureNodes.item(i);
                ProcedimentoProposicao procedimentoProposicao = createProcedureFromElement(procedureElement);
                procedimentoProposicaos.add(procedimentoProposicao);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing procedures XML data", e);
        }
        return procedimentoProposicaos;
    }

    private ProcedimentoProposicao createProcedureFromElement(Element element) {
        ProcedimentoProposicao procedimentoProposicao = new ProcedimentoProposicao();
        String dataHoraString = getElementTextContent(element, "DataHora");
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraString, DATE_TIME_FORMATTER);
        procedimentoProposicao.setDataHora(dataHora);
        // TODO adicionar novos campos depois
        return procedimentoProposicao;
    }

    private void publishProcedure(ProcedimentoProposicao procedimentoProposicao) {
        rabbitMQProducer.sendMessage(PROCEDURE_EXCHANGE, PROCEDURE_ROUTING_KEY, procedimentoProposicao);
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    // Additional methods for CRUD operations

    public Materia findMatterById(String id) {
        return matterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matter not found: " + id));
    }

    public List<Materia> findMattersByYear(int year) {
        return matterRepository.findByAno(year);
    }

    public Materia updateMatter(Materia materia) {
        if (!matterRepository.existsById(materia.getCodigo())) {
            throw new RuntimeException("Cannot update non-existent matter");
        }
        return matterRepository.save(materia);
    }

    public void deleteMatter(String id) {
        matterRepository.deleteById(id);
    }

    public int getDefaultYear() {
        return defaultYear;
    }

    public int getDefaultItems() {
        return defaultItems;
    }

    public Materia saveAndFetchProcedures(Materia materia) {
        Materia savedMateria = matterRepository.save(materia);
        publishMatter(savedMateria);

        List<ProcedimentoProposicao> procedures = fetchProcedures(Integer.parseInt(savedMateria.getCodigo()));

        saveProcedures(procedures);

        return savedMateria;
    }

    private void saveProcedures(List<ProcedimentoProposicao> procedures) {

        procedureRepository.saveAll(procedures);
        procedures.forEach(this::publishProcedure);
    }

    public List<Materia> fetchAllMatters() {
        return matterRepository.findAll();
    }

    public String getLastUpdateTime() {
        return lastSuccessfulUpdate != null
                ? lastSuccessfulUpdate.toString()
                : "Nenhuma atualização realizada ainda";
    }

    public void updateLastSuccessfulUpdate() {
        this.lastSuccessfulUpdate = LocalDateTime.now();
    }

    public List<Parlamentar> fetchSenadores(){
        return senateRepository.findAll();
    }
}