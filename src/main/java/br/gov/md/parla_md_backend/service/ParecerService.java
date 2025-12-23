package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.Parecer;
import br.gov.md.parla_md_backend.domain.parecer.Recomendacao;
import br.gov.md.parla_md_backend.dto.parecer.ParecerDTO;
import br.gov.md.parla_md_backend.dto.parecer.SolicitarParecerDTO;
import br.gov.md.parla_md_backend.exception.ParecerJaExisteException;
import br.gov.md.parla_md_backend.repository.IParecerRepository;
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
 * Serviço para gerenciamento de pareceres técnicos internos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParecerService {

    private final IParecerRepository parecerRepository;
    private final ProcessoLegislativoService processoService;
    private final NotificacaoService notificacaoService;

    /**
     * Solicita parecer a um setor
     */
    @Transactional
    public ParecerDTO solicitarParecer(SolicitarParecerDTO dto, String solicitanteId) {
        log.info("Solicitando parecer do setor {} para processo {}",
                dto.getSetorEmissorId(), dto.getProcessoId());

        processoService.buscarPorId(dto.getProcessoId());

        verificarParecerDuplicado(dto.getProcessoId(), dto.getSetorEmissorId());

        String numeroParecer = gerarNumeroParecer();

        Parecer parecer = Parecer.builder()
                .processoId(dto.getProcessoId())
                .numero(numeroParecer)
                .setorEmissorId(dto.getSetorEmissorId())
                .setorEmissorNome(dto.getSetorEmissorNome())
                .tipo(dto.getTipo())
                .assunto(dto.getAssunto())
                .dataSolicitacao(LocalDateTime.now())
                .prazo(dto.getPrazo())
                .observacoes(dto.getObservacoes())
                .build();

        Parecer salvo = parecerRepository.save(parecer);

        processoService.atualizarContagemPareceres(dto.getProcessoId(), 1);

        notificacaoService.notificarSolicitacaoParecer(salvo);

        log.info("Parecer solicitado com sucesso: {}", salvo.getNumero());

        return converterParaDTO(salvo);
    }

    /**
     * Emite parecer
     */
    @Transactional
    public ParecerDTO emitirParecer(
            String parecerId,
            String analistaId,
            String analistaNome,
            String contexto,
            String analise,
            Recomendacao recomendacao,
            String justificativa,
            List<String> fundamentacaoLegal,
            List<String> impactosIdentificados,
            String conclusao) {

        log.info("Emitindo parecer: {}", parecerId);

        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parecer não encontrado: " + parecerId));

        if (parecer.getDataEmissao() != null) {
            throw new IllegalStateException("Parecer já foi emitido");
        }

        parecer.setAnalistaResponsavelId(analistaId);
        parecer.setAnalistaResponsavelNome(analistaNome);
        parecer.setContexto(contexto);
        parecer.setAnalise(analise);
        parecer.setRecomendacao(recomendacao);
        parecer.setJustificativaRecomendacao(justificativa);
        parecer.setFundamentacaoLegal(fundamentacaoLegal);
        parecer.setImpactosIdentificados(impactosIdentificados);
        parecer.setConclusao(conclusao);
        parecer.setDataEmissao(LocalDateTime.now());
        parecer.setAtendidoPrazo(LocalDateTime.now().isBefore(parecer.getPrazo()));

        Parecer atualizado = parecerRepository.save(parecer);

        notificacaoService.notificarParecerEmitido(atualizado);

        return converterParaDTO(atualizado);
    }

    /**
     * Aprova parecer (superior hierárquico)
     */
    @Transactional
    public ParecerDTO aprovarParecer(
            String parecerId,
            String aprovadorId,
            String aprovadorNome) {

        log.info("Aprovando parecer: {}", parecerId);

        Parecer parecer = parecerRepository.findById(parecerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parecer não encontrado: " + parecerId));

        if (parecer.getDataEmissao() == null) {
            throw new IllegalStateException("Parecer ainda não foi emitido");
        }

        if (parecer.getDataAprovacao() != null) {
            throw new IllegalStateException("Parecer já foi aprovado");
        }

        parecer.setAprovadoPorId(aprovadorId);
        parecer.setAprovadoPorNome(aprovadorNome);
        parecer.setDataAprovacao(LocalDateTime.now());

        Parecer atualizado = parecerRepository.save(parecer);

        processoService.atualizarContagemPareceres(parecer.getProcessoId(), -1);

        notificacaoService.notificarParecerAprovado(atualizado);

        return converterParaDTO(atualizado);
    }

    /**
     * Busca pareceres de um processo
     */
    public List<ParecerDTO> buscarPorProcesso(String processoId) {
        log.debug("Buscando pareceres do processo: {}", processoId);

        return parecerRepository.findByProcessoIdOrderByDataSolicitacaoDesc(processoId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Busca pareceres pendentes de um setor
     */
    public Page<ParecerDTO> buscarPendentesPorSetor(
            String setorId,
            Pageable pageable) {

        log.debug("Buscando pareceres pendentes do setor: {}", setorId);

        return parecerRepository.findBySetorEmissorIdAndDataEmissaoIsNull(
                        setorId,
                        pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca pareceres emitidos pendentes de aprovação
     */
    public Page<ParecerDTO> buscarPendentesAprovacao(Pageable pageable) {
        return parecerRepository.findByDataEmissaoIsNotNullAndDataAprovacaoIsNull(pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca pareceres com prazo vencido
     */
    public List<ParecerDTO> buscarComPrazoVencido() {
        LocalDateTime agora = LocalDateTime.now();

        return parecerRepository.findByPrazoBeforeAndDataEmissaoIsNull(agora)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private void verificarParecerDuplicado(String processoId, String setorId) {
        boolean existe = parecerRepository.existsByProcessoIdAndSetorEmissorId(
                processoId, setorId);

        if (existe) {
            throw new ParecerJaExisteException(
                    "Setor já possui parecer solicitado para este processo");
        }
    }

    private String gerarNumeroParecer() {
        int ano = LocalDateTime.now().getYear();
        long count = parecerRepository.countByNumeroStartingWith(String.valueOf(ano));
        return String.format("PARECER-%d/%05d", ano, count + 1);
    }

    private ParecerDTO converterParaDTO(Parecer parecer) {
        return ParecerDTO.builder()
                .id(parecer.getId())
                .processoId(parecer.getProcessoId())
                .numero(parecer.getNumero())
                .setorEmissorId(parecer.getSetorEmissorId())
                .setorEmissorNome(parecer.getSetorEmissorNome())
                .analistaResponsavelId(parecer.getAnalistaResponsavelId())
                .analistaResponsavelNome(parecer.getAnalistaResponsavelNome())
                .tipo(parecer.getTipo())
                .assunto(parecer.getAssunto())
                .contexto(parecer.getContexto())
                .analise(parecer.getAnalise())
                .recomendacao(parecer.getRecomendacao())
                .justificativaRecomendacao(parecer.getJustificativaRecomendacao())
                .fundamentacaoLegal(parecer.getFundamentacaoLegal())
                .impactosIdentificados(parecer.getImpactosIdentificados())
                .conclusao(parecer.getConclusao())
                .dataSolicitacao(parecer.getDataSolicitacao())
                .dataEmissao(parecer.getDataEmissao())
                .prazo(parecer.getPrazo())
                .atendidoPrazo(parecer.isAtendidoPrazo())
                .aprovadoPorId(parecer.getAprovadoPorId())
                .aprovadoPorNome(parecer.getAprovadoPorNome())
                .dataAprovacao(parecer.getDataAprovacao())
                .anexos(parecer.getAnexos())
                .observacoes(parecer.getObservacoes())
                .build();
    }
}