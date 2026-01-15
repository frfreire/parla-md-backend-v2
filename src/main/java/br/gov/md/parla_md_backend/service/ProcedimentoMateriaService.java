package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.config.WorkflowConfig;
import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.ProcedimentoMateria;
import br.gov.md.parla_md_backend.exception.ApiExternaException;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IMateriaRepository;
import br.gov.md.parla_md_backend.repository.IProcedimentoMateriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcedimentoMateriaService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${senado.api.base-url:https://legis.senado.leg.br/dadosabertos/}")
    private String senadoApiBaseUrl;

    private final IMateriaRepository materiaRepository;
    private final IProcedimentoMateriaRepository procedimentoRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final SenadoService senadoService;

    /**
     * Atualização agendada de todas as matérias.
     * Executa diariamente às 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void atualizarTodasTramitacoesAgendadas() {
        log.info("Iniciando atualização agendada de procedimentos de matérias às {}",
                LocalDateTime.now());

        AtomicInteger totalMaterias = new AtomicInteger(0);
        AtomicInteger atualizadas = new AtomicInteger(0);
        AtomicInteger erros = new AtomicInteger(0);

        try {
            List<Materia> todasMaterias = materiaRepository.findAll();
            totalMaterias.set(todasMaterias.size());

            log.info("Total de matérias a atualizar: {}", totalMaterias.get());

            todasMaterias.forEach(materia -> {
                try {
                    if (materia.getCodigoMateria() != null) {
                        List<ProcedimentoMateria> procedimentos =
                                buscarESalvarTramitacoes(materia.getCodigoMateria());

                        log.debug("Atualizados {} procedimentos para a matéria {}",
                                procedimentos.size(), materia.getCodigoMateria());

                        atualizadas.incrementAndGet();

                        if (!procedimentos.isEmpty()) {
                            ProcedimentoMateria ultimo = procedimentos.get(procedimentos.size() - 1);

                            materia.setSituacaoAtual(ultimo.getSituacaoDescricao());
                            materia.setDataUltimaAtualizacao(LocalDateTime.now());
                            materiaRepository.save(materia);
                        }

                        rabbitMQProducer.sendMessage(
                                WorkflowConfig.ATUALIZACAO_API_EXCHANGE,
                                WorkflowConfig.ATUALIZACAO_API_ROUTING_KEY,
                                materia
                        );
                    }

                } catch (Exception e) {
                    log.error("Erro ao atualizar procedimentos para a matéria {}: {}",
                            materia.getCodigoMateria(), e.getMessage());
                    erros.incrementAndGet();
                }
            });

        } catch (Exception e) {
            log.error("Erro ao buscar matérias: {}", e.getMessage(), e);
        } finally {
            log.info("Atualização concluída - Total: {}, Atualizadas: {}, Erros: {}",
                    totalMaterias.get(), atualizadas.get(), erros.get());
        }
    }

    /**
     * Processa mensagens de solicitação de atualização vindas da fila.
     * Escuta a fila definida em WorkflowConfig.ATUALIZACAO_API_QUEUE.
     */
    @RabbitListener(queues = WorkflowConfig.ATUALIZACAO_API_QUEUE)
    @Transactional
    public void processarMensagemAtualizacao(String mensagem) {
        try {

            String codigoStr = mensagem.replaceAll("[^0-9]", "");

            if (codigoStr.isEmpty()) {
                log.warn("Mensagem vazia ou inválida recebida na fila de atualização: {}", mensagem);
                return;
            }

            Long codigoMateria = Long.parseLong(codigoStr);

            materiaRepository.findByCodigoMateria(codigoMateria)
                    .ifPresent(materia -> buscarESalvarTramitacoes(codigoMateria));

        } catch (Exception e) {
            log.error("Erro ao processar mensagem da fila de atualização: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public List<ProcedimentoMateria> buscarESalvarTramitacoes(Long codigoMateria) {
        try {
            log.debug("Buscando procedimentos para matéria: {}", codigoMateria);

            List<ProcedimentoMateria> procedimentos =
                    senadoService.buscarProcedimentos(codigoMateria);

            log.info("Salvos {} procedimentos para matéria {}",
                    procedimentos.size(), codigoMateria);

            procedimentos.forEach(this::publicarTramitacao);

            return procedimentos;

        } catch (Exception e) {
            log.error("Erro ao buscar procedimentos da matéria {}: {}",
                    codigoMateria, e.getMessage(), e);
            throw new ApiExternaException(
                    "Falha ao buscar procedimentos da matéria: " + codigoMateria, e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProcedimentoMateria> buscarPorMateria(Long codigoMateria) {
        return procedimentoRepository.findByCodigoMateria(codigoMateria);
    }

    private void publicarTramitacao(ProcedimentoMateria procedimento) {
        try {
            rabbitMQProducer.sendMessage(
                    WorkflowConfig.TRAMITACAO_EXCHANGE,
                    WorkflowConfig.TRAMITACAO_ROUTING_KEY,
                    procedimento);

            log.debug("Evento publicado para tramitação da matéria: {}",
                    procedimento.getCodigoMateria());

        } catch (Exception e) {
            log.error("Erro ao publicar evento de tramitação: {}", e.getMessage());
        }
    }

    private LocalDateTime parseDataHora(String dataHoraString) {
        if (dataHoraString == null || dataHoraString.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dataHoraString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dataHoraString, DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e2) {
                log.warn("Erro ao converter data: {}", dataHoraString);
                return null;
            }
        }
    }
}