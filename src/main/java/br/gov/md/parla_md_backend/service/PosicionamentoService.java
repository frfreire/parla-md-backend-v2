package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import br.gov.md.parla_md_backend.domain.Posicionamento;
import br.gov.md.parla_md_backend.domain.dto.PosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.RegistrarPosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitacaoPosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IOrgaoExternoRepository;
import br.gov.md.parla_md_backend.repository.IPosicionamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static br.gov.md.parla_md_backend.config.WorkflowConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosicionamentoService {

    private final IPosicionamentoRepository posicionamentoRepository;
    private final IOrgaoExternoRepository orgaoExternoRepository;
    private final ProcessoLegislativoService processoService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public PosicionamentoDTO solicitarPosicionamento(SolicitacaoPosicionamentoDTO dto, String solicitanteId) {
        log.info("Solicitando posicionamento do órgão {} para processo {}",
                dto.orgaoExternoId(), dto.processoId());

        processoService.buscarPorId(dto.processoId());

        OrgaoExterno orgao = orgaoExternoRepository.findById(dto.orgaoExternoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão externo não encontrado"));

        if (!orgao.isAtivo()) {
            throw new IllegalStateException("Órgão externo está inativo");
        }

        verificarPosicionamentoDuplicado(dto.processoId(), dto.orgaoExternoId());

        String numeroPosicionamento = gerarNumeroPosicionamento();

        Posicionamento posicionamento = Posicionamento.builder()
                .numero(numeroPosicionamento)
                .processoId(dto.processoId())
                .orgaoEmissorId(orgao.getId())
                .orgaoEmissorNome(orgao.getNome())
                .tipoOrgao(String.valueOf(orgao.getTipo()))
                .assunto(dto.assunto())
                .dataSolicitacao(LocalDateTime.now())
                .prazo(dto.prazo())
                .status(StatusPosicionamento.PENDENTE)
                .observacoes(dto.observacoes())
                .build();

        posicionamento = posicionamentoRepository.save(posicionamento);

        enviarParaFila(posicionamento, "POSICIONAMENTO_SOLICITADO");

        log.info("Posicionamento {} solicitado com sucesso", posicionamento.getNumero());

        return converterParaDTO(posicionamento);
    }

    @Transactional
    public PosicionamentoDTO registrarPosicionamento(RegistrarPosicionamentoDTO dto, String registradorId) {
        log.info("Registrando posicionamento: {}", dto.posicionamentoId());

        Posicionamento posicionamento = posicionamentoRepository.findById(dto.posicionamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Posicionamento não encontrado"));

        if (posicionamento.getStatus() != StatusPosicionamento.PENDENTE) {
            throw new IllegalStateException("Posicionamento já foi respondido");
        }

        posicionamento.setPosicao(dto.tipo());
        posicionamento.setManifestacao(dto.conteudo());
        posicionamento.setJustificativa(dto.justificativa());
        posicionamento.setFundamentacao(dto.fundamentacao());
        posicionamento.setCondicoesRessalvas(dto.condicoesRessalvas());
        posicionamento.setImpactoEstimado(dto.impactoEstimado());
        posicionamento.setRepresentanteNome(dto.representanteNome());
        posicionamento.setRepresentanteCargo(dto.representanteCargo());
        posicionamento.setNumeroOficio(dto.numeroOficio());
        posicionamento.setDocumentoOficial(dto.documentoOficial());
        posicionamento.setAnexos(dto.anexos());
        posicionamento.setDataRecebimento(LocalDateTime.now());
        posicionamento.setAtendidoPrazo(posicionamento.getPrazo() != null &&
                LocalDateTime.now().isBefore(posicionamento.getPrazo()));
        posicionamento.setStatus(StatusPosicionamento.RECEBIDO);

        if (dto.observacoes() != null) {
            posicionamento.setObservacoes(dto.observacoes());
        }

        posicionamento = posicionamentoRepository.save(posicionamento);

        enviarParaFila(posicionamento, "POSICIONAMENTO_RECEBIDO");

        log.info("Posicionamento {} registrado com sucesso", posicionamento.getNumero());

        return converterParaDTO(posicionamento);
    }

    @Transactional
    public PosicionamentoDTO consolidarPosicionamento(String posicionamentoId, String consolidadorId) {
        log.info("Consolidando posicionamento: {}", posicionamentoId);

        Posicionamento posicionamento = posicionamentoRepository.findById(posicionamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Posicionamento não encontrado"));

        if (posicionamento.getStatus() != StatusPosicionamento.RECEBIDO) {
            throw new IllegalStateException("Posicionamento não está em status RECEBIDO");
        }

        posicionamento.setStatus(StatusPosicionamento.CONSOLIDADO);

        posicionamento = posicionamentoRepository.save(posicionamento);

        enviarParaFila(posicionamento, "POSICIONAMENTO_CONSOLIDADO");

        log.info("Posicionamento {} consolidado com sucesso", posicionamento.getNumero());

        return converterParaDTO(posicionamento);
    }

    public List<PosicionamentoDTO> buscarPorProcesso(String processoId) {
        log.debug("Buscando posicionamentos do processo: {}", processoId);

        return posicionamentoRepository.findByProcessoId(processoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public Page<PosicionamentoDTO> buscarPendentesPorOrgao(String orgaoId, Pageable pageable) {
        log.debug("Buscando posicionamentos pendentes do órgão: {}", orgaoId);

        return posicionamentoRepository.findByOrgaoEmissorIdAndStatus(
                        orgaoId,
                        StatusPosicionamento.PENDENTE,
                        pageable)
                .map(this::converterParaDTO);
    }

    public List<PosicionamentoDTO> buscarComPrazoVencido() {
        LocalDateTime agora = LocalDateTime.now();

        return posicionamentoRepository.findByPrazoBeforeAndStatus(
                        agora,
                        StatusPosicionamento.PENDENTE)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public Page<PosicionamentoDTO> buscarPendentesConsolidacao(Pageable pageable) {
        return posicionamentoRepository.findByStatus(StatusPosicionamento.RECEBIDO, pageable)
                .map(this::converterParaDTO);
    }

    public PosicionamentoDTO buscarPorId(String posicionamentoId) {
        Posicionamento posicionamento = posicionamentoRepository.findById(posicionamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Posicionamento não encontrado"));

        return converterParaDTO(posicionamento);
    }

    public PosicionamentoDTO buscarPorNumero(String numero) {
        Posicionamento posicionamento = posicionamentoRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Posicionamento não encontrado"));

        return converterParaDTO(posicionamento);
    }

    private void verificarPosicionamentoDuplicado(String processoId, String orgaoId) {
        boolean existe = posicionamentoRepository.existsByProcessoIdAndOrgaoEmissorId(processoId, orgaoId);

        if (existe) {
            throw new IllegalStateException("Órgão já possui posicionamento solicitado para este processo");
        }
    }

    private String gerarNumeroPosicionamento() {
        int ano = LocalDateTime.now().getYear();
        long count = posicionamentoRepository.countByNumeroStartingWith(String.valueOf(ano));
        return String.format("POSIC-%d/%05d", ano, count + 1);
    }

    private void enviarParaFila(Posicionamento posicionamento, String evento) {
        try {
            rabbitTemplate.convertAndSend(
                    NOTIFICACAO_EXCHANGE,
                    NOTIFICACAO_ROUTING_KEY,
                    Map.of(
                            "tipo", evento,
                            "posicionamentoId", posicionamento.getId(),
                            "processoId", posicionamento.getProcessoId(),
                            "numero", posicionamento.getNumero()
                    )
            );
            log.debug("Evento {} enviado para fila RabbitMQ", evento);
        } catch (Exception e) {
            log.error("Erro ao enviar evento {} para fila", evento, e);
        }
    }

    private PosicionamentoDTO converterParaDTO(Posicionamento posicionamento) {
        return PosicionamentoDTO.builder()
                .id(posicionamento.getId())
                .processoId(posicionamento.getProcessoId())
                .numero(posicionamento.getNumero())
                .orgaoEmissorId(posicionamento.getOrgaoEmissorId())
                .orgaoEmissorNome(posicionamento.getOrgaoEmissorNome())
                .tipoOrgao(posicionamento.getTipoOrgao())
                .representanteNome(posicionamento.getRepresentanteNome())
                .representanteCargo(posicionamento.getRepresentanteCargo())
                .posicao(posicionamento.getPosicao())
                .assunto(posicionamento.getAssunto())
                .manifestacao(posicionamento.getManifestacao())
                .justificativa(posicionamento.getJustificativa())
                .fundamentacao(posicionamento.getFundamentacao())
                .dataSolicitacao(posicionamento.getDataSolicitacao())
                .dataRecebimento(posicionamento.getDataRecebimento())
                .prazo(posicionamento.getPrazo())
                .atendidoPrazo(posicionamento.isAtendidoPrazo())
                .status(posicionamento.getStatus())
                .numeroOficio(posicionamento.getNumeroOficio())
                .anexos(posicionamento.getAnexos())
                .observacoes(posicionamento.getObservacoes())
                .build();
    }
}