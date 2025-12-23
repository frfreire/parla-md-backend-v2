package br.gov.md.parla_md_backend.service.processo;

import br.gov.md.parla_md_backend.domain.processo.ProcessoLegislativo;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import br.gov.md.parla_md_backend.domain.processo.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.proposicao.Proposicao;
import br.gov.md.parla_md_backend.dto.processo.CriarProcessoDTO;
import br.gov.md.parla_md_backend.dto.processo.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.exception.ProcessoNaoEncontradoException;
import br.gov.md.parla_md_backend.exception.ProposicaoNaoEncontradaException;
import br.gov.md.parla_md_backend.repository.IProcessoLegislativoRepository;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de processos legislativos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessoLegislativoService {

    private final IProcessoLegislativoRepository processoRepository;
    private final IProposicaoRepository proposicaoRepository;

    /**
     * Cria um novo processo legislativo
     */
    @Transactional
    public ProcessoLegislativoDTO criarProcesso(CriarProcessoDTO dto, String usuarioId) {
        log.info("Criando novo processo legislativo: {}", dto.getTitulo());

        validarProposicoes(dto.getProposicaoIds());

        String numeroProcesso = gerarNumeroProcesso();

        List<Proposicao> proposicoes = proposicaoRepository.findAllById(dto.getProposicaoIds());

        ProcessoLegislativo processo = ProcessoLegislativo.builder()
                .numero(numeroProcesso)
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .temaPrincipal(dto.getTemaPrincipal())
                .prioridade(dto.getPrioridade())
                .status(StatusProcesso.CRIADO)
                .proposicoesVinculadas(proposicoes)
                .setorResponsavel(dto.getSetorResponsavel())
                .analistaResponsavel(usuarioId)
                .dataCriacao(LocalDateTime.now())
                .dataUltimaAtualizacao(LocalDateTime.now())
                .prazoFinal(dto.getPrazoFinal())
                .areasImpacto(dto.getAreasImpacto())
                .requerAnaliseJuridica(dto.isRequerAnaliseJuridica())
                .requerAnaliseOrcamentaria(dto.isRequerAnaliseOrcamentaria())
                .requerConsultaExterna(dto.isRequerConsultaExterna())
                .numeroPareceresPendentes(0)
                .numeroPosicionamentosPendentes(0)
                .observacoes(dto.getObservacoes())
                .build();

        ProcessoLegislativo salvo = processoRepository.save(processo);

        log.info("Processo legislativo criado com sucesso: {}", salvo.getNumero());

        return converterParaDTO(salvo);
    }

    /**
     * Busca processo por ID
     */
    public ProcessoLegislativoDTO buscarPorId(String id) {
        log.debug("Buscando processo por ID: {}", id);

        ProcessoLegislativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ProcessoNaoEncontradoException(
                        "Processo não encontrado com ID: " + id));

        return converterParaDTO(processo);
    }

    /**
     * Lista processos com paginação
     */
    public Page<ProcessoLegislativoDTO> listarProcessos(Pageable pageable) {
        log.debug("Listando processos - página: {}, tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return processoRepository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca processos por status
     */
    public Page<ProcessoLegislativoDTO> buscarPorStatus(
            StatusProcesso status,
            Pageable pageable) {

        log.debug("Buscando processos por status: {}", status);

        return processoRepository.findByStatus(status, pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca processos por setor responsável
     */
    public Page<ProcessoLegislativoDTO> buscarPorSetor(
            String setorId,
            Pageable pageable) {

        log.debug("Buscando processos por setor: {}", setorId);

        return processoRepository.findBySetorResponsavel(setorId, pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Busca processos por analista responsável
     */
    public Page<ProcessoLegislativoDTO> buscarPorAnalista(
            String analistaId,
            Pageable pageable) {

        log.debug("Buscando processos por analista: {}", analistaId);

        return processoRepository.findByAnalistaResponsavel(analistaId, pageable)
                .map(this::converterParaDTO);
    }

    /**
     * Atualiza status do processo
     */
    @Transactional
    public ProcessoLegislativoDTO atualizarStatus(
            String processoId,
            StatusProcesso novoStatus,
            String observacao) {

        log.info("Atualizando status do processo {} para {}", processoId, novoStatus);

        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        validarTransicaoStatus(processo.getStatus(), novoStatus);

        processo.setStatus(novoStatus);
        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        if (observacao != null && !observacao.isBlank()) {
            String obsAtual = processo.getObservacoes() != null ? processo.getObservacoes() : "";
            processo.setObservacoes(obsAtual + "\n[" + LocalDateTime.now() + "] " + observacao);
        }

        if (novoStatus == StatusProcesso.FINALIZADO) {
            processo.setDataFinalizacao(LocalDateTime.now());
        }

        ProcessoLegislativo atualizado = processoRepository.save(processo);

        return converterParaDTO(atualizado);
    }

    /**
     * Vincula proposição ao processo
     */
    @Transactional
    public ProcessoLegislativoDTO vincularProposicao(
            String processoId,
            String proposicaoId) {

        log.info("Vinculando proposição {} ao processo {}", proposicaoId, processoId);

        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        Proposicao proposicao = proposicaoRepository.findById(proposicaoId)
                .orElseThrow(() -> new ProposicaoNaoEncontradaException(
                        "Proposição não encontrada: " + proposicaoId));

        if (processo.getProposicoesVinculadas().stream()
                .anyMatch(p -> p.getId().equals(proposicaoId))) {
            throw new IllegalStateException(
                    "Proposição já está vinculada ao processo");
        }

        processo.getProposicoesVinculadas().add(proposicao);
        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        ProcessoLegislativo atualizado = processoRepository.save(processo);

        return converterParaDTO(atualizado);
    }

    /**
     * Desvincula proposição do processo
     */
    @Transactional
    public ProcessoLegislativoDTO desvincularProposicao(
            String processoId,
            String proposicaoId) {

        log.info("Desvinculando proposição {} do processo {}", proposicaoId, processoId);

        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        boolean removido = processo.getProposicoesVinculadas()
                .removeIf(p -> p.getId().equals(proposicaoId));

        if (!removido) {
            throw new IllegalStateException(
                    "Proposição não está vinculada ao processo");
        }

        if (processo.getProposicoesVinculadas().isEmpty()) {
            throw new IllegalStateException(
                    "Processo deve ter pelo menos uma proposição vinculada");
        }

        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        ProcessoLegislativo atualizado = processoRepository.save(processo);

        return converterParaDTO(atualizado);
    }

    /**
     * Atualiza contador de pareceres pendentes
     */
    @Transactional
    public void atualizarContagemPareceres(String processoId, int delta) {
        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        int novaContagem = processo.getNumeroPareceresPendentes() + delta;
        processo.setNumeroPareceresPendentes(Math.max(0, novaContagem));
        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        processoRepository.save(processo);
    }

    /**
     * Atualiza contador de posicionamentos pendentes
     */
    @Transactional
    public void atualizarContagemPosicionamentos(String processoId, int delta) {
        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        int novaContagem = processo.getNumeroPosicionamentosPendentes() + delta;
        processo.setNumeroPosicionamentosPendentes(Math.max(0, novaContagem));
        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        processoRepository.save(processo);
    }

    /**
     * Define posição final do MD
     */
    @Transactional
    public ProcessoLegislativoDTO definirPosicaoFinal(
            String processoId,
            String posicao,
            String justificativa,
            String usuarioId) {

        log.info("Definindo posição final do MD para processo {}", processoId);

        ProcessoLegislativo processo = buscarProcessoOuLancarExcecao(processoId);

        if (processo.getNumeroPareceresPendentes() > 0 ||
                processo.getNumeroPosicionamentosPendentes() > 0) {
            throw new IllegalStateException(
                    "Existem pareceres ou posicionamentos pendentes");
        }

        processo.setPosicaoFinalMD(posicao);
        processo.setJustificativaPosicaoFinal(justificativa);
        processo.setStatus(StatusProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now());
        processo.setDataUltimaAtualizacao(LocalDateTime.now());

        ProcessoLegislativo atualizado = processoRepository.save(processo);

        return converterParaDTO(atualizado);
    }

    private ProcessoLegislativo buscarProcessoOuLancarExcecao(String id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new ProcessoNaoEncontradoException(
                        "Processo não encontrado: " + id));
    }

    private void validarProposicoes(List<String> proposicaoIds) {
        if (proposicaoIds == null || proposicaoIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Processo deve ter pelo menos uma proposição vinculada");
        }

        List<Proposicao> proposicoes = proposicaoRepository.findAllById(proposicaoIds);

        if (proposicoes.size() != proposicaoIds.size()) {
            throw new ProposicaoNaoEncontradaException(
                    "Uma ou mais proposições não foram encontradas");
        }
    }

    private void validarTransicaoStatus(StatusProcesso atual, StatusProcesso novo) {
        // Implementar lógica de validação de transições permitidas
        // Por exemplo: CRIADO -> EM_ANALISE_INTERNA é permitido
        // mas FINALIZADO -> CRIADO não é permitido
    }

    private String gerarNumeroProcesso() {
        int ano = LocalDateTime.now().getYear();
        long count = processoRepository.countByNumeroStartingWith(String.valueOf(ano));
        return String.format("%d/%05d", ano, count + 1);
    }

    private ProcessoLegislativoDTO converterParaDTO(ProcessoLegislativo processo) {
        return ProcessoLegislativoDTO.builder()
                .id(processo.getId())
                .numero(processo.getNumero())
                .titulo(processo.getTitulo())
                .descricao(processo.getDescricao())
                .temaPrincipal(processo.getTemaPrincipal())
                .prioridade(processo.getPrioridade())
                .status(processo.getStatus())
                .proposicaoIds(processo.getProposicoesVinculadas().stream()
                        .map(Proposicao::getId)
                        .collect(Collectors.toList()))
                .setorResponsavel(processo.getSetorResponsavel())
                .analistaResponsavel(processo.getAnalistaResponsavel())
                .dataCriacao(processo.getDataCriacao())
                .dataUltimaAtualizacao(processo.getDataUltimaAtualizacao())
                .prazoFinal(processo.getPrazoFinal())
                .areasImpacto(processo.getAreasImpacto())
                .requerAnaliseJuridica(processo.isRequerAnaliseJuridica())
                .requerAnaliseOrcamentaria(processo.isRequerAnaliseOrcamentaria())
                .requerConsultaExterna(processo.isRequerConsultaExterna())
                .numeroPareceresPendentes(processo.getNumeroPareceresPendentes())
                .numeroPosicionamentosPendentes(processo.getNumeroPosicionamentosPendentes())
                .posicaoFinalMD(processo.getPosicaoFinalMD())
                .justificativaPosicaoFinal(processo.getJustificativaPosicaoFinal())
                .dataFinalizacao(processo.getDataFinalizacao())
                .observacoes(processo.getObservacoes())
                .build();
    }
}