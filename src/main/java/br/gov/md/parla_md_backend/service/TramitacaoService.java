package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Tramitacao;
import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.dto.EncaminhamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.TramitacaoDTO;
import br.gov.md.parla_md_backend.exception.TramitacaoInvalidaException;
import br.gov.md.parla_md_backend.repository.ITramitacaoRepository;
import br.gov.md.parla_md_backend.service.tramitacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciamento de tramitações de processos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TramitacaoService {

    private final ITramitacaoRepository tramitacaoRepository;
    private final ProcessoLegislativoService processoService;
    private final NotificacaoService notificacaoService;

    /**
     * Encaminha processo para setor ou órgão
     */
    @Transactional
    public TramitacaoDTO encaminharProcesso(EncaminhamentoDTO dto, String usuarioId) {
        log.info("Encaminhando processo {} de {} para {}",
                dto.getProcessoId(), dto.getRemetenteId(), dto.getDestinatarioId());

        validarEncaminhamento(dto);

        processoService.buscarPorId(dto.getProcessoId());

        Tramitacao tramitacao = Tramitacao.builder()
                .processoId(dto.getProcessoId())
                .tipo(dto.getTipo())
                .status(StatusTramitacao.ENVIADO)
                .remetenteId(dto.getRemetenteId())
                .remetenteTipo(dto.getRemetenteTipo())
                .remetenteNome(dto.getRemetenteNome())
                .destinatarioId(dto.getDestinatarioId())
                .destinatarioTipo(dto.getDestinatarioTipo())
                .destinatarioNome(dto.getDestinatarioNome())
                .despacho(dto.getDespacho())
                .assunto(dto.getAssunto())
                .dataEnvio(LocalDateTime.now())
                .prazo(LocalDate.from(dto.getPrazo()))
                .urgente(dto.isUrgente())
                .motivoTramitacao(dto.getMotivoTramitacao())
                .remetenteId(usuarioId)
                .observacoes(dto.getObservacoes())
                .build();

        Tramitacao salva = tramitacaoRepository.save(tramitacao);

        notificacaoService.notificarNovoEncaminhamento(salva);

        log.info("Tramitação criada com sucesso: {}", salva.getId());

        return converterParaDTO(salva);
    }

    /**
     * Recebe tramitação
     */
    @Transactional
    public TramitacaoDTO receberTramitacao(String tramitacaoId, String usuarioId) {
        log.info("Recebendo tramitação: {}", tramitacaoId);

        Tramitacao tramitacao = tramitacaoRepository.findById(tramitacaoId)
                .orElseThrow(() -> new TramitacaoInvalidaException(
                        "Tramitação não encontrada: " + tramitacaoId));

        if (tramitacao.getStatus() != StatusTramitacao.ENVIADO) {
            throw new TramitacaoInvalidaException(
                    "Tramitação já foi recebida anteriormente");
        }

        tramitacao.setStatus(StatusTramitacao.RECEBIDO);
        tramitacao.setDataRecebimento(LocalDateTime.now());
        tramitacao.setDestinatarioId(usuarioId);

        Tramitacao atualizada = tramitacaoRepository.save(tramitacao);

        return converterParaDTO(atualizada);
    }

    /**
     * Busca tramitações de um processo
     */
    public List<TramitacaoDTO> buscarPorProcesso(String processoId) {
        log.debug("Buscando tramitações do processo: {}", processoId);

        return tramitacaoRepository.findByProcessoIdOrderByDataEnvioDesc(processoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Busca tramitações pendentes de recebimento
     */
    public Page<TramitacaoDTO> buscarPendentesRecebimento(
            String destinatarioId,
            Pageable pageable) {

        log.debug("Buscando tramitações pendentes para: {}", destinatarioId);

        return tramitacaoRepository.findByDestinatarioIdAndStatus(
                        destinatarioId,
                        StatusTramitacao.ENVIADO,
                        pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca tramitações enviadas por remetente
     */
    public Page<TramitacaoDTO> buscarEnviadasPor(
            String remetenteId,
            Pageable pageable) {

        log.debug("Buscando tramitações enviadas por: {}", remetenteId);

        return tramitacaoRepository.findByRemetenteIdOrderByDataEnvioDesc(
                        remetenteId,
                        pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca tramitações recebidas
     */
    public Page<TramitacaoDTO> buscarRecebidasPor(
            String destinatarioId,
            Pageable pageable) {

        log.debug("Buscando tramitações recebidas por: {}", destinatarioId);

        return tramitacaoRepository.findByDestinatarioIdAndStatus(
                        destinatarioId,
                        StatusTramitacao.RECEBIDO,
                        pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca tramitações com prazo vencido
     */
    public List<TramitacaoDTO> buscarComPrazoVencido() {
        LocalDateTime agora = LocalDateTime.now();

        return tramitacaoRepository.findByPrazoBeforeAndStatus(
                        agora,
                        StatusTramitacao.ENVIADO)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Busca tramitações urgentes pendentes
     */
    public List<TramitacaoDTO> buscarUrgentePendentes(String destinatarioId) {
        return tramitacaoRepository.findByDestinatarioIdAndUrgenteAndStatus(
                        destinatarioId,
                        true,
                        StatusTramitacao.ENVIADO)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private void validarEncaminhamento(EncaminhamentoDTO dto) {
        if (dto.getRemetenteId().equals(dto.getDestinatarioId())) {
            throw new TramitacaoInvalidaException(
                    "Remetente e destinatário não podem ser iguais");
        }

        if (dto.getDespacho() == null || dto.getDespacho().isBlank()) {
            throw new TramitacaoInvalidaException(
                    "Despacho é obrigatório");
        }
    }

    private TramitacaoDTO converterParaDTO(Tramitacao tramitacao) {
        return TramitacaoDTO.builder()
                .id(tramitacao.getId())
                .processoId(tramitacao.getProcessoId())
                .tipo(tramitacao.getTipo())
                .status(tramitacao.getStatus())
                .remetenteId(tramitacao.getRemetenteId())
                .remetenteTipo(tramitacao.getRemetenteTipo())
                .remetenteNome(tramitacao.getRemetenteNome())
                .destinatarioId(tramitacao.getDestinatarioId())
                .destinatarioTipo(tramitacao.getDestinatarioTipo())
                .destinatarioNome(tramitacao.getDestinatarioNome())
                .despacho(tramitacao.getDespacho())
                .assunto(tramitacao.getAssunto())
                .dataEnvio(tramitacao.getDataEnvio())
                .dataRecebimento(tramitacao.getDataRecebimento())
                .prazo(tramitacao.getPrazo())
                .urgente(tramitacao.isUrgente())
                .motivoTramitacao(tramitacao.getMotivoTramitacao())
                .observacoes(tramitacao.getObservacoes())
                .build();
    }
}
