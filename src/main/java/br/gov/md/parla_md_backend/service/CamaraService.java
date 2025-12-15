package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.TemaComportamento;
import br.gov.md.parla_md_backend.domain.Parlamentar;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.legislativo.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IProcedimentoProposicaoRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class CamaraService {

    private static final Logger logger = LoggerFactory.getLogger(CamaraService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    private final ApiClient apiClient;
    private final IProposicaoRepository propositionRepository;
    private final IProcedimentoProposicaoRepository procedureRepository;
    private final IParlamentarRepository parlamentarianRepository;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    private List<String> searchTerms;

    @Autowired
    public CamaraService(ApiClient apiClient, IProposicaoRepository propositionRepository,
                         IProcedimentoProposicaoRepository procedureRepository, IParlamentarRepository parlamentarianRepository) {
        this.apiClient = apiClient;
        this.propositionRepository = propositionRepository;
        this.procedureRepository = procedureRepository;
        this.parlamentarianRepository = parlamentarianRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void scheduledFetchAndSavePropositions() {
        fetchAndSavePropositions();
    }

    public List<Proposicao> fetchAndSavePropositions() {
        String endpoint = camaraApiBaseUrl + "proposicoes?ordem=ASC&ordenarPor=id&itens=20";
        logger.info("Fetching data from endpoint: {}", endpoint);

        String jsonData = apiClient.get(endpoint);
        JSONObject json = new JSONObject(jsonData);
        JSONArray dados = json.getJSONArray("dados");

        logger.info("Received {} propositions from API", dados.length());

        List<Proposicao> fetchedProposicaos = new ArrayList<>();
        for (int i = 0; i < dados.length(); i++) {
            try {
                JSONObject propJson = dados.getJSONObject(i);
                Proposicao proposicao = parseProposition(propJson);
                fetchedProposicaos.add(proposicao);

                logger.info("Saving proposition: {}", proposicao);
                Proposicao savedProposicao = propositionRepository.save(proposicao);

                if (savedProposicao != null && savedProposicao.getId() != null) {
                    logger.info("Successfully saved proposition with ID: {}", savedProposicao.getId());
                } else {
                    logger.warn("Failed to save proposition: {}", proposicao);
                }
            } catch (Exception e) {
                logger.error("Error processing proposition at index {}: {}", i, e.getMessage(), e);
            }
        }

        logger.info("Total propositions processed: {}", fetchedProposicaos.size());
        return fetchedProposicaos;
    }

    private Proposicao parseProposition(JSONObject propJson) {
        Proposicao proposicao = new Proposicao();

        proposicao.setId(propJson.optString("id"));
        proposicao.setUri(propJson.optString("uri"));
        proposicao.setSiglaTipo(propJson.optString("siglaTipo"));
        proposicao.setCodTipo(propJson.optInt("codTipo", 0));
        proposicao.setNumero(propJson.optInt("numero", 0));
        proposicao.setAno(propJson.optInt("ano", 0));
        proposicao.setEmenta(propJson.optString("ementa"));

        String dataApresentacaoStr = propJson.optString("dataApresentacao");
        proposicao.setDataApresentacao(parseDateTime(dataApresentacaoStr));

        JSONObject statusProposicao = propJson.optJSONObject("statusProposicao");
        if (statusProposicao != null) {
            proposicao.setUriUltimoRelator(statusProposicao.optString("uriUltimoRelator"));
            proposicao.setDescricaoTramitacao(statusProposicao.optString("descricaoTramitacao"));
            proposicao.setIdTipoTramitacao(statusProposicao.optString("idTipoTramitacao"));
            proposicao.setDespacho(statusProposicao.optString("despacho"));

            String dataHoraStr = statusProposicao.optString("dataHora");
            proposicao.setDataHora(parseDateTime(dataHoraStr));

            proposicao.setSequencia(statusProposicao.optInt("sequencia", 0));
            proposicao.setSiglaOrgao(statusProposicao.optString("siglaOrgao"));
            proposicao.setUriOrgao(statusProposicao.optString("uriOrgao"));
            proposicao.setRegime(statusProposicao.optString("regime"));
            proposicao.setDescricaoSituacao(statusProposicao.optString("descricaoSituacao"));
            proposicao.setIdSituacao(statusProposicao.optInt("idSituacao", 0));
            proposicao.setApreciacao(statusProposicao.optString("apreciacao"));
            proposicao.setUrl(statusProposicao.optString("url"));
        }

        // Campos específicos da aplicação
        proposicao.setProbabilidadeAprovacao(0.0);
        proposicao.setTriagemStatus(StatusTriagem.NAO_AVALIADO);

        return proposicao;
    }

    private Proposicao mapApiToProposition(/* parâmetros */) {
        Proposicao proposicao = new Proposicao();
        // TODO Montar mapeamento dos campos
        proposicao.setTriagemStatus(StatusTriagem.NAO_AVALIADO);
        return proposicao;
    }

    private Proposicao mapJsonToProposition(JSONObject propJson) {
        Proposicao proposicao = new Proposicao();

        proposicao.setId(String.valueOf(propJson.getInt("id")));
        proposicao.setUri(propJson.optString("uri"));
        proposicao.setSiglaTipo(propJson.optString("siglaTipo"));
        proposicao.setCodTipo(propJson.optInt("codTipo", 0));
        proposicao.setNumero(propJson.optInt("numero", 0));
        proposicao.setAno(propJson.optInt("ano", 0));
        proposicao.setEmenta(propJson.optString("ementa", ""));

        JSONObject autorObject = propJson.optJSONObject("autor1");
        if (autorObject != null) {
            proposicao.setAutorId(autorObject.optString("id", ""));
            proposicao.setAutorNome(autorObject.optString("nome", ""));
            proposicao.setPartidoAutor(autorObject.optString("siglaPartido", ""));
        }

        proposicao.setTema(propJson.optString("tema", "Não categorizado"));

        String dataApresentacao = propJson.optString("dataApresentacao", null);
        if (dataApresentacao != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime dateTime = LocalDateTime.parse(dataApresentacao, formatter);
                proposicao.setDataApresentacao(dateTime);
            } catch (DateTimeParseException e) {
                logger.error("Error parsing date: {}", dataApresentacao, e);
            }
        }

        JSONObject statusObj = propJson.optJSONObject("statusProposicao");
        if (statusObj != null) {
            proposicao.setDescricaoSituacao(statusObj.optString("descricaoSituacao", "Status não disponível"));
            proposicao.setUriUltimoRelator(statusObj.optString("uriUltimoRelator"));
            proposicao.setDescricaoTramitacao(statusObj.optString("descricaoTramitacao"));
            proposicao.setIdTipoTramitacao(statusObj.optString("idTipoTramitacao"));
            proposicao.setDespacho(statusObj.optString("despacho"));

            String dataHora = statusObj.optString("dataHora", null);
            if (dataHora != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                    LocalDateTime dateTime = LocalDateTime.parse(dataHora, formatter);
                    proposicao.setDataHora(dateTime);
                } catch (DateTimeParseException e) {
                    logger.error("Error parsing date: {}", dataHora, e);
                }
            }

            proposicao.setSequencia(statusObj.optInt("sequencia", 0));
            proposicao.setSiglaOrgao(statusObj.optString("siglaOrgao"));
            proposicao.setUriOrgao(statusObj.optString("uriOrgao"));
            proposicao.setRegime(statusObj.optString("regime"));
            proposicao.setIdSituacao(statusObj.optInt("idSituacao", 0));
            proposicao.setApreciacao(statusObj.optString("apreciacao"));
            proposicao.setUrl(statusObj.optString("url"));
        }

        // Assumindo que a aprovação não está diretamente disponível na API
        proposicao.setAprovada(false);

        // Calcular e definir a probabilidade de aprovação
        double approvalProbability = calculateApprovalProbability(proposicao);
        proposicao.setProbabilidadeAprovacao(approvalProbability);

        // Definir valores padrão para campos específicos da sua aplicação
        proposicao.setTriagemStatus(StatusTriagem.NAO_AVALIADO);
        proposicao.setStatusTramitacao(StatusTramitacao.EM_ANDAMENTO);

        return proposicao;
    }


    // Melhorar a lógica para calcular a probabilidade de aprovação
    // Implementação extremamente simplificada
    private double calculateApprovalProbability(Proposicao proposicao) {

        double baseProbability = 0.5; // 50% de chance base

        // Ajustar com base no partido do autor (exemplo simplificado)
        if ("PARTIDO_MAJORITARIO".equals(proposicao.getPartidoAutor())) {
            baseProbability += 0.1;
        }

        // Ajustar com base no tempo desde a apresentação
        long daysSincePresentation = ChronoUnit.DAYS.between(proposicao.getDataApresentacao(), LocalDateTime.now());
        if (daysSincePresentation > 365) {
            baseProbability -= 0.1; // Diminui a chance se estiver pendente por mais de um ano
        }

        // Limitar a probabilidade entre 0 e 1
        return Math.max(0, Math.min(1, baseProbability));
    }

    public List<Proposicao> getAllPropositions() {
        return propositionRepository.findAll();
    }

    public Optional<Proposicao> getPropositionById(String id) {
        return propositionRepository.findById(id);
    }

    public List<Proposicao> getPropositionsByAutor(String autor) {
        return propositionRepository.findByAutorId(autor);
    }

    @Transactional
    public List<ProcedimentoProposicao> fetchAndSaveProcedures(int propositionId) {
        String endpoint = camaraApiBaseUrl + "proposicoes/" + propositionId + "/tramitacoes";
        String jsonData = apiClient.get(endpoint);
        JSONObject json = new JSONObject(jsonData);
        JSONArray dados = json.getJSONArray("dados");

        List<ProcedimentoProposicao> procedimentoProposicaos = new ArrayList<>();
        for (int i = 0; i < dados.length(); i++) {
            JSONObject tramitacaoJson = dados.getJSONObject(i);
            ProcedimentoProposicao procedimentoProposicao = mapJsonToProcedure(tramitacaoJson);
            procedimentoProposicao.setPropositionId(String.valueOf(propositionId));

            // Buscar a Proposition correspondente e definir a referência
            Proposicao proposicao = propositionRepository.findById(String.valueOf(propositionId)).orElse(null);
            procedimentoProposicao.setProposicao(proposicao);

            procedimentoProposicaos.add(procedimentoProposicao);
        }

        // Salvar os procedimentos
        return procedureRepository.saveAll(procedimentoProposicaos);
    }

    private ProcedimentoProposicao mapJsonToProcedure(JSONObject json) {
        ProcedimentoProposicao procedimentoProposicao = new ProcedimentoProposicao();

        String dataHoraString = json.getString("dataHora");
        LocalDateTime dataHora = LocalDateTime.parse(dataHoraString, DateTimeFormatter.ISO_DATE_TIME);
        procedimentoProposicao.setDataHora(dataHora);

        procedimentoProposicao.setSequencia(json.getInt("sequencia"));
        procedimentoProposicao.setSiglaOrgao(json.optString("siglaOrgao", ""));
        procedimentoProposicao.setDescricaoTramitacao(json.optString("descricaoTramitacao", ""));
        procedimentoProposicao.setDespacho(json.optString("despacho", ""));
        return procedimentoProposicao;
    }

    public Optional<ProcedimentoProposicao> getProcedureById(String id) {
        return procedureRepository.findById(id);
    }

    @Transactional
    public void registrarComportamento(String parlamentarId, String tema, boolean votoAFavor) {
        Parlamentar parlamentar = parlamentarianRepository.findById(parlamentarId)
                .orElseThrow(() -> new RuntimeException("Parlamentar não encontrado com ID: " + parlamentarId));

        Map<String, TemaComportamento> behaviors = parlamentar.getComportamentos();
        TemaComportamento comportamento = behaviors.computeIfAbsent(tema, k -> new TemaComportamento());

        if (votoAFavor) {
            comportamento.incrementVotosAFavor();
        } else {
            comportamento.incrementVotosContra();
        }

        parlamentarianRepository.save(parlamentar);
    }

    public double analisarComportamento(String parlamentarId, String tema) {
        Parlamentar parlamentar = parlamentarianRepository.findById(parlamentarId)
                .orElseThrow(() -> new RuntimeException("Parlamentar não encontrado com ID: " + parlamentarId));

        TemaComportamento comportamento = parlamentar.getComportamento(tema);
        if (comportamento == null) {
            return -1; // Indica que não há dados suficientes
        }

        return comportamento.calcularIndiceApoio();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            return LocalDateTime.parse(dateTimeStr, formatter);
        }
        return null;
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public List<Parlamentar> fetchDeputados() {
        return parlamentarianRepository.findAll();
    }

    public List<String> loadSpecificTerms(String specificTermsFile) throws IOException {
        try {
            Resource resource = resourceLoader.getResource(specificTermsFile);
            searchTerms = Files.readAllLines(resource.getFile().toPath());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                searchTerms = reader.lines().collect(Collectors.toList());
            }
            logger.info("Rodando {} termos específicos", searchTerms.size());
            return searchTerms;
        } catch (IOException e) {

            System.err.println("Erro no loading de termos para pesquisa: " + e.getMessage());
            searchTerms = new ArrayList<>();
        }
        return new ArrayList<>();
    }
}