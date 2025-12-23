package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IVotacaoRepository;
import br.gov.md.parla_md_backend.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VotoParlamentarService {

    private static final String VOTING_EXCHANGE = "voting.exchange";
    private static final String VOTING_ROUTING_KEY = "voting.new";
    private static final String VOTE_ROUTING_KEY = "vote.new";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${camara.api.base-url}")
    private String camaraApiBaseUrl;

    private final ApiClient apiClient;
    private final IParlamentarRepository parlamentarRepository;
    private final IVotacaoRepository votacaoRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final MongoTemplate mongoTemplate;

    private final ProposicaoService proposicaoService;
    private final SenadoService senadoService;
    private final CamaraService camaraService;


    public VotoParlamentarService(ApiClient apiClient,
                                  IParlamentarRepository parlamentarRepository,
                                  IVotacaoRepository votacaoRepository,
                                  RabbitMQProducer rabbitMQProducer,
                                  MongoTemplate mongoTemplate,
                                  ProposicaoService proposicaoService,
                                  SenadoService senadoService,
                                  CamaraService camaraService) {
        this.apiClient = apiClient;
        this.parlamentarRepository = parlamentarRepository;
        this.votacaoRepository = votacaoRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.mongoTemplate = mongoTemplate;
        this.proposicaoService = proposicaoService;
        this.senadoService = senadoService;
        this.camaraService = camaraService;
    }

    public List<Votacao> buscarVotacoesEntreDatas(LocalDate startDate, LocalDate endDate) {
        String endpoint = buildVotacoesEndpoint(startDate, endDate);
        JSONArray votacoesData = buscaDadosParaApi(endpoint);
        return analisarEPublicarVotacoes(votacoesData);
    }

    public List<Voto> buscarVotosPorVotacao(String votacaoId) {
        String endpoint = constroiEndpointVotos(votacaoId);
        JSONArray votosData = buscaDadosParaApi(endpoint);
        Votacao votacao = buscaVotacaoPorId(votacaoId);
        return analisarEPublicarVotos(votosData, votacao);
    }

    public List<Voto> fetchVotosForDeputado(String deputadoId, LocalDate startDate, LocalDate endDate) {
        String endpoint = constroiEndpointVotosDeputadoDeputado(deputadoId, startDate, endDate);
        JSONArray votosData = buscaDadosParaApi(endpoint);
        Parlamentar parlamentar = buscaParlamentarPorId(deputadoId);
        return analisarEPublicarVotosDeputado(votosData, parlamentar);
    }

    @RabbitListener(queues = "voting.queue")
    public void processoVotacao(Votacao votacao) {
        salvarVotacao(votacao);
        buscarVotosPorVotacao(votacao.getId());
    }

    @RabbitListener(queues = "vote.queue")
    public void processoVoto(Voto voto) {
        salvarVoto(voto);
    }

    private String buildVotacoesEndpoint(LocalDate startDate, LocalDate endDate) {
        return camaraApiBaseUrl + "votacoes?dataInicio=" + startDate.format(DATE_FORMATTER)
                + "&dataFim=" + endDate.format(DATE_FORMATTER);
    }

    private String constroiEndpointVotos(String votacaoId) {
        return camaraApiBaseUrl + "votacoes/" + votacaoId + "/votos";
    }

    private String constroiEndpointVotosDeputadoDeputado(String deputadoId, LocalDate startDate, LocalDate endDate) {
        return camaraApiBaseUrl + "deputados/" + deputadoId + "/votacoes?dataInicio="
                + startDate.format(DATE_FORMATTER) + "&dataFim=" + endDate.format(DATE_FORMATTER);
    }

    private JSONArray buscaDadosParaApi(String endpoint) {
        String jsonData = apiClient.get(endpoint);
        return new JSONObject(jsonData).getJSONArray("dados");
    }

    private List<Votacao> analisarEPublicarVotacoes(JSONArray votacoesData) {
        List<Votacao> votacoes = new ArrayList<>();
        for (int i = 0; i < votacoesData.length(); i++) {
            Votacao votacao = analisarVotacao(votacoesData.getJSONObject(i));
            votacoes.add(votacao);
            publicarVotacao(votacao);
        }
        return votacoes;
    }

    private List<Voto> analisarEPublicarVotos(JSONArray votosData, Votacao votacao) {
        List<Voto> votos = new ArrayList<>();
        for (int i = 0; i < votosData.length(); i++) {
            Voto voto = analisarVoto(votosData.getJSONObject(i), votacao);
            votos.add(voto);
            publicarVoto(voto);
        }
        return votos;
    }

    private List<Voto> analisarEPublicarVotosDeputado(JSONArray votosData, Parlamentar parlamentar) {
        List<Voto> votos = new ArrayList<>();
        for (int i = 0; i < votosData.length(); i++) {
            JSONObject votoData = votosData.getJSONObject(i);
            Votacao votacao = buscarOrCriarVotacao(votoData);
            Voto voto = criarVotoDados(votoData, votacao, parlamentar);
            votos.add(voto);
            publicarVoto(voto);
        }
        return votos;
    }

    private Votacao analisarVotacao(JSONObject votacaoData) {
        Votacao votacao = new Votacao();
        votacao.setId(votacaoData.getString("id"));
        votacao.setDataHoraInicio(analisarDateTime(votacaoData.getString("dataHoraInicio")));
        votacao.setDataHoraFim(analisarDateTime(votacaoData.getString("dataHoraFim")));
        votacao.setSiglaOrgao(votacaoData.getString("siglaOrgao"));
        votacao.setUriProposicaoPrincipal(votacaoData.optString("uriProposicaoPrincipal", null));
        votacao.setDescricao(votacaoData.optString("descricao", null));
        votacao.setParlamentarId(votacaoData.optString("parlamentarId", null));
        votacao.setProposicaoId(votacaoData.optString("proposicaoId", null));
        votacao.setMateriaId(votacaoData.optString("matterId", null));
        votacao.setVoto(votacaoData.optString("voto", null));
        if (votacaoData.has("dataHoraVoto")) {
            votacao.setVotoData(analisarDateTime(votacaoData.getString("dataHoraVoto")));
        }

        String proposicaoId = votacaoData.optString("uriProposicaoPrincipal", null);
        if (proposicaoId != null) {
            Proposicao proposicao = proposicaoService.buscarProposicaoPorId(proposicaoId);
            votacao.setProposicaoId(proposicao.getId());
        } else {
            // If it's not a proposition, it might be a senate matter
            // You may need to adjust this logic based on how you identify senate matters
            String matterCode = votacaoData.optString("codigoMateria", null);
            if (matterCode != null) {
                Materia materia = senadoService.findMatterById(matterCode);
                votacao.setMateriaId(materia.getCodigo());
            }
        }

        return votacao;
    }

    private Voto analisarVoto(JSONObject votoData, Votacao votacao) {
        JSONObject deputadoData = votoData.getJSONObject("deputado");
        Parlamentar parlamentar = buscarOuCriarParlamentar(deputadoData);
        return createVoto(votacao, parlamentar, votoData.getString("voto"));
    }

    private Parlamentar buscarOuCriarParlamentar(JSONObject deputadoData) {
        String deputadoId = deputadoData.getString("id");
        return parlamentarRepository.findById(deputadoId)
                .orElseGet(() -> criarSalvarParlamentar(deputadoData));
    }

    private Parlamentar criarSalvarParlamentar(JSONObject parlamentarData) {
        Parlamentar newParlamentar = new Parlamentar(
                parlamentarData.getString("id"),
                parlamentarData.getString("nome"),
                parlamentarData.getString("siglaPartido"),
                parlamentarData.getString("siglaUf")
        );
        return parlamentarRepository.save(newParlamentar);
    }

    private Voto createVoto(Votacao votacao, Parlamentar parlamentar, String votoValor) {
        return new Voto(UUID.randomUUID().toString(), votacao, parlamentar, votoValor);
    }

    private Votacao buscarOrCriarVotacao(JSONObject votoData) {
        String votacaoId = votoData.getString("idVotacao");
        return votacaoRepository.findById(votacaoId)
                .orElseGet(() -> criarESalvarVotacao(votoData));
    }

    private Votacao criarESalvarVotacao(JSONObject votoData) {
        Votacao newVotacao = new Votacao();
        newVotacao.setId(votoData.getString("idVotacao"));
        newVotacao.setDataHoraInicio(analisarDateTime(votoData.getString("dataHoraVotacao")));
        return votacaoRepository.save(newVotacao);
    }

    private Voto criarVotoDados(JSONObject votoData, Votacao votacao, Parlamentar parlamentar) {
        return new Voto(
                UUID.randomUUID().toString(),
                votacao,
                parlamentar,
                votoData.getString("voto")
        );
    }

    private LocalDateTime analisarDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    }

    private void publicarVotacao(Votacao votacao) {
        rabbitMQProducer.sendMessage(VOTING_EXCHANGE, VOTING_ROUTING_KEY, votacao);
    }

    private void publicarVoto(Voto voto) {
        rabbitMQProducer.sendMessage(VOTING_EXCHANGE, VOTE_ROUTING_KEY, voto);
    }

    private Votacao buscaVotacaoPorId(String votacaoId) {
        return votacaoRepository.findById(votacaoId)
                .orElseThrow(() -> new RuntimeException("Votação não encontrada: " + votacaoId));
    }

    private Parlamentar buscaParlamentarPorId(String parlamentarId) {
        return parlamentarRepository.findById(parlamentarId)
                .orElseThrow(() -> new RuntimeException("Deputado não encontrado: " + parlamentarId));
    }

    private void salvarVotacao(Votacao votacao) {
        mongoTemplate.save(votacao);
    }

    private void salvarVoto(Voto voto) {
        mongoTemplate.save(voto);
    }
}