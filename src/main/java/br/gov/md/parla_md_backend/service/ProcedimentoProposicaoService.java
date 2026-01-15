package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.config.WorkflowConfig;
import br.gov.md.parla_md_backend.domain.ProcedimentoProposicao;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IProcedimentoProposicaoRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcedimentoProposicaoService {

    private static final Logger logger = LoggerFactory.getLogger(ProcedimentoProposicaoService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    private final ApiClient apiClient;
    private final IProcedimentoProposicaoRepository procedimentoRepository;
    private final RabbitMQProducer rabbitMQProducer;

    public ProcedimentoProposicaoService(ApiClient apiClient,
                                         IProcedimentoProposicaoRepository procedimentoRepository,
                                         RabbitMQProducer rabbitMQProducer) {
        this.apiClient = apiClient;
        this.procedimentoRepository = procedimentoRepository;
        this.rabbitMQProducer = rabbitMQProducer;
    }


    @Transactional
    public List<ProcedimentoProposicao> buscarESalvarTramitacoes(Proposicao proposicao) {
        logger.info("Iniciando busca de tramitações para proposição: {}", proposicao.getId());

        String dadosJson = buscarDadosTramitacaoJson(String.valueOf(proposicao.getIdCamara()));
        List<ProcedimentoProposicao> tramitacoes = converterJsonParaTramitacoes(dadosJson, proposicao);

        return salvarTramitacoes(tramitacoes);
    }

    private String buscarDadosTramitacaoJson(String idProposicao) {

        String endpoint = camaraApiBaseUrl + "proposicoes/" + idProposicao + "/tramitacoes";
        return apiClient.get(endpoint);
    }

    private List<ProcedimentoProposicao> converterJsonParaTramitacoes(String dadosJson, Proposicao proposicao) {
        List<ProcedimentoProposicao> tramitacoes = new ArrayList<>();
        JSONObject json = new JSONObject(dadosJson);

        if (!json.has("dados")) {
            return tramitacoes;
        }

        JSONArray dados = json.getJSONArray("dados");

        for (int i = 0; i < dados.length(); i++) {
            JSONObject dadosTramitacao = dados.getJSONObject(i);
            ProcedimentoProposicao tramitacao = criarTramitacaoDeJson(dadosTramitacao, proposicao);
            tramitacoes.add(tramitacao);
        }

        return tramitacoes;
    }

    private ProcedimentoProposicao criarTramitacaoDeJson(JSONObject dadosTramitacao, Proposicao proposicao) {
        ProcedimentoProposicao tramitacao = new ProcedimentoProposicao();
        tramitacao.setProposicao(proposicao);
        tramitacao.setSequencia(dadosTramitacao.getInt("sequencia"));
        tramitacao.setDataHora(LocalDateTime.parse(dadosTramitacao.getString("dataHora"), DATE_TIME_FORMATTER));
        tramitacao.setSiglaOrgao(dadosTramitacao.getString("siglaOrgao"));
        tramitacao.setUriOrgao(dadosTramitacao.getString("uriOrgao"));
        tramitacao.setDescricaoTramitacao(dadosTramitacao.getString("descricaoTramitacao"));
        tramitacao.setDespacho(dadosTramitacao.optString("despacho", null));
        tramitacao.setRegime(dadosTramitacao.optString("regime", null));
        tramitacao.setIdTipoTramitacao(dadosTramitacao.optString("idTipoTramitacao", null));
        tramitacao.setStatusProposicao(dadosTramitacao.optString("statusProposicao", null));
        tramitacao.setUriUltimoRelator(dadosTramitacao.optString("uriUltimoRelator", null));
        tramitacao.setUrlDocumento(dadosTramitacao.optString("urlDocumento", null));
        return tramitacao;
    }

    private List<ProcedimentoProposicao> salvarTramitacoes(List<ProcedimentoProposicao> tramitacoes) {
        if (tramitacoes.isEmpty()) {
            return tramitacoes;
        }

        List<ProcedimentoProposicao> tramitacoesSalvas = procedimentoRepository.saveAll(tramitacoes);
        tramitacoesSalvas.forEach(this::publicarTramitacao);

        logger.info("Salvas {} tramitações para a proposição {}", tramitacoesSalvas.size(),
                tramitacoesSalvas.get(0).getProposicao().getId());

        return tramitacoesSalvas;
    }

    private void publicarTramitacao(ProcedimentoProposicao tramitacao) {
        try {
            rabbitMQProducer.sendMessage(
                    WorkflowConfig.TRAMITACAO_EXCHANGE,
                    WorkflowConfig.TRAMITACAO_ROUTING_KEY,
                    tramitacao
            );
        } catch (Exception e) {
            logger.error("Erro ao publicar tramitação no RabbitMQ: {}", e.getMessage());
        }
    }
}