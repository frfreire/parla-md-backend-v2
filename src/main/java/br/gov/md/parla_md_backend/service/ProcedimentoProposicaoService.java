package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.old.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.repository.IProcedimentoProposicaoRepository;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.service.interfaces.IProcedimentoStrategy;
import br.gov.md.parla_md_backend.util.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcedimentoProposicaoService implements IProcedimentoStrategy<Proposicao> {

    private static final String PROCEDURE_EXCHANGE = "procedure.exchange";
    private static final String PROCEDURE_ROUTING_KEY = "procedure.new";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    private final ApiClient apiClient;
    private final IProcedimentoProposicaoRepository procedureRepository;
    private final RabbitMQProducer rabbitMQProducer;

    public ProcedimentoProposicaoService(ApiClient apiClient,
                                         IProcedimentoProposicaoRepository procedureRepository,
                                         RabbitMQProducer rabbitMQProducer) {
        this.apiClient = apiClient;
        this.procedureRepository = procedureRepository;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public List<ProcedimentoProposicao> fetchAndSaveProcedures(Proposicao proposicao) {
        String jsonData = fetchProceduresJsonData(proposicao.getId());
        List<ProcedimentoProposicao> procedures = parseProceduresFromJson(jsonData, proposicao);
        return saveProcedures(procedures);
    }

    private String fetchProceduresJsonData(String propositionId) {
        String endpoint = camaraApiBaseUrl + "proposicoes/" + propositionId + "/tramitacoes";
        return apiClient.get(endpoint);
    }

    private List<ProcedimentoProposicao> parseProceduresFromJson(String jsonData, Proposicao proposicao) {
        List<ProcedimentoProposicao> procedures = new ArrayList<>();
        JSONObject json = new JSONObject(jsonData);
        JSONArray dados = json.getJSONArray("dados");

        for (int i = 0; i < dados.length(); i++) {
            JSONObject procedureData = dados.getJSONObject(i);
            ProcedimentoProposicao procedure = createProcedureFromJson(procedureData, proposicao);
            procedures.add(procedure);
        }

        return procedures;
    }

    private ProcedimentoProposicao createProcedureFromJson(JSONObject procedureData, Proposicao proposicao) {
        ProcedimentoProposicao procedure = new ProcedimentoProposicao();
        procedure.setProposicao(proposicao);
        procedure.setSequencia(procedureData.getInt("sequencia"));
        procedure.setDataHora(LocalDateTime.parse(procedureData.getString("dataHora"), DATE_TIME_FORMATTER));
        procedure.setSiglaOrgao(procedureData.getString("siglaOrgao"));
        procedure.setUriOrgao(procedureData.getString("uriOrgao"));
        procedure.setDescricaoTramitacao(procedureData.getString("descricaoTramitacao"));
        procedure.setDespacho(procedureData.optString("despacho", null));
        procedure.setRegime(procedureData.optString("regime", null));
        procedure.setIdTipoTramitacao(procedureData.optString("idTipoTramitacao", null));
        procedure.setStatusProposicao(procedureData.optString("statusProposicao", null));
        procedure.setUriUltimoRelator(procedureData.optString("uriUltimoRelator", null));
        procedure.setUrlDocumento(procedureData.optString("urlDocumento", null));
        return procedure;
    }

    private List<ProcedimentoProposicao> saveProcedures(List<ProcedimentoProposicao> procedures) {
        List<ProcedimentoProposicao> savedProcedures = procedureRepository.saveAll(procedures);
        savedProcedures.forEach(this::publishProcedure);
        return savedProcedures;
    }

    private void publishProcedure(ProcedimentoProposicao procedure) {
        rabbitMQProducer.sendMessage(PROCEDURE_EXCHANGE, PROCEDURE_ROUTING_KEY, procedure);
    }

    @Override
    public void buscarESalvarProcedimentos(Proposicao projeto) {

    }

    @Override
    public String getTipoProcedimento() {
        return "";
    }

    @Override
    public boolean podeProcessar(Object projeto) {
        return false;
    }
}
