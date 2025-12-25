package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.*;
import br.gov.md.parla_md_backend.domain.dto.CriarProcessoDTO;
import br.gov.md.parla_md_backend.domain.dto.ProcessoLegislativoDTO;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessoLegislativoService {

    private final IProcessoLegislativoRepository processoRepository;
    private final IProposicaoRepository proposicaoRepository;
    private final IMateriaRepository materiaRepository;
    private final ISetorRepository setorRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional
    public ProcessoLegislativo criar(CriarProcessoDTO dto, String criadorId) {
        if (processoRepository.existsByNumero(dto.getNumeroProcesso())) {
            throw new IllegalArgumentException("Já existe processo com este número");
        }

        Usuario criador = usuarioRepository.findById(criadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Setor setor = setorRepository.findById(dto.getSetorResponsavel())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        ProcessoLegislativo processo = ProcessoLegislativo.builder()
                .numero(dto.getNumeroProcesso())
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .status(StatusProcesso.INICIADO)
                .prioridade(dto.getPrioridade())
                .temaPrincipal(dto.getTemaPrincipal())
                .setorResponsavelId(setor.getId())
                .setorResponsavelNome(setor.getNome())
                .gestorId(criador.getId())
                .gestorNome(criador.getNome())
                .requerAnaliseJuridica(Boolean.TRUE.equals(dto.isRequerAnaliseJuridica()))
                .requerAnaliseOrcamentaria(Boolean.TRUE.equals(dto.isRequerAnaliseOrcamentaria()))
                .requerConsultaExterna(Boolean.TRUE.equals(dto.isRequerConsultaExterna()))
                .build();

        if (dto.getProposicaoIds() != null) {
            dto.getProposicaoIds().forEach(processo::adicionarProposicao);
        }

        if (dto.getMateriaIds() != null) {
            dto.getMateriaIds().forEach(processo::adicionarMateria);
        }

        processo = processoRepository.save(processo);

        log.info("Processo legislativo {} criado por {}", processo.getNumero(), criador.getNome());

        return processo;
    }

    public ProcessoLegislativoDTO buscarPorId(String id) {
        ProcessoLegislativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        return converterParaDTO(processo);
    }

    public ProcessoLegislativoDTO buscarPorNumero(String numero) {
        ProcessoLegislativo processo = processoRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        return converterParaDTO(processo);
    }

    public Page<ProcessoLegislativoDTO> listar(Pageable pageable) {
        return processoRepository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    public Page<ProcessoLegislativoDTO> buscarPorStatus(StatusProcesso status, Pageable pageable) {
        return processoRepository.findByStatus(status, pageable)
                .map(this::converterParaDTO);
    }

    public Page<ProcessoLegislativoDTO> buscarPorSetor(String setorId, Pageable pageable) {
        return processoRepository.findBySetorResponsavelId(setorId, pageable)
                .map(this::converterParaDTO);
    }

    public List<ProcessoLegislativoDTO> buscarPorGestor(String gestorId) {
        return processoRepository.findByGestorId(gestorId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public ProcessoLegislativo atualizarStatus(String id, StatusProcesso novoStatus) {
        ProcessoLegislativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        processo.setStatus(novoStatus);
        processo.setDataAtualizacao(LocalDateTime.now());

        if (novoStatus == StatusProcesso.FINALIZADO || novoStatus == StatusProcesso.ARQUIVADO) {
            processo.setDataConclusao(LocalDateTime.now());
        }

        processo = processoRepository.save(processo);

        log.info("Status do processo {} atualizado para {}", processo.getNumero(), novoStatus);

        return processo;
    }

    @Transactional
    public void adicionarProposicao(String processoId, String proposicaoId) {
        ProcessoLegislativo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        if (!proposicaoRepository.existsById(proposicaoId)) {
            throw new RecursoNaoEncontradoException("Proposição não encontrada");
        }

        processo.adicionarProposicao(proposicaoId);
        processo.setDataAtualizacao(LocalDateTime.now());

        processoRepository.save(processo);

        log.info("Proposição {} adicionada ao processo {}", proposicaoId, processo.getNumero());
    }

    @Transactional
    public void adicionarMateria(String processoId, String materiaId) {
        ProcessoLegislativo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado"));

        if (!materiaRepository.existsById(materiaId)) {
            throw new RecursoNaoEncontradoException("Matéria não encontrada");
        }

        processo.adicionarMateria(materiaId);
        processo.setDataAtualizacao(LocalDateTime.now());

        processoRepository.save(processo);

        log.info("Matéria {} adicionada ao processo {}", materiaId, processo.getNumero());
    }

    private ProcessoLegislativoDTO converterParaDTO(ProcessoLegislativo processo) {
        return new ProcessoLegislativoDTO(
                processo.getId(),
                processo.getNumero(),
                processo.getTitulo(),
                processo.getDescricao(),
                processo.getStatus(),
                processo.getPrioridade(),
                processo.getProposicaoIds(),
                processo.getMateriaIds(),
                processo.getTemaPrincipal(),
                processo.getSetorResponsavelId(),
                processo.getSetorResponsavelNome(),
                processo.getGestorId(),
                processo.getGestorNome(),
                processo.getNumeroPareceresPendentes(),
                processo.getNumeroPosicionamentosPendentes(),
                processo.getDataCriacao(),
                processo.getDataAtualizacao(),
                processo.getDataConclusao()
        );
    }
}