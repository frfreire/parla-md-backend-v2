package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.dto.EncaminhamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.TramitacaoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.TipoTramitacao;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.exception.TramitacaoInvalidaException;
import br.gov.md.parla_md_backend.repository.IProcessoLegislativoRepository;
import br.gov.md.parla_md_backend.repository.ITramitacaoRepository;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static br.gov.md.parla_md_backend.config.WorkflowConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TramitacaoService {

    private final ITramitacaoRepository tramitacaoRepository;
    private final IProcessoLegislativoRepository processoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Tramitacao encaminhar(EncaminhamentoDTO dto, String remetenteId) {
        ProcessoLegislativo processo = processoRepository.findById(dto.getProcessoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Remetente não encontrado"));

        Usuario destinatario = usuarioRepository.findById(dto.getDestinatarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Destinatário não encontrado"));

        Tramitacao tramitacao = Tramitacao.builder()
                .processoId(processo.getId())
                .tipo(dto.getTipo())
                .remetenteId(remetente.getId())
                .remetenteTipo("SETOR")
                .remetenteNome(remetente.getSetorNome())
                .destinatarioId(destinatario.getId())
                .destinatarioTipo(dto.getDestinatarioTipo())
                .destinatarioNome(destinatario.getNome())
                .despacho(dto.getDespacho())
                .observacoes(dto.getObservacoes())
                .status(StatusTramitacao.PENDENTE)
                .urgente(Boolean.TRUE.equals(dto.isUrgente()))
                .dataEnvio(LocalDateTime.now())
                .prazo(LocalDate.from(dto.getPrazo()))
                .build();

        tramitacao = tramitacaoRepository.save(tramitacao);

        enviarParaFila(tramitacao);

        log.info("Tramitação {} encaminhada do setor {} para {}",
                tramitacao.getId(), remetente.getSetorNome(), destinatario.getNome());

        return tramitacao;
    }

    public Page<TramitacaoDTO> buscarPorDestinatario(String destinatarioId, Pageable pageable) {
        return tramitacaoRepository.findByDestinatarioIdAndStatus(
                        destinatarioId,
                        StatusTramitacao.PENDENTE,
                        pageable)
                .map(this::converterParaDTO);
    }

    public Page<TramitacaoDTO> buscarPorRemetente(String remetenteId, Pageable pageable) {
        return tramitacaoRepository.findByRemetenteIdOrderByDataEnvioDesc(remetenteId, pageable)
                .map(this::converterParaDTO);
    }

    public List<TramitacaoDTO> buscarPorProcesso(String processoId) {
        return tramitacaoRepository.findByProcessoIdOrderByDataEnvioDesc(processoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<TramitacaoDTO> buscarUrgentes(String destinatarioId) {
        return tramitacaoRepository.findByDestinatarioIdAndUrgenteAndStatus(
                        destinatarioId,
                        true,
                        StatusTramitacao.PENDENTE)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public Tramitacao receber(String tramitacaoId, String destinatarioId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        if (!tramitacao.getDestinatarioId().equals(destinatarioId)) {
            throw new TramitacaoInvalidaException("Tramitação não pertence ao destinatário");
        }

        if (tramitacao.getStatus() != StatusTramitacao.PENDENTE) {
            throw new TramitacaoInvalidaException("Tramitação já foi recebida");
        }

        tramitacao.setStatus(StatusTramitacao.RECEBIDO);
        tramitacao.setDataRecebimento(LocalDateTime.now());
        tramitacao.setDataAtualizacao(LocalDateTime.now());

        tramitacao = tramitacaoRepository.save(tramitacao);

        log.info("Tramitação {} recebida por {}", tramitacaoId, destinatarioId);

        return tramitacao;
    }

    @Transactional
    public Tramitacao concluir(String tramitacaoId, String destinatarioId) {
        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tramitação não encontrada"));

        if (!tramitacao.getDestinatarioId().equals(destinatarioId)) {
            throw new TramitacaoInvalidaException("Tramitação não pertence ao destinatário");
        }

        if (tramitacao.getStatus() == StatusTramitacao.CONCLUIDO) {
            throw new TramitacaoInvalidaException("Tramitação já foi concluída");
        }

        tramitacao.setStatus(StatusTramitacao.CONCLUIDO);
        tramitacao.setDataConclusao(LocalDateTime.now());
        tramitacao.setDataAtualizacao(LocalDateTime.now());

        tramitacao = tramitacaoRepository.save(tramitacao);

        log.info("Tramitação {} concluída por {}", tramitacaoId, destinatarioId);

        return tramitacao;
    }

    private void enviarParaFila(Tramitacao tramitacao) {
        try {
            rabbitTemplate.convertAndSend(
                    TRAMITACAO_EXCHANGE,
                    TRAMITACAO_ROUTING_KEY,
                    tramitacao
            );
            log.debug("Tramitação {} enviada para fila RabbitMQ", tramitacao.getId());
        } catch (Exception e) {
            log.error("Erro ao enviar tramitação para fila", e);
        }
    }

    private TramitacaoDTO converterParaDTO(Tramitacao tramitacao) {
        return new TramitacaoDTO(
                tramitacao.getId(),
                tramitacao.getProcessoId(),
                tramitacao.getTipo(),
                tramitacao.getRemetenteId(),
                tramitacao.getRemetenteTipo(),
                tramitacao.getRemetenteNome(),
                tramitacao.getDestinatarioId(),
                tramitacao.getDestinatarioTipo(),
                tramitacao.getDestinatarioNome(),
                tramitacao.getDespacho(),
                tramitacao.getObservacoes(),
                tramitacao.getStatus(),
                tramitacao.isUrgente(),
                tramitacao.getDataEnvio(),
                tramitacao.getDataRecebimento(),
                tramitacao.getDataConclusao(),
                tramitacao.getPrazo(),
                tramitacao.getDataCriacao()
        );
    }
}