package br.gov.md.parla_md_backend.service;

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

    private static final String PROCEDURE_EXCHANGE = "procedure.exchange";
    private static final String PROCEDURE_ROUTING_KEY = "procedure.new";
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

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledUpdateAllMatterProcedures() {
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
                                fetchAndSaveProcedures(materia.getCodigoMateria());

                        log.debug("Atualizados {} procedimentos para a matéria {}",
                                procedimentos.size(), materia.getCodigoMateria());

                        atualizadas.incrementAndGet();

                        if (!procedimentos.isEmpty()) {
                            ProcedimentoMateria ultimo = procedimentos.get(
                                    procedimentos.size() - 1);

                            materia.setSituacaoAtual(ultimo.getSituacaoDescricao());
                            materia.setDataUltimaAtualizacao(LocalDateTime.now());
                            materiaRepository.save(materia);
                        }

                        rabbitMQProducer.sendMessage("materia.exchange",
                                "materia.atualizada", materia);
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

    @RabbitListener(queues = "matter.queue")
    @Transactional
    public void processMatterMessage(String message) {
        try {
            String codigoStr = message.replaceAll("[^0-9]", "");
            Long codigoMateria = Long.parseLong(codigoStr);

            materiaRepository.findByCodigoMateria(codigoMateria)
                    .ifPresent(materia -> fetchAndSaveProcedures(codigoMateria));

        } catch (Exception e) {
            log.error("Erro ao processar mensagem da fila: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public List<ProcedimentoMateria> fetchAndSaveProcedures(Long codigoMateria) {
        try {
            log.debug("Buscando procedimentos para matéria: {}", codigoMateria);

            List<ProcedimentoMateria> procedimentos =
                    senadoService.buscarProcedimentos(codigoMateria);

            log.info("Salvos {} procedimentos para matéria {}",
                    procedimentos.size(), codigoMateria);

            procedimentos.forEach(this::publicarProcedimento);

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

    private void publicarProcedimento(ProcedimentoMateria procedimento) {
        try {
            rabbitMQProducer.sendMessage(
                    PROCEDURE_EXCHANGE,
                    PROCEDURE_ROUTING_KEY,
                    procedimento);

            log.debug("Evento publicado para procedimento da matéria: {}",
                    procedimento.getCodigoMateria());

        } catch (Exception e) {
            log.error("Erro ao publicar evento de procedimento: {}", e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateTimeString, DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e2) {
                log.warn("Erro ao parsear data: {}", dateTimeString);
                return null;
            }
        }
    }
}