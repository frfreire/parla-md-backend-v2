package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.config.WorkflowConfig;
import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IParlamentarRepository;
import br.gov.md.parla_md_backend.repository.IVotacaoRepository;
import br.gov.md.parla_md_backend.repository.IVotoRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VotoParlamentarService {

    private static final Logger log = LoggerFactory.getLogger(VotoParlamentarService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final IVotacaoRepository votacaoRepository;
    IVotoRepository votoRepository;
    private final IParlamentarRepository parlamentarRepository;
    private final RabbitMQProducer rabbitMQProducer;

    public VotoParlamentarService(IVotacaoRepository votacaoRepository,
                                  IParlamentarRepository parlamentarRepository,
                                  RabbitMQProducer rabbitMQProducer, IVotoRepository votoRepository) {
        this.votacaoRepository = votacaoRepository;
        this.parlamentarRepository = parlamentarRepository;
        this.rabbitMQProducer = rabbitMQProducer;
        this.votoRepository = votoRepository;
    }

    /**
     * Processa mensagem de votação recebida da fila.
     * Espera um JSON contendo dados da votação e lista de votos.
     */
    @RabbitListener(queues = WorkflowConfig.VOTACAO_QUEUE)
    @Transactional
    public void processarVotacao(String mensagem) {
        try {
            log.info("Recebida mensagem de votação para processamento.");
            JSONObject json = new JSONObject(mensagem);

            // Cria e salva a votação
            Votacao votacao = criarDadosVotacao(json);

            // Notifica que a votação foi salva (Evento de Saída)
            publicarVotacaoConcluida(votacao);

            // Processa os votos individuais
            if (json.has("votos")) {
                JSONArray votos = json.getJSONArray("votos");
                for (int i = 0; i < votos.length(); i++) {
                    processarVoto(votos.getJSONObject(i), votacao);
                }
            }

            log.info("Processamento de votação concluído com sucesso. ID: {}", votacao.getId());

        } catch (Exception e) {
            log.error("Erro ao processar votação: {}", e.getMessage(), e);
            // Em produção, considerar lançar exceção para Dead Letter Queue (DLQ)
        }
    }

    private void processarVoto(JSONObject dadosVoto, Votacao votacao) {
        try {
            String idParlamentar = String.valueOf(dadosVoto.getLong("idDeputado"));
            Parlamentar parlamentar = buscaParlamentarPorId(idParlamentar);

            Voto voto = criarDadosVoto(dadosVoto, votacao, parlamentar);

            votoRepository.save(voto);

            // Notifica registro do voto (Evento de Saída)
            publicarVotoRegistrado(voto);

        } catch (Exception e) {
            log.warn("Falha ao processar voto individual: {}", e.getMessage());
        }
    }

    private Votacao criarDadosVotacao(JSONObject dadosVotacao) {
        Votacao novaVotacao = new Votacao();
        novaVotacao.setId(dadosVotacao.getString("idVotacao"));
        //TODO completar os demais campos da votação
        // novaVotacao.setProposicao(...);
        novaVotacao.setDataHoraInicio(analisarDataHora(dadosVotacao.getString("dataHoraVotacao")));

        return votacaoRepository.save(novaVotacao);
    }

    private Voto criarDadosVoto(JSONObject dadosVoto, Votacao votacao, Parlamentar parlamentar) {
        return new Voto(
                UUID.randomUUID().toString(),
                votacao,
                parlamentar,
                dadosVoto.getString("voto")
        );
    }

    private LocalDateTime analisarDataHora(String dataHoraString) {
        return LocalDateTime.parse(dataHoraString, DATE_TIME_FORMATTER);
    }

    /**
     * Publica evento de domínio: Votação Concluída.
     * Usa routing key específica para evitar loop com a fila de entrada.
     */
    private void publicarVotacaoConcluida(Votacao votacao) {
        rabbitMQProducer.sendMessage(
                WorkflowConfig.VOTACAO_EXCHANGE,
                WorkflowConfig.VOTACAO_CONCLUIDA_ROUTING_KEY,
                votacao
        );
    }

    /**
     * Publica evento de domínio: Voto Registrado.
     */
    private void publicarVotoRegistrado(Voto voto) {
        rabbitMQProducer.sendMessage(
                WorkflowConfig.VOTACAO_EXCHANGE,
                WorkflowConfig.VOTO_REGISTRADO_ROUTING_KEY,
                voto
        );
    }

    private Parlamentar buscaParlamentarPorId(String idParlamentar) {
        return parlamentarRepository.findById(idParlamentar)
                .orElseThrow(() -> new RuntimeException("Parlamentar não encontrado: " + idParlamentar));
    }
}