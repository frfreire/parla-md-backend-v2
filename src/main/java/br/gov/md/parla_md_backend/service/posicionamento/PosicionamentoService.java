package br.gov.md.parla_md_backend.service.posicionamento;

import br.gov.md.parla_md_backend.domain.posicionamento.Posicionamento;
import br.gov.md.parla_md_backend.domain.posicionamento.StatusPosicionamento;
import br.gov.md.parla_md_backend.domain.posicionamento.TipoPosicionamento;
import br.gov.md.parla_md_backend.domain.dto.PosicionamentoDTO;
import br.gov.md.parla_md_backend.domain.dto.SolicitacaoPosicionamentoDTO;
import br.gov.md.parla_md_backend.repository.IPosicionamentoRepository;
import br.gov.md.parla_md_backend.service.processo.ProcessoLegislativoService;
import br.gov.md.parla_md_backend.service.tramitacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciamento de posicionamentos externos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosicionamentoService {

    private final IPosicionamentoRepository posicionamentoRepository;
    private final ProcessoLegislativoService processoService;
    private final NotificacaoService notificacaoService;

    /**
     * Solicita posicionamento a órgão externo
     */
    @Transactional
    public PosicionamentoDTO solicitarPosicionamento(
            SolicitacaoPosicionamentoDTO dto,
            String solicitanteId) {

        log.info("Solicitando posicionamento do órgão {} para processo {}",
                dto.getOrgaoEmissorId(), dto.getProcessoId());

        processoService.buscarPorId(dto.getProcessoId());

        String numeroPosicionamento = gerarNumeroPosicionamento();

        Posicionamento posicionamento = Posicionamento.builder()
                .processoId(dto.getProcessoId())
                .numero(numeroPosicionamento)
                .orgaoEmissorId(dto.getOrgaoEmissorId())
                .orgaoEmissorNome(dto.getOrgaoEmissorNome())
                .tipoOrgao(dto.getTipoOrgao())
                .assunto(dto.getAssunto())
                .dataSolicitacao(LocalDateTime.now())
                .prazo(dto.getPrazo())
                .status(StatusPosicionamento.SOLICITADO)
                .observacoes(dto.getObservacoes())
                .build();

        Posicionamento salvo = posicionamentoRepository.save(posicionamento);

        processoService.atualizarContagemPosicionamentos(dto.getProcessoId(), 1);

        notificacaoService.notificarSolicitacaoPosicionamento(salvo);

        log.info("Posicionamento solicitado com sucesso: {}", salvo.getNumero());

        return converterParaDTO(salvo);
    }

    /**
     * Registra posicionamento recebido
     */
    @Transactional
    public PosicionamentoDTO registrarPosicionamento(
            String posicionamentoId,
            String representanteNome,
            String representanteCargo,
            TipoPosicionamento posicao,
            String manifestacao,
            String justificativa,
            List<String> fundamentacao,
            String numeroOficio) {

        log.info("Registrando posicionamento recebido: {}", posicionamentoId);

        Posicionamento posicionamento = posicionamentoRepository.findById(posicionamentoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Posicionamento não encontrado: " + posicionamentoId));

        if (posicionamento.getStatus() != StatusPosicionamento.SOLICITADO) {
            throw new IllegalStateException("Posicionamento já foi recebido");
        }

        posicionamento.setRepresentanteNome(representanteNome);
        posicionamento.setRepresentanteCargo(representanteCargo);
        posicionamento.setPosicao(posicao);
        posicionamento.setManifestacao(manifestacao);
        posicionamento.setJustificativa(justificativa);
        posicionamento.setFundamentacao(fundamentacao);
        posicionamento.setNumeroOficio(numeroOficio);
        posicionamento.setDataRecebimento(LocalDateTime.now());
        posicionamento.setStatus(StatusPosicionamento.RECEBIDO);
        posicionamento.setAtendidoPrazo(
                LocalDateTime.now().isBefore(posicionamento.getPrazo()));

        Posicionamento atualizado = posicionamentoRepository.save(posicionamento);

        processoService.atualizarContagemPosicionamentos(
                posicionamento.getProcessoId(), -1);

        notificacaoService.notificarPosicionamentoRecebido(atualizado);

        return converterParaDTO(atualizado);
    }

    /**
     * Busca posicionamentos de um processo
     */
    public List<PosicionamentoDTO> buscarPorProcesso(String processoId) {
        log.debug("Buscando posicionamentos do processo: {}", processoId);

        return posicionamentoRepository.findByProcessoIdOrderByDataSolicitacaoDesc(processoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Busca posicionamentos pendentes
     */
    public Page<PosicionamentoDTO> buscarPendentes(Pageable pageable) {
        return posicionamentoRepository.findByStatus(
                        StatusPosicionamento.SOLICITADO,
                        pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca posicionamentos com prazo vencido
     */
    public List<PosicionamentoDTO> buscarComPrazoVencido() {
        LocalDateTime agora = LocalDateTime.now();

        return posicionamentoRepository.findByPrazoBeforeAndStatus(
                        agora,
                        StatusPosicionamento.SOLICITADO)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private String gerarNumeroPosicionamento() {
        int ano = LocalDateTime.now().getYear();
        long count = posicionamentoRepository.countByNumeroStartingWith(
                String.valueOf(ano));
        return String.format("POSIC-%d/%05d", ano, count + 1);
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