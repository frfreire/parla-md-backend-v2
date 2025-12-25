package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Setor;
import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.dto.CriarSetorDTO;
import br.gov.md.parla_md_backend.domain.dto.AtualizarSetorDTO;
import br.gov.md.parla_md_backend.domain.dto.SetorDTO;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.ISetorRepository;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetorService {

    private final ISetorRepository setorRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional
    public SetorDTO criar(CriarSetorDTO dto) {
        log.info("Criando setor: {}", dto.sigla());

        validarSiglaUnica(dto.sigla());

        Setor setorPai = null;
        Integer nivel = 1;

        if (dto.setorPaiId() != null) {
            setorPai = setorRepository.findById(dto.setorPaiId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Setor pai não encontrado"));
            nivel = setorPai.getNivel() + 1;
        }

        Setor setor = Setor.builder()
                .nome(dto.nome())
                .sigla(dto.sigla())
                .descricao(dto.descricao())
                .setorPaiId(dto.setorPaiId())
                .nivel(nivel)
                .responsavelId(dto.responsavelId())
                .responsavelNome(dto.responsavelNome())
                .competencias(dto.competencias())
                .areasAtuacao(dto.areasAtuacao())
                .email(dto.email())
                .telefone(dto.telefone())
                .ativo(true)
                .podeEmitirParecer(dto.podeEmitirParecer() != null ? dto.podeEmitirParecer() : false)
                .recebeTramitacoes(dto.recebeTramitacoes() != null ? dto.recebeTramitacoes() : true)
                .build();

        setor = setorRepository.save(setor);

        log.info("Setor {} criado com sucesso", setor.getSigla());

        return converterParaDTO(setor);
    }

    @Transactional
    public SetorDTO atualizar(String id, AtualizarSetorDTO dto) {
        log.info("Atualizando setor: {}", id);

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        if (dto.nome() != null) {
            setor.setNome(dto.nome());
        }

        if (dto.sigla() != null && !dto.sigla().equals(setor.getSigla())) {
            validarSiglaUnica(dto.sigla());
            setor.setSigla(dto.sigla());
        }

        if (dto.descricao() != null) {
            setor.setDescricao(dto.descricao());
        }

        if (dto.responsavelId() != null) {
            Usuario responsavel = usuarioRepository.findById(dto.responsavelId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável não encontrado"));
            setor.setResponsavelId(responsavel.getId());
            setor.setResponsavelNome(responsavel.getNome());
        }

        if (dto.competencias() != null) {
            setor.setCompetencias(dto.competencias());
        }

        if (dto.areasAtuacao() != null) {
            setor.setAreasAtuacao(dto.areasAtuacao());
        }

        if (dto.email() != null) {
            setor.setEmail(dto.email());
        }

        if (dto.telefone() != null) {
            setor.setTelefone(dto.telefone());
        }

        if (dto.podeEmitirParecer() != null) {
            setor.setPodeEmitirParecer(dto.podeEmitirParecer());
        }

        if (dto.recebeTramitacoes() != null) {
            setor.setRecebeTramitacoes(dto.recebeTramitacoes());
        }

        setor = setorRepository.save(setor);

        log.info("Setor {} atualizado com sucesso", setor.getSigla());

        return converterParaDTO(setor);
    }

    public SetorDTO buscarPorId(String id) {
        log.debug("Buscando setor por ID: {}", id);

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        return converterParaDTO(setor);
    }

    public SetorDTO buscarPorSigla(String sigla) {
        log.debug("Buscando setor por sigla: {}", sigla);

        Setor setor = setorRepository.findBySigla(sigla)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        return converterParaDTO(setor);
    }

    public Page<SetorDTO> listar(Pageable pageable) {
        log.debug("Listando setores - página: {}", pageable.getPageNumber());

        return setorRepository.findAll(pageable)
                .map(this::converterParaDTO);
    }

    public List<SetorDTO> listarAtivos() {
        log.debug("Listando setores ativos");

        return setorRepository.findByAtivoTrue()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<SetorDTO> listarPorNivel(Integer nivel) {
        log.debug("Listando setores de nível: {}", nivel);

        return setorRepository.findByNivel(nivel)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<SetorDTO> listarSubsetores(String setorPaiId) {
        log.debug("Listando subsetores do setor: {}", setorPaiId);

        return setorRepository.findBySetorPaiId(setorPaiId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<SetorDTO> listarSetoresRaiz() {
        log.debug("Listando setores raiz (nível 1)");

        return setorRepository.findByNivel(1)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public List<SetorDTO> buscarPorNome(String nome) {
        log.debug("Buscando setores por nome: {}", nome);

        return setorRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public SetorDTO ativar(String id) {
        log.info("Ativando setor: {}", id);

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        setor.setAtivo(true);
        setor = setorRepository.save(setor);

        log.info("Setor {} ativado", setor.getSigla());

        return converterParaDTO(setor);
    }

    @Transactional
    public SetorDTO desativar(String id) {
        log.info("Desativando setor: {}", id);

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        setor.setAtivo(false);
        setor = setorRepository.save(setor);

        log.info("Setor {} desativado", setor.getSigla());

        return converterParaDTO(setor);
    }

    @Transactional
    public SetorDTO definirResponsavel(String setorId, String usuarioId) {
        log.info("Definindo responsável do setor: {}", setorId);

        Setor setor = setorRepository.findById(setorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        setor.setResponsavelId(usuario.getId());
        setor.setResponsavelNome(usuario.getNome());

        setor = setorRepository.save(setor);

        log.info("Responsável {} definido para setor {}", usuario.getNome(), setor.getSigla());

        return converterParaDTO(setor);
    }

    @Transactional
    public void deletar(String id) {
        log.info("Deletando setor: {}", id);

        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado"));

        List<Setor> subsetores = setorRepository.findBySetorPaiId(id);
        if (!subsetores.isEmpty()) {
            throw new IllegalStateException("Não é possível deletar setor com subsetores. Remova os subsetores primeiro.");
        }

        setorRepository.delete(setor);

        log.info("Setor {} deletado", setor.getSigla());
    }

    private void validarSiglaUnica(String sigla) {
        if (setorRepository.existsBySigla(sigla)) {
            throw new IllegalArgumentException("Já existe setor com esta sigla: " + sigla);
        }
    }

    private SetorDTO converterParaDTO(Setor setor) {
        return new SetorDTO(
                setor.getId(),
                setor.getNome(),
                setor.getSigla(),
                setor.getDescricao(),
                setor.getSetorPaiId(),
                setor.getNivel(),
                setor.getResponsavelId(),
                setor.getResponsavelNome(),
                setor.getCompetencias(),
                setor.getAreasAtuacao(),
                setor.getEmail(),
                setor.getTelefone(),
                setor.isAtivo(),
                setor.isPodeEmitirParecer(),
                setor.isRecebeTramitacoes()
        );
    }
}
